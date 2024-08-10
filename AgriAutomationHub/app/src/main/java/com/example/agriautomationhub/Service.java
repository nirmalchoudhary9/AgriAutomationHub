package com.example.agriautomationhub;

public class Service {
    private String name;
    private int imageResource;

    public Service(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResource;
    }
}
