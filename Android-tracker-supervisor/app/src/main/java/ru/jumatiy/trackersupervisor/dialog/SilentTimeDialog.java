package ru.jumatiy.trackersupervisor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.activity.MapActivity;
import ru.jumatiy.trackersupervisor.activity.SettingsActivity;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 30.04.2015 1:48.
 */
public class SilentTimeDialog extends DialogFragment {

    @InjectView(R.id.timePickerMin)
    TimePicker timePickerMin;

    @InjectView(R.id.timePickerMax)
    TimePicker timePickerMax;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {




        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Тихий период:");

        View layout = View.inflate(getActivity(), R.layout.silent_time_picker, null);
        ButterKnife.inject(this, layout);
        timePickerMin.setIs24HourView(true);
        timePickerMax.setIs24HourView(true);

        builder.setView(layout);
        builder.setNeutralButton("Отмена", null);
        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() instanceof SettingsActivity) {
                    int hourStart = timePickerMin.getCurrentHour();
                    int minuteStart = timePickerMin.getCurrentMinute();

                    int hourEnd = timePickerMax.getCurrentHour();
                    int minuteEnd = timePickerMax.getCurrentMinute();

                    ((SettingsActivity) getActivity()).silentTimeChanged(hourStart, minuteStart, hourEnd, minuteEnd);
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Bundle args = getArguments();
                int hourStart  = args.getInt("SILENT_HOUR_START");
                int minuteStart  = args.getInt("SILENT_MINUTE_START");

                int hourEnd  = args.getInt("SILENT_HOUR_END");
                int minuteEnd  = args.getInt("SILENT_MINUTE_END");

                timePickerMin.setCurrentHour(hourStart);
                timePickerMin.setCurrentMinute(minuteStart);

                timePickerMax.setCurrentHour(hourEnd);
                timePickerMax.setCurrentMinute(minuteEnd);

            }
        });


        return dialog;
    }


    public void show(FragmentManager fm, int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        String tag = "SILENT_TIME_PICKER_DIALOG";
        Fragment f = fm.findFragmentByTag(tag);
        if (f == null || !f.isAdded()) {
            Bundle args = new Bundle();

            args.putInt("SILENT_HOUR_START", hourStart);
            args.putInt("SILENT_MINUTE_START", minuteStart);

            args.putInt("SILENT_HOUR_END", hourEnd);
            args.putInt("SILENT_MINUTE_END", minuteEnd);

            setArguments(args);
            show(fm, tag);
        }

    }
}
