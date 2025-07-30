package com.example.agriautomationhub.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DemoRetrofitClient {

    private static final String BASE_URL = "https://crop.kindwise.com/";
    private static DemoRetrofitClient instance;
    private DemoApiService api;

    private DemoRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(DemoApiService.class);
    }

    public static synchronized DemoRetrofitClient getInstance() {
        if (instance == null) {
            instance = new DemoRetrofitClient();
        }
        return instance;
    }

    public DemoApiService getApi() {
        return api;
    }
}
