package ru.jumatiy.trackersupervisor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ru.jumatiy.trackersupervisor.service.GetTrackService;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 12.04.2015 09:28.
 */
public class RestartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, GetTrackService.class);
        context.startService(serviceIntent);
    }
}
