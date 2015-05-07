package ru.jumatiy.trackersupervisor.util;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 11:06.
 */

@Singleton
public class Settings {
    private static final long DEFAULT_LOAD_TRACK_INTERVAL = 60000;

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
     * return get track interval in milliseconds
    * */
    public long getLoadTrackInterval() {
        return preferences.getLong("LOAD_TRACK_INTERVAL", DEFAULT_LOAD_TRACK_INTERVAL);
    }

    public void setLoadTrackInterval(long val) {
        preferences.edit().putLong("LOAD_TRACK_INTERVAL", val).apply();
    }


    public long getNextLoadUpTime() {
        return preferences.getLong("NEXT_LOAD_UP_TIME", Long.MIN_VALUE);
    }

    public void setNextLoadUpTime(long val) {
        preferences.edit().putLong("NEXT_LOAD_UP_TIME", val).apply();
    }

    public int[] getSilentTime() {
        int [] time = new int[4];

        time[0] = preferences.getInt("SILENT_HOUR_START", 0);
        time[1] = preferences.getInt("SILENT_MINUTE_START", 0);
        time[2] = preferences.getInt("SILENT_HOUR_END", 0);
        time[3] = preferences.getInt("SILENT_MINUTE_END", 0);

        return time;
    }

    public void saveSilentTime(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        preferences.edit()
                .putInt("SILENT_HOUR_START", hourStart)
                .putInt("SILENT_MINUTE_START", minuteStart)
                .putInt("SILENT_HOUR_END", hourEnd)
                .putInt("SILENT_MINUTE_END", minuteEnd).apply();
    }

    public long getLastNotificationTrackTime() {
        return preferences.getLong("LAST_NOTIFIED_TRACK_TIME", Long.MIN_VALUE);
    }

    public void saveLastNotificationTrackTime(long trackTime) {
        preferences.edit().putLong("LAST_NOTIFIED_TRACK_TIME", trackTime).apply();
    }


}
