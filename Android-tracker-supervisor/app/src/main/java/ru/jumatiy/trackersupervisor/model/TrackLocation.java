package ru.jumatiy.trackersupervisor.model;

import com.google.gson.annotations.SerializedName;
import uz.droid.orm.annotation.Column;
import uz.droid.orm.annotation.Id;
import uz.droid.orm.annotation.Table;
import uz.droid.orm.model.DBObject;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 21:57.
 */

@Table(name = "location")
public class TrackLocation implements DBObject{

    @Id
    @Column(name = "id")
    private Long id;


    @Column(name = "latitude")
    @SerializedName("lat")
    private Double latitude;

    @Column(name = "longitude")
    @SerializedName("long")
    private Double longitude;

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "device")
    private String device;

    @Column(name = "time")
    private Long time;

    public TrackLocation() {
    }

    public TrackLocation(Double latitude, Double longitude, String device, Long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.device = device;
        this.time = time;
    }

    public boolean isValid() {
        return time != null && latitude != null && longitude != null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public String getDevice() {
        return device;
    }

    public Long getTime() {
        return time;
    }
}
