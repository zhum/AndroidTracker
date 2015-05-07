package ru.jumatiy.tracker.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.jumatiy.tracker.api.Api;
import ru.jumatiy.tracker.application.TrackerApplication;
import ru.jumatiy.tracker.event.GlobalEventHandler;
import ru.jumatiy.tracker.event.TrackerEvent;
import ru.jumatiy.tracker.model.TrackLocation;
import ru.jumatiy.tracker.orm.TrackLocationDao;
import ru.jumatiy.tracker.receiver.LocationRequestReceiver;
import ru.jumatiy.tracker.receiver.SendToServerReceiver;
import ru.jumatiy.tracker.util.Settings;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;
import uz.droid.orm.criteria.Criteria;
import uz.droid.orm.criteria.DBOrder;
import uz.droid.orm.criteria.Expression;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 11.04.2015 20:52.
 */
public class TrackerService extends Service {

    public static final String LOCATION_REQUEST_INTENT = "ru.jumatiy.tracker.LOCATION_REQUEST_INTENT";
    public static final String SEND_TO_SERVER_INTENT = "ru.jumatiy.tracker.SEND_TO_SERVER_INTENT";

    @Inject
    Settings settings;

    @Inject
    DatabaseQueue databaseQueue;

    @Inject
    TrackLocationDao trackLocationDao;

    @Inject
    GlobalEventHandler eventHandler;

    @Inject
    Api api;

    @Inject
    DatabaseQueue db;

    @Inject
    TrackLocationDao dao;

    private static volatile PowerManager.WakeLock mWakeLock;
    private LocationManager mLocationManager;

    private PendingIntent mLocationUpdateAlarmIntent;
    private PendingIntent mSendLocationAlarmIntent;
    private BroadcastReceiver mRequestLocationBroadcastReceiver;
    private BroadcastReceiver mSendLocationBroadcastReceiver;
    private boolean mGPSProviderEnabled = true;
    private boolean mNetworkProviderEnabled = true;
    private Location mGpsLocation;
    private long gpsLocationRequestTime;
    private boolean mLockLocationListener = false;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        ((TrackerApplication) getApplication()).inject(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (settings.isTrackerActive()) {
            startTracking();
        } else {
            stopTracking();
        }

        IntentFilter filter = new IntentFilter(SEND_TO_SERVER_INTENT);
        mSendLocationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setNextSendAlarm();
            }
        };

        registerReceiver(mSendLocationBroadcastReceiver, filter);

        setNextSendAlarm();
    }

    private void startTracking() {
        if (!settings.isTrackerActive()) {
            settings.setTrackerActive(true);
        }
        IntentFilter filter = new IntentFilter(LOCATION_REQUEST_INTENT);
        mRequestLocationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                requestLocation();
            }
        };

        registerReceiver(mRequestLocationBroadcastReceiver, filter);
        requestLocation();
    }

    private void stopTracking() {
        unregisterAllReceivers();
        settings.setTrackerActive(false);
    }

    private void unregisterAllReceivers() {
        try {
            if (mLocationUpdateAlarmIntent != null) {
                getAlarmManager().cancel(mLocationUpdateAlarmIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mRequestLocationBroadcastReceiver != null) {
                unregisterReceiver(mRequestLocationBroadcastReceiver);
            }
        } catch (Exception e) {
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private void requestLocation() {
        long upTime = settings.getLocationUpTime();
        boolean isValidUpTime = upTime > System.currentTimeMillis() + 1000;
        if (!isValidUpTime) {
            findLocation();
        }

        Intent locationRequestIntent = new Intent(this, LocationRequestReceiver.class);
        mLocationUpdateAlarmIntent = PendingIntent.getBroadcast(this, 1, locationRequestIntent, 0);
        getAlarmManager().cancel(mLocationUpdateAlarmIntent);

        if (!isValidUpTime) {
            upTime = System.currentTimeMillis() + settings.getLocationUpdateInterval();
            settings.setLocationUpTime(upTime);

        }

        getAlarmManager().set(AlarmManager.RTC_WAKEUP, upTime, mLocationUpdateAlarmIntent);
    }

    private void findLocation() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackerService");
        mWakeLock.acquire();

        mGPSProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mLockLocationListener = false;
        mGpsLocation = null;
        mLocationManager.removeUpdates(gpsListener);
        mLocationManager.removeUpdates(networkListener);
        if (mGPSProviderEnabled) {
            gpsLocationRequestTime = System.currentTimeMillis();
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, gpsListener);
        }

        if (mNetworkProviderEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 2, networkListener);
        }

        if (!mNetworkProviderEnabled && !mGPSProviderEnabled) {
            mHandler.post(locationNotFoundRunner);
        } else {
            mHandler.postDelayed(locationNotFoundRunner, 50000);
        }
    }


    private LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mLockLocationListener) {
                return;
            }
            mLockLocationListener = true;
            mHandler.removeCallbacks(locationNotFoundRunner);
            mGpsLocation = location;
            mLocationManager.removeUpdates(gpsListener);
            mLocationManager.removeUpdates(networkListener);
            saveLocation(new TrackLocation(location));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    private LocationListener networkListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (mGPSProviderEnabled) {
                if (mGpsLocation == null) {
                    if ((System.currentTimeMillis() - gpsLocationRequestTime) > settings.getNetworkLocationPriorityTimeout()) {
                        if (mLockLocationListener) {
                            return;
                        }
                        mLockLocationListener = true;
                        mLocationManager.removeUpdates(gpsListener);
                        mLocationManager.removeUpdates(networkListener);
                        mHandler.removeCallbacks(locationNotFoundRunner);
                        saveLocation(new TrackLocation(location));
                    }
                }
            } else {
                if (mLockLocationListener) {
                    return;
                }
                mLockLocationListener = true;
                mLocationManager.removeUpdates(networkListener);
                mHandler.removeCallbacks(locationNotFoundRunner);
                saveLocation(new TrackLocation(location));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void saveLocation(TrackLocation t) {
        databaseQueue.save(trackLocationDao, t, null);
        eventHandler.dispatchEvent(TrackerEvent.NEW_LOCATION_ADDED);
    }

    private Runnable locationNotFoundRunner = new Runnable() {
        @Override
        public void run() {
            onLocationNotFound();
        }
    };

    private void onLocationNotFound() {
        mLocationManager.removeUpdates(gpsListener);
        mLocationManager.removeUpdates(networkListener);
    }

    @Override
    public void onDestroy() {
        unregisterAllReceivers();
        Intent restartBroadcast = new Intent("ru.jumatiy.tracker.receiver.RestartServiceReceiver");
        sendBroadcast(restartBroadcast);
        super.onDestroy();
    }


    private void sendLocations() {
        Criteria criteria = dao.createCriteria();
        criteria.addOrder(DBOrder.asc("id"));
        criteria.add(Expression.eq("isSent", false));
        criteria.setMaxResult(10);

        db.getAll(dao, criteria, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                List<TrackLocation> locations = (List<TrackLocation>) obj;
                if (locations.size() > 0) {
                    sendLocations(locations);
                } else {
                    if (turnOffMobileDataRequired) {
                        try {
                            ConnectivityManager dataManager;
                            dataManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                            dataMtd.setAccessible(true);
                            dataMtd.invoke(dataManager, false);
                            turnOffMobileDataRequired = false;
                        } catch (Exception e) {
                            //NOP
                        }
                    }
                }
            }
        });
    }

    private boolean turnOffMobileDataRequired = false;
    private void sendLocations(final List<TrackLocation> locations) {
        hasSuccess = false;
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                try {
                    ConnectivityManager dataManager;
                    dataManager  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    dataMtd.setAccessible(true);
                    dataMtd.invoke(dataManager, true);
                } catch (Exception e) {
                    return;
                }

                turnOffMobileDataRequired = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendLocations(locations);
                    }
                }, 7500);
            } else {
                for (TrackLocation loc : locations) {
                    sendLocation(loc);
                }
            }
        } catch (Exception e) {
            for (TrackLocation loc : locations) {
                sendLocation(loc);
            }
        }
    }

    private Set<Long> processedLocations = new HashSet<Long>(10);
    private boolean hasSuccess = false;
    private void sendLocation(final TrackLocation loc) {
        processedLocations.add(loc.getId());
        api.sendLocation(loc.getLatitude(), loc.getLongitude(), loc.getTime()/1000, loc.getAccuracy(), "devel", new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                hasSuccess = true;
                loc.setIsSent(true);
                db.save(dao, loc, null);
                checkIsLastLocation(loc);
            }

            @Override
            public void failure(RetrofitError error) {
                checkIsLastLocation(loc);
            }
        });
    }

    private void checkIsLastLocation(final TrackLocation loc) {
        processedLocations.remove(loc.getId());
        if (processedLocations.size() == 0 && hasSuccess) {
            sendLocations();
        }
    }


    private void setNextSendAlarm() {
        long upTime = settings.getLocationSendUpTime();
        boolean isExpired = upTime <= System.currentTimeMillis();
        if (isExpired) {
            sendLocations();
        }

        Intent sendLocationIntent = new Intent(this, SendToServerReceiver.class);
        mSendLocationAlarmIntent = PendingIntent.getBroadcast(this, 1, sendLocationIntent, 0);
        getAlarmManager().cancel(mSendLocationAlarmIntent);

        if (isExpired) {
            upTime = System.currentTimeMillis() + settings.getLocationSendInterval();
            settings.setLocationSendUpTime(upTime);
        }

        getAlarmManager().set(AlarmManager.RTC_WAKEUP, upTime, mSendLocationAlarmIntent);
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
