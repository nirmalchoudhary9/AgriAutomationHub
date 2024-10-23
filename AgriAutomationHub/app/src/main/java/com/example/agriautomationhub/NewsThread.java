package com.example.agriautomationhub;

import com.google.gson.annotations.SerializedName;

public class NewsThread {
    @SerializedName("uuid")
    private String uuid;

    @SerializedName("url")
    private String url;

    @SerializedName("title")
    private String title;

    @SerializedName("published")
    private String published;

    @SerializedName("site_full")
    private String siteFull;

    @SerializedName("main_image")
    private String mainImage;

    public String getUuid() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPublished() {
        return published;
    }

    public String getSiteFull() {
        return siteFull;
    }

    public String getMainImage() {
        return mainImage;
    }
}
