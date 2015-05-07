package ru.jumatiy.trackersupervisor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.gms.maps.model.LatLng;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.activity.MapActivity;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 9:43.
 */
public class AddDetectorDialog extends DialogFragment {

    @InjectView(R.id.nameInput)
    EditText nameInput;

    @InjectView(R.id.latitudeInput)
    EditText latitudeInput;

    @InjectView(R.id.longitudeInput)
    EditText longitudeInput;

    @InjectView(R.id.radiusInput)
    EditText radiusInput;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Новая точка");
        View content = View.inflate(getActivity(), R.layout.add_detector, null);
        ButterKnife.inject(this, content);

        builder.setView(content);

        Bundle args = getArguments();

        final double latitude = args.getDouble("LATITUDE", 0);
        final double longitude = args.getDouble("LONGITUDE", 0);

        builder.setNeutralButton("Отмена", null);
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Double latitude = Double.parseDouble(latitudeInput.getText().toString());
                Double longitude = Double.parseDouble(longitudeInput.getText().toString());

                Integer radius = Integer.parseInt(radiusInput.getText().toString());
                String name = nameInput.getText().toString();

                DetectorPoint detectorPoint = new DetectorPoint(name, latitude, longitude, radius);

                if (getActivity() instanceof MapActivity) {
                    ((MapActivity)getActivity()).addDetector(detectorPoint);
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                latitudeInput.setText(String.valueOf(latitude));
                longitudeInput.setText(String.valueOf(longitude));
            }
        });

        return dialog;
    }

    public void show(FragmentManager fm, LatLng latLng) {
        String tag = "ADD_DETECTOR_DIALOG";
        Fragment f = fm.findFragmentByTag(tag);
        if (f == null || !f.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putDouble("LATITUDE", latLng.latitude);
            bundle.putDouble("LONGITUDE", latLng.longitude);
            setArguments(bundle);
            show(fm, tag);
        }
    }
}
