package com.spf.album.event;

public class WordMarkEvent {
    private String content;
    private int color;

    public WordMarkEvent(String content, int color) {
        this.content = content;
        this.color = color;
    }

    public String getContent() {
        return content;
    }

    public int getColor() {
        return color;
    }
}