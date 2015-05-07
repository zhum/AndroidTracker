package ru.jumatiy.trackersupervisor.application;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import dagger.ObjectGraph;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.activity.MapActivity;
import ru.jumatiy.trackersupervisor.event.GlobalEventHandler;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;
import ru.jumatiy.trackersupervisor.model.TrackLocation;
import ru.jumatiy.trackersupervisor.module.MainModule;
import uz.droid.orm.DatabaseQueue;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 21:59.
 */
public class TrackerApplication extends Application {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(new MainModule(this));

        GlobalEventHandler.initInstance();
        DatabaseQueue.initInstance();
    }

    public void inject(Object object) {
        try {
            graph.inject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showNotification(DetectorPoint point, TrackLocation location, boolean isOut) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String ticker = point.getName();
        String content = isOut ? String.format("Объект покидал зону %s", point.getName()) :
                String.format("Объект внутри зоны %s", point.getName());

//        String content = String.format("Координаты: шир. %.4f; дол. %.4f; точность %d м",
//                location.getLatitude(), location.getLongitude(),
//                location.getAccuracy().intValue());

        NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_stat_image_filter_tilt_shift)
                .setAutoCancel(true)
                .setTicker(ticker)
                .setContentText(content)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
//                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.push))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        if (point.getNotificationUri() != null) {
            try {
                nb.setSound(Uri.parse(point.getNotificationUri()))
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
            } catch (Exception e) {
            }
        }

        Notification notification = nb.build();
        mNotificationManager.notify(location.getTime().hashCode(), notification);
    }

}
