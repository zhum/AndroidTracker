package ru.jumatiy.tracker.api;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;


/**
 * Created by Sarimsakov Bakhrom Azimovich on 14.04.2015 14:31.
 */
public interface Api {

    @POST("/")
    @FormUrlEncoded
    void sendLocation(@Field("lat") Double latitude,
                      @Field("long") Double longitude,
                      @Field("time") Long time,
                      @Field("accuracy") Float accuracy,
                      @Field("device") String device,
                      Callback<String> callback);
}
