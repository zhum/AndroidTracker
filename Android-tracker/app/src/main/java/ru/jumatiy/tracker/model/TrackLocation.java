package ru.jumatiy.tracker.model;

import android.location.Location;
import android.location.LocationManager;
import uz.droid.orm.annotation.*;
import uz.droid.orm.model.DBObject;

import java.util.logging.LogManager;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 11.04.2015 20:29.
 */

@Table(name = "location")
public class TrackLocation implements DBObject {

    @Id(autoIncrement = true)
    @Column(name = "id")
    private Long id;

    @Column(name = "provider")
    private String provider;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "accuracy")
    private Float accuracy;

    @Column(name = "speed")
    private Float speed;

    @Column(name = "time")
    private Long time;

    @Column(name = "bearing")
    private Float bearing;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "is_sent")
    private boolean isSent;

    // Don't remove required for Orm
    public TrackLocation() {
    }


    public TrackLocation(Location loc) {
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();

        accuracy = loc.getAccuracy();
        provider = loc.getProvider();
        speed = loc.getSpeed();
        time = loc.getTime();
        bearing = loc.getBearing();
        altitude = loc.getAltitude();
        isSent = false;
    }

    public Long getId() {
        return id;
    }

    public String getProvider() {
        return provider;
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

    public Float getSpeed() {
        return speed;
    }

    public Long getTime() {
        return time;
    }

    public Float getBearing() {
        return bearing;
    }

    public Double getAltitude() {
        return altitude;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }
}
