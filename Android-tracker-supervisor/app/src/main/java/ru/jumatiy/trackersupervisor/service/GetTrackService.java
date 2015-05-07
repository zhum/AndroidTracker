package ru.jumatiy.trackersupervisor.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.jumatiy.trackersupervisor.api.Api;
import ru.jumatiy.trackersupervisor.application.TrackerApplication;
import ru.jumatiy.trackersupervisor.db.DetectorPointDao;
import ru.jumatiy.trackersupervisor.db.TrackLocationDao;
import ru.jumatiy.trackersupervisor.event.GlobalEventHandler;
import ru.jumatiy.trackersupervisor.event.TrackerEvent;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;
import ru.jumatiy.trackersupervisor.model.TrackLocation;
import ru.jumatiy.trackersupervisor.receiver.LoadTrackReceiver;
import ru.jumatiy.trackersupervisor.util.Settings;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;
import uz.droid.orm.criteria.Criteria;
import uz.droid.orm.criteria.DBOrder;
import uz.droid.orm.criteria.Expression;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 10:59.
 */
public class GetTrackService extends Service {

    public static final String GET_TRACKS_INTENT = "ru.jumatiy.trackersupervisor.GET_TRACKS_INTENT";


    @Inject
    AlarmManager alarmManager;

    @Inject
    Settings settings;

    @Inject
    Api api;

    @Inject
    DatabaseQueue db;

    @Inject
    TrackLocationDao trackLocationDao;

    @Inject
    DetectorPointDao detectorPointDao;

    @Inject
    GlobalEventHandler globalEventHandler;

    private PendingIntent mLoadTrackAlarmIntent;
    private BroadcastReceiver mLoadTrackBroadcastReceiver;

    private Long startTime;


    @Override
    public void onCreate() {
        super.onCreate();
        ((TrackerApplication) getApplication()).inject(this);

        IntentFilter filter = new IntentFilter(GET_TRACKS_INTENT);
        mLoadTrackBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setNextLoadAlarm();
            }
        };

        registerReceiver(mLoadTrackBroadcastReceiver, filter);
        setNextLoadAlarm();
    }

    private void setNextLoadAlarm() {
        long upTime = settings.getNextLoadUpTime();
        boolean isExpired = upTime <= System.currentTimeMillis();
        if (isExpired) {
            loadTracks();
        }

        Intent loadTracksIntent = new Intent(this, LoadTrackReceiver.class);
        mLoadTrackAlarmIntent = PendingIntent.getBroadcast(this, 1, loadTracksIntent, 0);
        alarmManager.cancel(mLoadTrackAlarmIntent);

        if (isExpired) {
            upTime = System.currentTimeMillis() + settings.getLoadTrackInterval();
            settings.setNextLoadUpTime(upTime);
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, upTime, mLoadTrackAlarmIntent);
    }

    private void loadTracks() {
        if (startTime == null) {
            Criteria c = trackLocationDao.createCriteria();
            c.addOrder(DBOrder.desc("time"));
            c.setMaxResult(1);
            db.getAll(trackLocationDao, c, new DBCallback() {
                @Override
                public void onComplete(Object obj) {
                    List<TrackLocation> list = (List<TrackLocation>)obj;
                    if (list.size() > 0) {
                        startTime = list.get(0).getTime();
                    }

                    if (startTime == null) {
                        startTime = Long.MIN_VALUE;
                    }

                    long currentSeconds = System.currentTimeMillis() / 1000;
                    long threeDaysAgo = currentSeconds - 3 * 24 * 3600;
                    if ( threeDaysAgo > startTime) {
                        startTime = threeDaysAgo;
                    }

                    loadTracks();
                }
            });
        } else {
            db.getAll(detectorPointDao, new DBCallback() {
                @Override
                public void onComplete(Object obj) {
                    detectorPoints = (List<DetectorPoint>) obj;
                }
            });
            api.getLocations(startTime, null, 500, new Callback<List<TrackLocation>>() {
                @Override
                public void success(List<TrackLocation> trackLocations, Response response) {
                        saveLocations(trackLocations);
                }

                @Override
                public void failure(RetrofitError error) {
                }
            });
        }
    }

    private List<DetectorPoint> detectorPoints = new ArrayList<>();

    private boolean needNextLoad = false;
    private void saveLocations(final List<TrackLocation> locations) {


        db.getAll(detectorPointDao, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                detectorPoints = (List<DetectorPoint>) obj;
            }
        });

        for (TrackLocation loc : locations) {
            if (loc.isValid()) {
                loc.setId(loc.getTime());
                db.save(trackLocationDao, loc, null);
            }
        }

        if (locations.size() > 0) {
            globalEventHandler.dispatchEvent(TrackerEvent.TRACK_UPDATED);
        }

        // update startTime
        Criteria c = trackLocationDao.createCriteria();
        c.addOrder(DBOrder.desc("time"));
        c.setMaxResult(1);
        db.getAll(trackLocationDao, c, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                List<TrackLocation> list = (List<TrackLocation>)obj;
                if (list.size() > 0) {
                    startTime = list.get(0).getTime();
                }

                if (startTime == null) {
                    startTime = Long.MIN_VALUE;
                }

                long currentSeconds = System.currentTimeMillis() / 1000;
                long threeDaysAgo = currentSeconds - 3 * 24 * 3600;
                if ( threeDaysAgo > startTime) {
                    startTime = threeDaysAgo;
                }
            }
        });

        int[] time = settings.getSilentTime();
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        int minutesOfDay = hour * 60 + minute;
        int minutesOfDayMin = time[0] * 60 * time[1];
        int minutesOfDayMax = time[2] * 60 * time[3];

        boolean isSilentTime;
        if (minutesOfDayMin > minutesOfDayMax) {
            isSilentTime = minutesOfDay > minutesOfDayMin || minutesOfDay < minutesOfDayMax;
        } else {
            isSilentTime = minutesOfDay > minutesOfDayMin && minutesOfDay < minutesOfDayMax;
        }


        if (locations.size() > 0 && !isSilentTime) {
            long lastNotifiedTrackTime = settings.getLastNotificationTrackTime();
            List<TrackLocation> locList = new ArrayList<TrackLocation>();
            for (TrackLocation location : locations) {
                if (location.isValid() && location.getTime() > lastNotifiedTrackTime) {
                    locList.add(location);
                }
            }
            hitTestLocations(locList);
        }
    }

    private void hitTestLocations(List<TrackLocation> locations) {
        long lastNotifiedTime = settings.getLastNotificationTrackTime();
        long newNotifiedTime = lastNotifiedTime;
        if (detectorPoints != null && detectorPoints.size() > 0) {
            for (TrackLocation location : locations) {
                for (DetectorPoint detectorPoint : detectorPoints) {
                    if (detectorPoint.hitTest(location)) {
                        if (!detectorPoint.isHasInside()) {
                            newNotifiedTime = Math.max(newNotifiedTime, lastNotifiedTime);
                            notifyDetection(detectorPoint, location, false);
                            detectorPoint.setHasInside(true);
                            db.save(detectorPointDao, detectorPoint, null);
                        }
                    } else if (detectorPoint.isHasInside()) {
                        newNotifiedTime = Math.max(newNotifiedTime, lastNotifiedTime);
                        notifyDetection(detectorPoint, location, true);
                        detectorPoint.setHasInside(false);
                        db.save(detectorPointDao, detectorPoint, null);
                    }
                }
            }
        }

        if (newNotifiedTime > lastNotifiedTime) {
            settings.saveLastNotificationTrackTime(newNotifiedTime);
        }
    }

    private void notifyDetection(DetectorPoint detectorPoint, TrackLocation location, boolean isOut) {
        ((TrackerApplication) getApplication()).showNotification(detectorPoint, location, isOut);
    }

    @Override
    public void onDestroy() {
        unregisterAllReceivers();
        Intent restartBroadcast = new Intent("ru.jumatiy.trackersupervisor.receiver.RestartServiceReceiver");
        sendBroadcast(restartBroadcast);
        super.onDestroy();
    }

    private void unregisterAllReceivers() {
        if (mLoadTrackBroadcastReceiver != null) {
            unregisterReceiver(mLoadTrackBroadcastReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
