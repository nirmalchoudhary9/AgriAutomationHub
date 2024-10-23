package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GNewsApiService {
    @GET("v2/everything")
    Call<GNewsResponse> getNewsArticles(
            @Query("q") String query,
            @Query("language") String language,
            @Query("apiKey") String apiKey
    );
}


