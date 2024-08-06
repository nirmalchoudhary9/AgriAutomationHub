package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GNewsApiService {
    @GET("search")
    Call<GNewsResponse> getNewsArticles(
            @Query("q") String query,
            @Query("country") String country,
            @Query("lang") String language,
            @Query("token") String apiKey
    );
}
