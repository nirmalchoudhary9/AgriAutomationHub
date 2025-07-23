package com.example.agriautomationhub.network;

import com.example.agriautomationhub.network.model.ApiResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface PlantixService {

    @Multipart
    @POST("v1/image-diagnosis")
    Call<ApiResponse> diagnose(
            @Header("Authorization") String bearer,
            @Part MultipartBody.Part image);
}
