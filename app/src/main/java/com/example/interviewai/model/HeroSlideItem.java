package com.example.interviewai.model;

/**
 * Simple data holder for a hero carousel slide that only displays an image.
 */
public class HeroSlideItem {
    private final int imageResId;

    public HeroSlideItem(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getImageResId() {
        return imageResId;
    }
}
