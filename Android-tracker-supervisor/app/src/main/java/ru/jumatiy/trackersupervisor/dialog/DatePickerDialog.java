package ru.jumatiy.trackersupervisor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.activity.MapActivity;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 30.04.2015 1:48.
 */
public class DatePickerDialog extends DialogFragment {

    @InjectView(R.id.datePicker)
    DatePicker datePicker;

    @InjectView(R.id.timePicker)
    TimePicker timePicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        final boolean isStartTimePicker = args.getBoolean("IS_START_TIME_PICKER");
        final Date date = new Date(args.getLong("DATE"));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setTitle(isStartTimePicker ? "Дата и время от:" : "Дата и время до:");

        View layout = View.inflate(getActivity(), R.layout.date_time_picker, null);
        ButterKnife.inject(this, layout);
        if (Build.VERSION.SDK_INT >= 11) {
            datePicker.setCalendarViewShown(false);
        }
        timePicker.setIs24HourView(true);

        builder.setView(layout);

        builder.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                if (getActivity() instanceof MapActivity) {
                    ((MapActivity) getActivity()).onDateTimePicked(cal.getTime(), isStartTimePicker);
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                datePicker.updateDate(year, month, dayOfMonth);
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);

            }
        });


        return dialog;
    }


    public void show(FragmentManager fm, boolean isStartTime, Date date) {
        String tag = "DATE_PICKER_DIALOG";
        Fragment f = fm.findFragmentByTag(tag);
        if (f == null || !f.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("IS_START_TIME_PICKER", isStartTime);
            bundle.putLong("DATE", date.getTime());
            setArguments(bundle);
            show(fm, tag);
        }

    }
}
