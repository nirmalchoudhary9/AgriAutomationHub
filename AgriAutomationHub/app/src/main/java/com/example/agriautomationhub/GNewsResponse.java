package com.example.agriautomationhub;

import java.util.List;

public class GNewsResponse {
    private List<GNewsArticle> articles;

    public List<GNewsArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<GNewsArticle> articles) {
        this.articles = articles;
    }
}

