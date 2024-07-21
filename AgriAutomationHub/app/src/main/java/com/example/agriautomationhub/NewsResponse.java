// NewsResponse.java
package com.example.agriautomationhub;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("articles")
    private List<Article> articles;

    // Getters and setters
    @SuppressWarnings("unused")
    public String getStatus() {
        return status;
    }

    @SuppressWarnings("unused")
    public void setStatus(String status) {
        this.status = status;
    }

    @SuppressWarnings("unused")
    public int getTotalResults() {
        return totalResults;
    }

    @SuppressWarnings("unused")
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<Article> getArticles() {
        return articles;
    }

    @SuppressWarnings("unused")
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}

