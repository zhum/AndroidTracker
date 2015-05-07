package ru.jumatiy.tracker.util;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 09.03.2015 12:19.
 */

@Singleton
public class Settings {
    private static final long DEFAULT_LOCATION_UPDATE_PERIOD = 60000;
    private static final long DEFAULT_LOCATION_SEND_PERIOD = 300000;
    private static final long DEFAULT_NETWORK_LOCATION_PRIORITY_TIMEOUT = 30000;

    private final SharedPreferences preferences;

    @Inject
    public Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getApiUrl() {
        return preferences.getString("API_URL", "http://0.0.0.0:4567");
    }

    public void saveApiUrl(String url) {
        preferences.edit().putString("API_URL", url).apply();
    }

    /**
     * return location update interval in milliseconds
    * */
    public long getLocationUpdateInterval() {
        return preferences.getLong("LOCATION_UPDATE_INTERVAL", DEFAULT_LOCATION_UPDATE_PERIOD);
    }

    public void setLocationUpdateInterval(long val) {
        preferences.edit().putLong("LOCATION_UPDATE_INTERVAL", val).apply();
    }

    /**
     * return location send inter in milliseconds
     * */
    public long getLocationSendInterval() {
        return preferences.getLong("LOCATION_SEND_INTERVAL", DEFAULT_LOCATION_SEND_PERIOD);
    }

    public void setLocationSendInterval(long val) {
        preferences.edit().putLong("LOCATION_SEND_INTERVAL", val).apply();
    }

    public boolean isTrackerActive() {
        return preferences.getBoolean("IS_TRACKER_ACTIVE", true);
    }

    public void setTrackerActive(boolean val) {
        preferences.edit().putBoolean("IS_TRACKER_ACTIVE", val).apply();
    }

    public long getLocationSendUpTime() {
        return preferences.getLong("LOCATION_SEND_UP_TIME", Long.MIN_VALUE);
    }

    public void setLocationSendUpTime(long val) {
        preferences.edit().putLong("LOCATION_SEND_UP_TIME", val).apply();
    }

    public long getLocationUpTime() {
        return preferences.getLong("LOCATION_UP_TIME", Long.MIN_VALUE);
    }

    public void setLocationUpTime(long val) {
        preferences.edit().putLong("LOCATION_UP_TIME", val).apply();
    }

    public long getNetworkLocationPriorityTimeout() {
        return preferences.getLong("NETWORK_LOCATION_PRIORITY_TIMEOUT", DEFAULT_NETWORK_LOCATION_PRIORITY_TIMEOUT);
    }

    public void setNetworkLocationPriorityTimeout(long val) {
        preferences.edit().putLong("NETWORK_LOCATION_PRIORITY_TIMEOUT", val).apply();
    }



}
