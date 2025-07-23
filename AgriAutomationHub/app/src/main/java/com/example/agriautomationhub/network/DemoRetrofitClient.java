package com.example.agriautomationhub.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DemoRetrofitClient {
    private static final String BASE_URL = "https://crop-disease-prediction-ml-api.onrender.com/";
    private static DemoDiseaseService service;

    public static DemoDiseaseService getInstance() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(DemoDiseaseService.class);
        }
        return service;
    }
}
