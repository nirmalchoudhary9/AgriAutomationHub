package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("newsApiLite")
    Call<NewsResponse> getNews(
            @Query("token") String token,
            @Query("q") String query,
            @Query("country") String country,
            @Query("language") String language,
            @Query("site_type") String siteType
    );
}

