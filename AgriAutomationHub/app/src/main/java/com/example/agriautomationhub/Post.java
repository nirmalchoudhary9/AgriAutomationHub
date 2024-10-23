package com.example.agriautomationhub;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("thread")
    private NewsThread thread;

    public NewsThread getThread() {
        return thread;
    }
}

