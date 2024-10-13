package com.example.agriautomationhub;

public class Service {
    private int nameResId;
    private int imageResId;

    public Service(int nameResId, int imageResId) {
        this.nameResId = nameResId;
        this.imageResId = imageResId;
    }

    public int getName() {
        return nameResId; // Just return the resource ID
    }

    public int getImageResId() {
        return imageResId;
    }
}

