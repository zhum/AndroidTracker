package ru.jumatiy.trackersupervisor.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import ru.jumatiy.trackersupervisor.application.TrackerApplication;
import ru.jumatiy.trackersupervisor.event.Event;
import ru.jumatiy.trackersupervisor.event.EventListener;
import ru.jumatiy.trackersupervisor.event.GlobalEventHandler;

import javax.inject.Inject;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 20:38.
 */
public class BaseActivity extends FragmentActivity implements EventListener {

    @Inject
    GlobalEventHandler globalEventHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TrackerApplication) getApplication()).inject(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.inject(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.inject(this);
    }

    @Override
    public void onEvent(Event event) {
    }

    protected void addEventListeners(int... eventIds) {
        globalEventHandler.addEventListeners(this, eventIds);
    }

    protected void removeEventListeners() {
        globalEventHandler.removeAllOccurency(this);
    }

    @Override
    protected void onDestroy() {
        removeEventListeners();
        super.onDestroy();
    }
}
