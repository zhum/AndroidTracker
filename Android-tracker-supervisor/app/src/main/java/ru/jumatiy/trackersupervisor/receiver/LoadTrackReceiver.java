package ru.jumatiy.trackersupervisor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ru.jumatiy.trackersupervisor.service.GetTrackService;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 11:23.
 */
public class LoadTrackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(GetTrackService.GET_TRACKS_INTENT));
    }
}
