package ru.jumatiy.tracker.application;

import android.app.Application;
import dagger.ObjectGraph;
import ru.jumatiy.tracker.event.GlobalEventHandler;
import ru.jumatiy.tracker.module.MainModule;
import uz.droid.orm.DatabaseQueue;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 12.04.2015 8:39.
 */
public class TrackerApplication extends Application {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalEventHandler.initInstance();
        DatabaseQueue.initInstance();

        graph = ObjectGraph.create(new MainModule(this));
    }

    public void inject(Object object) {
        graph.inject(object);
    }

}
