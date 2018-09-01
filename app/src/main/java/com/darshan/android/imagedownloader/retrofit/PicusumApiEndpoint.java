package com.darshan.android.imagedownloader.retrofit;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PicusumApiEndpoint {

    @GET("list")
    Call<ArrayList<Image>> getImages();

}
