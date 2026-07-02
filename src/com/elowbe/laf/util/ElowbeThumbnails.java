package com.elowbe.laf.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.SwingUtilities;

/**
 * Asynchronous thumbnail loader for the file chooser gallery view. Images are
 * decoded with ImageIO (subsampled for speed); video frames are extracted with
 * macOS Quick Look (qlmanage) or ffmpeg when available.
 */
public final class ElowbeThumbnails {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "wbmp", "tif", "tiff");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "m4v", "avi", "mkv", "webm", "mpg", "mpeg", "wmv", "flv");
    private static final int CACHE_LIMIT = 256;

    private static final Map<String, Image> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                    return size() > CACHE_LIMIT;
                }
            });
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();
    private static final ExecutorService LOADER = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "elowbe-thumbnails");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    private ElowbeThumbnails() {
    }

    public static boolean isImage(File file) {
        return IMAGE_EXTENSIONS.contains(extension(file));
    }

    public static boolean isVideo(File file) {
        return VIDEO_EXTENSIONS.contains(extension(file));
    }

    public static boolean isMedia(File file) {
        return isImage(file) || isVideo(file);
    }

    /**
     * Returns the cached thumbnail, or null while it loads in the background.
     * {@code onReady} runs on the EDT once the thumbnail becomes available.
     */
    public static Image thumbnail(File file, int maxWidth, int maxHeight, Runnable onReady) {
        if (file == null || !isMedia(file)) {
            return null;
        }
        String key = file.getAbsolutePath() + "|" + file.lastModified() + "|" + maxWidth + "x" + maxHeight;
        Image cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (FAILED.contains(key) || !PENDING.add(key)) {
            return null;
        }
        LOADER.execute(() -> {
            Image image = null;
            try {
                image = load(file, maxWidth, maxHeight);
            } catch (Exception ex) {
                // Unreadable or unsupported file; fall through to the failed set.
            }
            if (image != null) {
                CACHE.put(key, image);
            } else {
                FAILED.add(key);
            }
            PENDING.remove(key);
            if (image != null && onReady != null) {
                SwingUtilities.invokeLater(onReady);
            }
        });
        return null;
    }

    private static String extension(File file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static Image load(File file, int maxWidth, int maxHeight) throws IOException {
        if (isImage(file)) {
            return readScaled(file, maxWidth, maxHeight);
        }
        return videoFrame(file, maxWidth, maxHeight);
    }

    private static BufferedImage readScaled(File file, int maxWidth, int maxHeight) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                int sampling = Math.max(1, Math.min(width / Math.max(1, maxWidth), height / Math.max(1, maxHeight)));
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(sampling, sampling, 0, 0);
                return scaleToFit(reader.read(0, param), maxWidth, maxHeight);
            } finally {
                reader.dispose();
            }
        }
    }

    private static BufferedImage scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
        if (image == null) {
            return null;
        }
        double scale = Math.min(1d,
                Math.min(maxWidth / (double) image.getWidth(), maxHeight / (double) image.getHeight()));
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        if (width == image.getWidth() && height == image.getHeight()) {
            return image;
        }
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(image, 0, 0, width, height, null);
        } finally {
            g2.dispose();
        }
        return scaled;
    }

    private static BufferedImage videoFrame(File file, int maxWidth, int maxHeight) {
        Path directory = null;
        try {
            directory = Files.createTempDirectory("elowbe-thumb");
            BufferedImage frame = quickLookFrame(file, directory);
            if (frame == null) {
                frame = ffmpegFrame(file, directory);
            }
            return scaleToFit(frame, maxWidth, maxHeight);
        } catch (Exception ex) {
            return null;
        } finally {
            deleteRecursively(directory);
        }
    }

    private static BufferedImage quickLookFrame(File file, Path directory) {
        File qlmanage = new File("/usr/bin/qlmanage");
        if (!qlmanage.canExecute()) {
            return null;
        }
        try {
            Process process = new ProcessBuilder(qlmanage.getAbsolutePath(), "-t", "-s", "512",
                    "-o", directory.toString(), file.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            File output = new File(directory.toFile(), file.getName() + ".png");
            return output.isFile() ? ImageIO.read(output) : null;
        } catch (IOException ex) {
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static BufferedImage ffmpegFrame(File file, Path directory) {
        File output = new File(directory.toFile(), "frame.png");
        try {
            Process process = new ProcessBuilder("ffmpeg", "-y", "-ss", "1", "-i", file.getAbsolutePath(),
                    "-frames:v", "1", output.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            return output.isFile() ? ImageIO.read(output) : null;
        } catch (IOException ex) {
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static void deleteRecursively(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        } catch (IOException ex) {
            // Leftover temp files are cleaned by the OS eventually.
        }
    }
}
