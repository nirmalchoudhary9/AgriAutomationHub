package com.example.agriautomationhub;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(@Query("lat") double latitude,
                                            @Query("lon") double longitude,
                                            @Query("appid") String apiKey,
                                            @Query("units") String units);
}

