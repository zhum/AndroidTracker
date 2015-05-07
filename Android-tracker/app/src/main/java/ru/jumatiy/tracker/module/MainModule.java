package ru.jumatiy.tracker.module;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import ru.jumatiy.tracker.activity.MainActivity;
import ru.jumatiy.tracker.api.Api;
import ru.jumatiy.tracker.api.StringConverter;
import ru.jumatiy.tracker.event.GlobalEventHandler;
import ru.jumatiy.tracker.orm.TrackLocationDao;
import ru.jumatiy.tracker.service.TrackerService;
import ru.jumatiy.tracker.util.Settings;
import uz.droid.orm.DatabaseQueue;

import javax.inject.Singleton;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 11.04.2015 21:02.
 */

@Module(
        library = true,
        injects = {
                TrackerService.class,
                MainActivity.class
        }
)
public class MainModule {

    private Application mApplication;

    public MainModule(Application mApplication) {
        this.mApplication = mApplication;
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(final Settings settings, final Gson gson) {
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
                })
                .setConverter(new StringConverter())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                    }
                }).build();
    }

    @Provides
    @Singleton
    Api provideApi(RestAdapter restAdapter) {
        return restAdapter.create(Api.class);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
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
    DatabaseQueue provideDatabaseQueue() {
        return DatabaseQueue.getInstance();
    }

    @Provides
    @Singleton
    TrackLocationDao provideTrackLocationDao() {
        return new TrackLocationDao(mApplication);
    }

    @Provides
    @Singleton
    GlobalEventHandler provideGlobalEventHandler() {
        return GlobalEventHandler.getInstance();
    }

}
