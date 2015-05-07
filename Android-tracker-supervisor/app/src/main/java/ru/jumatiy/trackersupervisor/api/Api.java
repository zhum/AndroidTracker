package ru.jumatiy.trackersupervisor.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import ru.jumatiy.trackersupervisor.model.TrackLocation;

import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 21:55.
 */
public interface Api {

    @GET("/")
    void getLocations(@Query("start") Long startTime, @Query("end") Long endTime, @Query("limit") int limit, Callback<List<TrackLocation>> callback);

}
