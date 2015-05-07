package ru.jumatiy.trackersupervisor.module;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import ru.jumatiy.trackersupervisor.activity.BaseActivity;
import ru.jumatiy.trackersupervisor.activity.DetectorPointActivity;
import ru.jumatiy.trackersupervisor.activity.MapActivity;
import ru.jumatiy.trackersupervisor.activity.SettingsActivity;
import ru.jumatiy.trackersupervisor.api.Api;
import ru.jumatiy.trackersupervisor.application.TrackerApplication;
import ru.jumatiy.trackersupervisor.db.DetectorPointDao;
import ru.jumatiy.trackersupervisor.db.TrackLocationDao;
import ru.jumatiy.trackersupervisor.event.GlobalEventHandler;
import ru.jumatiy.trackersupervisor.service.GetTrackService;
import ru.jumatiy.trackersupervisor.util.Settings;
import uz.droid.orm.DatabaseQueue;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 20:40.
 */

@Module(
        library = true,
        injects = {
                MapActivity.class,
                GetTrackService.class,
                SettingsActivity.class,
                DetectorPointActivity.class
        }
)
public class MainModule {

    private TrackerApplication application;

    public MainModule(TrackerApplication application) {
        this.application = application;
    }


    @Provides
    @Singleton
    RestAdapter provideRestAdapter(final Gson gson, final Settings settings) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);

        return new RestAdapter.Builder()
                .setEndpoint(new Endpoint() {
                    @Override
                    public String getUrl() {
                        return settings.getApiUrl();
                    }

                    @Override
                    public String getName() {
                        return "server";
                    }
                }).setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson)).build();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    @Singleton
    Api provideApi(RestAdapter adapter) {
        return adapter.create(Api.class);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    DatabaseQueue provideDatabaseQueue() {
        return DatabaseQueue.getInstance();
    }

    @Provides
    @Singleton
    TrackLocationDao provideTrackLocationDao() {
        return new TrackLocationDao(application);
    }

    @Provides
    @Singleton
    DetectorPointDao provideDetectorPointDao() {
        return new DetectorPointDao(application);
    }

    @Provides
    AlarmManager provideAlarmManager() {
        return (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
    }


    @Provides
    @Singleton
    GlobalEventHandler provideGlobalEventHandler() {
        return GlobalEventHandler.getInstance();
    }
}
