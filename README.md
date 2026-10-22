# Elowbe LookAndFeel

A modern Look and Feel for Java Swing heavily inspired by [shadcn](https://ui.shadcn.com/).

<p align="center">
<img width="2287" height="1179" alt="elowbe-split" src="https://github.com/user-attachments/assets/e3f9ac25-dffd-4a5b-bf95-af4b9783f37e" />
</p>

## Features

- **Light and dark themes** — switch at runtime with `Elowbe.setTheme(...)` or
  `Elowbe.toggleTheme()`; open windows are refreshed automatically.
- **Modern component styling** — custom UI delegates for buttons, text inputs,
  combo boxes, check boxes, radio buttons, sliders, scroll bars, spinners,
  tabbed panes, tables, lists, menus, progress bars, panels, file choosers and
  color choosers.
- **Consistent input chrome** — rounded fields with a visible focus outline,
  disabled states and theme-aware colors shared by text fields, password
  fields, text areas, combo boxes and spinners.
- **Placeholder text** — set the `ElowbeDefaults.PLACEHOLDER_KEY` client
  property on any text component.
- **Button variants** — `primary`, `secondary`, `outline`, `ghost`,
  `destructive`, `link`, plus a pill shape via `ElowbeDefaults.ROUNDED_KEY`.
- **Card lists** — put `ElowbeCard` values in a `JList` to render rounded,
  theme-aware cards. Any non-card value still uses the regular cell renderer,
  so existing lists keep working.
- **Line chart** (`Elowbe2DGraph`) — scrollable, theme-aware line chart with
  configurable axis titles, spacing, tick intervals, value formatting, colors,
  point labels, tooltips, mouse selection and keyboard navigation.
- **Node graph** (`NodeCanvas`) — draggable nodes, terminals, connections,
  pan/zoom, selection, clipboard, undo/redo and JSON import/export. Keyboard
  shortcuts can be disabled with `new NodeCanvas(false)`.
- **Gradient panel** (`ElowbeGradientRectangle`) — rounded vertical or
  horizontal gradient that follows the active theme unless colors are supplied.
- **Command palette** — `ElowbeLookAndFeel.CommandPallete` provides a searchable
  popup list for commands or any other items.
- **Theme-aware icon set** — `ElowbeIcons` draws crisp vector-style Swing icons
  in the current palette colors.

## Installation

You need **Java 17** or later.

### Option 1: Download a release JAR

1. Open the [Releases](https://github.com/elowbe/ElowbeLookAndFeel/releases) page.
2. Download `ElowbeLookAndFeel-<version>.jar`.
3. Add the JAR to your application's classpath (build tool dependency, `-cp`, or IDE library).
4. Add [JIconFont](https://jiconfont.github.io/swing) to your dependencies:

```xml
<dependency>
  <groupId>com.github.jiconfont</groupId>
  <artifactId>jiconfont-swing</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.github.jiconfont</groupId>
  <artifactId>jiconfont-font_awesome</artifactId>
  <version>4.7.0.1</version>
</dependency>
```

### Option 2: Build from source

```bash
git clone https://github.com/elowbe/ElowbeLookAndFeel.git
cd ElowbeLookAndFeel
mvn package
```

The built JAR is at `target/ElowbeLookAndFeel-<version>.jar`.

To install into your local Maven repository:

```bash
mvn install
```

Then add it as a dependency in your project:

```xml
<dependency>
  <groupId>ElowbeLookAndFeel</groupId>
  <artifactId>ElowbeLookAndFeel</artifactId>
  <version><!-- release version --></version>
</dependency>
```

## Running the demos

The demos showcase Elowbe on real Swing UIs — a component gallery, dialogs, a
kanban board, a note-taking app and a node graph editor.

### Option 1: Download the demos JAR (releases)

1. From the [Releases](https://github.com/elowbe/ElowbeLookAndFeel/releases) page, download `ElowbeLookAndFeel-demos-<version>.jar`.
2. Run it:

```bash
java -jar ElowbeLookAndFeel-demos-<version>.jar
```

If the demos JAR requires the main library on the classpath, run:

```bash
java -cp "ElowbeLookAndFeel-<version>.jar:ElowbeLookAndFeel-demos-<version>.jar" com.elowbe.demo.ElowbeDemoApp
```

On Windows, use `;` instead of `:` in the classpath.

### Option 2: Run demos from source

From a cloned repository, after building:

```bash
mvn exec:java
```

That launches the **Component Gallery** demo (`ElowbeDemoApp`) by default.

To run a specific demo:

| Demo | Main class | Description |
|------|------------|-------------|
| Component Gallery | `com.elowbe.demo.ElowbeDemoApp` | Buttons, inputs, charts, card lists, tables, tabs and other styled components |
| Dialogs | `com.elowbe.demo.DialogDemoApp` | Option panes, file chooser and color chooser |
| Kanban Board | `com.elowbe.demo.KanbanBoardDemoApp` | Drag-and-drop task board |
| Note Taking | `com.elowbe.demo.NoteTakingDemoApp` | Searchable notes with categories and preview |
| Node Graph | `com.elowbe.demo.NodeGraphDemoApp` | Draggable modular nodes with colored terminals and connections |

Example — run the kanban demo:

```bash
mvn exec:java -Dexec.mainClass=com.elowbe.demo.KanbanBoardDemoApp
```

Or with `java` directly (after `mvn package`):

```bash
java -cp "target/ElowbeLookAndFeel-<version>.jar:target/classes" com.elowbe.demo.KanbanBoardDemoApp
```

## Usage

### Apply Elowbe to a Swing application

Install the Look and Feel before creating UI components, ideally on the Event
Dispatch Thread:

```java
import com.elowbe.laf.Elowbe;
import com.elowbe.laf.theme.ElowbeTheme;

Elowbe.install(ElowbeTheme.LIGHT); // or ElowbeTheme.DARK
```

Or use the default light theme:

```java
Elowbe.install();
```

Elowbe is a regular Swing `LookAndFeel`, so this also works:

```java
UIManager.setLookAndFeel(new ElowbeLookAndFeel(ElowbeTheme.DARK));
```

### Switch themes at runtime

```java
Elowbe.setTheme(ElowbeTheme.DARK);
// or
Elowbe.toggleTheme();
```

`setTheme` and `toggleTheme` refresh all open windows automatically. Use
`Elowbe.refresh(component)` to refresh a single component tree.

### Placeholder text and input states

```java
JTextField search = new JTextField();
search.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "Search...");

JPasswordField password = new JPasswordField();
password.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "Password");
```

Placeholders work for text fields, password fields and text areas. Focus,
disabled and selection colors are handled by the Look and Feel.

### Button variants and shape

```java
JButton primary = new JButton("Save");
primary.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, "primary");

JButton outline = new JButton("Cancel");
outline.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, "outline");

JButton pill = new JButton("Rounded");
pill.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Boolean.TRUE);
```

Supported variants are `primary`, `secondary`, `outline`, `ghost`,
`destructive` and `link`.

### Card lists

Add `ElowbeCard` values to a `JList`. The list UI renders them as rounded cards
with an optional icon, title, body and caption. Values that are not
`ElowbeCard` instances are delegated to the regular list cell renderer.

```java
DefaultListModel<ElowbeCard> model = new DefaultListModel<>();
model.addElement(new ElowbeCard(
        "Design review",
        "Walk through the new components.",
        "Today, 10:00"));
model.addElement(new ElowbeCard(
        "Release notes",
        "Summarize the UI polish.",
        "Tomorrow, 14:00"));

JList<ElowbeCard> list = new JList<>(model);
list.setVisibleRowCount(3);
```

Cards accept an optional icon and per-card colors come from the active theme:

```java
ElowbeCard card = new ElowbeCard("Design review", "Walk through the new components.");
card.setIcon(ElowbeIcons.star(16));
```

Cards may also be created from HTML:

```java
ElowbeCard card = ElowbeCard.html("<b>Rich</b> <i>content</i>");
```

### Line chart

`Elowbe2DGraph` is domain neutral: each point carries a numeric value and an
optional label. Points are placed left to right with a fixed pixel spacing. Put
the chart in a `JScrollPane` for horizontal scrolling.

```java
Elowbe2DGraph graph = new Elowbe2DGraph();
graph.setXAxisTitle("Month");
graph.setYAxisTitle("Revenue");
graph.setXValuePerPoint(1);     // value between two points
graph.setXTickInterval(4);      // draw a tick every 4 points
graph.setPointSpacing(96);      // pixels between points
graph.addPoint(new ElowbeGraphPoint(120, "Jan"));
graph.addPoint(new ElowbeGraphPoint(180, "Feb"));
graph.addPoint(new ElowbeGraphPoint(150, "Mar"));
```

Selection and formatting:

```java
graph.addPointSelectionListener(new PointSelectionListener() {
    @Override
    public void pointSelected(ElowbeGraphPoint point) {
        System.out.println("Selected " + point.getLabel());
    }

    @Override
    public void pointSelectionCleared() {
        System.out.println("Selection cleared");
    }
});

graph.setValueFormatter(value -> String.format("$%.0f", value));
graph.setLineColor(new Color(59, 130, 246));
graph.setGridVisible(false);
```

Other useful options include `setPointColor`, `setLabelColor`,
`setLabelsVisible`, `setPointsVisible`, `setLineVisible`, `setPointRadius`,
`setLineWidth`, `setPadding`, `setYHeadroom` and `setValueFormatter`. The chart
supports tooltips over points and left/right keyboard navigation when focused.

### Gradient panel

```java
ElowbeGradientRectangle header = new ElowbeGradientRectangle();
header.setRadius(ElowbeDefaults.RADIUS_LG);
header.setDirection(ElowbeGradientRectangle.Direction.HORIZONTAL);
header.setColors(new Color(0x7fffaa), PaintUtils.palette().background);
```

Without explicit colors the panel uses the current theme's accent and
background colors.

### Node graph

`NodeCanvas` is a self-contained graph editor component:

```java
NodeCanvas canvas = new NodeCanvas();
canvas.setKeyBindingsEnabled(false); // optional: disable built-in shortcuts
NodeComponent node = new NodeComponent("Input", "Source asset");
canvas.addNode(node, 40, 40);
```

It supports node dragging, terminal connections, marquee selection, pan/zoom,
clipboard operations, undo/redo and JSON import/export with `toJson()` and
`loadJson(String)`.

## Styled components

Elowbe installs custom UI delegates for:

- Buttons and toggle buttons
- Text fields, formatted text fields, password fields and text areas
- Combo boxes, spinners, check boxes and radio buttons
- Lists, tables and table headers
- Sliders, scroll bars and progress bars
- Tabbed panes and panels
- Menu bars, menus, menu items, check box/radio button menu items and popup menus
- File choosers and color choosers
- Tooltips and option panes (through theme defaults)

Components without a custom delegate fall back to the platform defaults but
still inherit Elowbe fonts, colors and metrics.

## FAQ

### How do I apply Elowbe to my Swing application?

See [Apply Elowbe to a Swing application](#apply-elowbe-to-a-swing-application).

### How do I switch between light and dark at runtime?

```java
Elowbe.setTheme(ElowbeTheme.DARK);
// or
Elowbe.toggleTheme();
```

### What Java version is required?

Java **17** or later. The project is built with Maven using `--release 17`.

### What font is it using?

The font is [Vercel's Geist](https://vercel.com/font).

### Can I use Elowbe alongside other Look and Feels?

Yes. Elowbe is a standard Swing `LookAndFeel`. Install it with
`Elowbe.install()` or `UIManager.setLookAndFeel(new ElowbeLookAndFeel())` like
any other LAF. Only one Look and Feel is active at a time.

### Does Elowbe work on macOS, Windows, and Linux?

Yes. Elowbe is a cross-platform Swing Look and Feel and runs anywhere Java 17+
and Swing are supported.

### Can I use the card renderer with existing lists?

Yes. Non-`ElowbeCard` values are passed to the list's regular cell renderer, so
the card renderer can be adopted incrementally.

### Where do I report bugs or request features?

Open an issue on [GitHub](https://github.com/elowbe/ElowbeLookAndFeel/issues).

## Additional Screenshots

<p align="center">

<img width="1162" height="706" alt="Screenshot 2026-05-25 at 10 17 31 AM" src="https://github.com/user-attachments/assets/875e52e9-9338-49f7-993a-38c5be46231d" />
<img width="2330" height="1347" alt="elowbe-notes-light-crop" src="https://github.com/user-attachments/assets/75d07c98-f107-42d4-ad73-7d60093f411d" />

</p>
