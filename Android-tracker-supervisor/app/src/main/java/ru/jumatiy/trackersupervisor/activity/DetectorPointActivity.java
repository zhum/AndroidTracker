package ru.jumatiy.trackersupervisor.activity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.db.DetectorPointDao;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;

import javax.inject.Inject;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 21:35.
 */
public class DetectorPointActivity extends BaseActivity {

    @Inject
    DatabaseQueue db;

    @Inject
    DetectorPointDao detectorPointDao;

    @InjectView(R.id.nameInput)
    EditText nameInput;

    @InjectView(R.id.latitudeInput)
    EditText latitudeInput;

    @InjectView(R.id.longitudeInput)
    EditText longitudeInput;

    @InjectView(R.id.radiusInput)
    EditText radiusInput;

    @InjectView(R.id.notificationSound)
    TextView notificationSound;

    private DetectorPoint detectorPoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detector_point_activity);

        Long id = getIntent().getLongExtra("ID", 0);
        db.get(detectorPointDao, id, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                if (obj != null) {
                    detectorPoint = (DetectorPoint) obj;
                    updateUI();
                }
            }
        });

    }

    private void updateUI() {
        if (detectorPoint == null) {
            return;
        }
        setTitle(detectorPoint.getName());
        nameInput.setText(detectorPoint.getName());
        latitudeInput.setText(detectorPoint.getLatitude().toString());
        longitudeInput.setText(detectorPoint.getLongitude().toString());
        radiusInput.setText(detectorPoint.getRadius().toString());

        if (detectorPoint.getNotification() != null) {
            notificationSound.setText(detectorPoint.getNotification());
        } else {
            notificationSound.setText("По умолчанию");
        }
    }


    @OnClick(R.id.buttonDelete)
    void delete() {
        db.delete(detectorPointDao, detectorPoint.getId(), new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                finish();
            }
        });
    }

    @OnClick(R.id.buttonSave)
    void save() {

        try {
            detectorPoint.setName(nameInput.getText().toString());
            detectorPoint.setLatitude(Double.parseDouble(latitudeInput.getText().toString()));
            detectorPoint.setLongitude(Double.parseDouble(longitudeInput.getText().toString()));
            detectorPoint.setRadius(Integer.parseInt(radiusInput.getText().toString()));
            if (mRingtone != null && mRingtoneName != null) {
                detectorPoint.setNotification(mRingtoneName);
                detectorPoint.setNotificationUri(mRingtone);
            }
            db.save(detectorPointDao, detectorPoint, new DBCallback() {
                @Override
                public void onComplete(Object obj) {
                    finish();
                }
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.notificationSound)
    void pickSound() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выбрать звук уведомления");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, 5);
    }


    private String mRingtone;
    private String mRingtoneName;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && resultCode == RESULT_OK) {

            try {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null)
                {
                    mRingtone = uri.toString();
                    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                    mRingtoneName = ringtone.getTitle(this);
                    notificationSound.setText(mRingtoneName);
                }
            } catch (Exception e) {
            }
        }
    }
}
