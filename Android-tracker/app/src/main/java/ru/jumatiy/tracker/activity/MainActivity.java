package ru.jumatiy.tracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.tracker.R;
import ru.jumatiy.tracker.adapter.LocationAdapter;
import ru.jumatiy.tracker.application.TrackerApplication;
import ru.jumatiy.tracker.dialog.ChangeUrlDialog;
import ru.jumatiy.tracker.event.Event;
import ru.jumatiy.tracker.event.EventListener;
import ru.jumatiy.tracker.event.GlobalEventHandler;
import ru.jumatiy.tracker.event.TrackerEvent;
import ru.jumatiy.tracker.model.TrackLocation;
import ru.jumatiy.tracker.orm.TrackLocationDao;
import ru.jumatiy.tracker.service.TrackerService;
import ru.jumatiy.tracker.util.Settings;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;
import uz.droid.orm.criteria.Criteria;
import uz.droid.orm.criteria.DBOrder;

import javax.inject.Inject;
import java.util.List;


/**
 * Created by Sarimsakov Bakhrom Azimovich on 12.04.2015 10:29.
 */
public class MainActivity extends FragmentActivity implements EventListener {

    @InjectView(R.id.list_view)
    ListView listView;

    @InjectView(R.id.empty)
    View emptyView;

    @Inject
    DatabaseQueue db;

    @Inject
    TrackLocationDao dao;

    @Inject
    GlobalEventHandler eventHandler;

    @Inject
    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TrackerApplication) getApplication()).inject(this);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        listView.setEmptyView(emptyView);

        eventHandler.addEventListeners(this, TrackerEvent.NEW_LOCATION_ADDED);
        startService(new Intent(getApplicationContext(), TrackerService.class));

        refreshList();
    }

    @Override
    public void onEvent(Event event) {
        if (event.getId() == TrackerEvent.NEW_LOCATION_ADDED) {
            refreshList();
        }
    }


    private void refreshList() {
        Criteria criteria = dao.createCriteria();
        criteria.setMaxResult(500);
        criteria.addOrder(DBOrder.desc("id"));
        db.getAll(dao, criteria, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                final List<TrackLocation> locations = (List<TrackLocation>)obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(new LocationAdapter(getApplicationContext(), locations));
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, Menu.FIRST, 0, "Настройки").setIcon(R.drawable.ic_action_action_settings);
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            new ChangeUrlDialog().show(getSupportFragmentManager(), settings.getApiUrl());
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveNewUrl(String url) {
        settings.saveApiUrl(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventHandler.removeAllOccurency(this);
    }
}
