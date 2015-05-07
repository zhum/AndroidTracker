package ru.jumatiy.trackersupervisor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.adapter.DetectorListAdapter;
import ru.jumatiy.trackersupervisor.db.DetectorPointDao;
import ru.jumatiy.trackersupervisor.dialog.ChangeUrlDialog;
import ru.jumatiy.trackersupervisor.dialog.SilentTimeDialog;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;
import ru.jumatiy.trackersupervisor.util.Settings;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 20:22.
 */
public class SettingsActivity extends BaseActivity {

    @Inject
    DatabaseQueue db;

    @Inject
    DetectorPointDao detectorPointDao;

    @Inject
    Settings settings;


    @InjectView(R.id.listView)
    ListView listView;

    @InjectView(R.id.emptyView)
    View emptyView;

    @InjectView(R.id.silentTime)
    TextView silentTime;

    @InjectView(R.id.url)
    TextView url;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DetectorPoint detectorPoint = (DetectorPoint) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), DetectorPointActivity.class);
                intent.putExtra("ID", detectorPoint.getId());
                startActivity(intent);
            }
        });

        updateUI();
    }

    private void updateUI() {
        int[] time = settings.getSilentTime();
        String hS = time[0] > 9 ? "" + time[0] : "0" + time[0];
        String mS = time[1] > 9 ? "" + time[1] : "0" + time[1];
        String hE = time[2] > 9 ? "" + time[2] : "0" + time[2];
        String mE = time[3] > 9 ? "" + time[3] : "0" + time[3];

        silentTime.setText(hS + ":" + mS + " - " + hE + ":" + mE);
        url.setText(settings.getApiUrl());
    }

    private void refreshList() {
        db.getAll(detectorPointDao, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                final List<DetectorPoint> points = (List<DetectorPoint>) obj;
                emptyView.setVisibility(points.size() > 0 ? View.GONE : View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DetectorListAdapter adapter = new DetectorListAdapter(getApplicationContext(), points);
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    public void silentTimeChanged(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        settings.saveSilentTime(hourStart, minuteStart, hourEnd, minuteEnd);
        updateUI();
    }

    @OnClick(R.id.silentTimeContainer)
    void changeSilentTime() {
        int[] time = settings.getSilentTime();
        new SilentTimeDialog().show(getSupportFragmentManager(), time[0], time[1], time[2], time[3]);
    }

    @OnClick(R.id.urlContainer)
    void changeUrl() {
        new ChangeUrlDialog().show(getSupportFragmentManager(), settings.getApiUrl());
    }

    public void saveNewUrl(String url) {
        settings.saveApiUrl(url);
        updateUI();
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
