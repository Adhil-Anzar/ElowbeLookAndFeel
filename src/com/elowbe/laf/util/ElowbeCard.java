package com.elowbe.laf.util;

import javax.swing.Icon;

/**
 * A small, renderer-friendly model for card style list items.
 *
 * <p>Place instances in a {@link javax.swing.JList} and Elowbe's list UI will
 * render them as rounded cards with a title, body and optional caption. Any
 * non-card value falls back to the list's regular cell renderer, so a card list
 * can be introduced without replacing existing renderers.</p>
 */
public class ElowbeCard {
    private String title;
    private String body;
    private String caption;
    private Icon icon;
    private boolean html;

    public ElowbeCard() {
    }

    public ElowbeCard(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public ElowbeCard(String title, String body, String caption) {
        this.title = title;
        this.body = body;
        this.caption = caption;
    }

    /**
     * Creates a card with a plain-text body.
     */
    public ElowbeCard(String body) {
        this.body = body;
    }

    /**
     * Creates a card whose body is raw HTML. The markup is rendered as-is.
     */
    public static ElowbeCard html(String html) {
        ElowbeCard card = new ElowbeCard(html);
        card.setHtml(true);
        return card;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Whether {@link #getBody()} contains raw HTML that should not be escaped.
     */
    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    /**
     * @deprecated Use {@link #isHtml()}.
     */
    @Deprecated
    public boolean isForceHtml() {
        return isHtml();
    }

    /**
     * @deprecated Use {@link #setHtml(boolean)}.
     */
    @Deprecated
    public void setForceHtml(boolean forceHtml) {
        setHtml(forceHtml);
    }

    @Override
    public String toString() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        if (body != null && !body.isBlank()) {
            return body;
        }
        return caption == null ? "" : caption;
    }
}
