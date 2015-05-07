package ru.jumatiy.tracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ru.jumatiy.tracker.service.TrackerService;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 12.04.2015 09:28.
 */
public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, TrackerService.class);
        context.startService(serviceIntent);
    }
}
