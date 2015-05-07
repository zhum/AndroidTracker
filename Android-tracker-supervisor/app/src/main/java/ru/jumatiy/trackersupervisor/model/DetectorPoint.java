package ru.jumatiy.trackersupervisor.model;

import android.location.Location;
import uz.droid.orm.annotation.Column;
import uz.droid.orm.annotation.Id;
import uz.droid.orm.annotation.Table;
import uz.droid.orm.model.DBObject;

import java.text.DecimalFormat;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 10:40.
 */

@Table(name = "detector")
public class DetectorPoint implements DBObject {

    @Id(autoIncrement = true)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "radius")
    private Integer radius;

    @Column(name = "has_inside")
    private boolean hasInside;

    @Column(name = "notification")
    private String notification;

    @Column(name = "notification_uri")
    private String notificationUri;

    public DetectorPoint() {
    }

    public DetectorPoint(String name, Double latitude, Double longitude, Integer radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public boolean hitTest(TrackLocation loc) {
        Location loc1 = new Location("GPS");
        loc1.setLatitude(latitude);
        loc1.setLongitude(longitude);

        Location loc2 = new Location("GPS");
        loc2.setLatitude(loc.getLatitude());
        loc2.setLongitude(loc.getLongitude());

        return loc1.distanceTo(loc2) < radius;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public boolean isHasInside() {
        return hasInside;
    }

    public void setHasInside(boolean hasInside) {
        this.hasInside = hasInside;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }


    public String getNotificationUri() {
        return notificationUri;
    }

    public void setNotificationUri(String notificationUri) {
        this.notificationUri = notificationUri;
    }

    @Override
    public String toString() {

        String lat = new DecimalFormat("#.####").format(latitude);
        String lng = new DecimalFormat("#.####").format(longitude);


        return lat + " " + lng + "; " + radius + "м";
    }
}
