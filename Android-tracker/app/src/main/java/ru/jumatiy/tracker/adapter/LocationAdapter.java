package ru.jumatiy.tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.tracker.R;
import ru.jumatiy.tracker.model.TrackLocation;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 12.04.2015 11:44.
 */
public class LocationAdapter extends ArrayAdapter<TrackLocation> {

    public LocationAdapter(Context context, List<TrackLocation> objects) {
        super(context, 0, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            new ViewHolder(convertView);
        }

        ViewHolder h = (ViewHolder) convertView.getTag();
        TrackLocation t = getItem(position);
        h.id.setText(String.valueOf(t.getId()));
        h.latitude.setText(new DecimalFormat("#.####").format(t.getLatitude()));
        h.longitude.setText(new DecimalFormat("#.####").format(t.getLongitude()));
        h.accuracy.setText(new DecimalFormat("#.#").format(t.getAccuracy()));
        h.provider.setText(t.getProvider());
        h.time.setText(timeFormat(t.getTime()));

        return convertView;
    }

    private String timeFormat(long ms) {
        String time = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            time = sdf.format(new Date(ms));
        } catch (Exception e) {
        }

        return time;
    }

    public static class ViewHolder {

        @InjectView(R.id.id)
        TextView id;

        @InjectView(R.id.latitude)
        TextView latitude;

        @InjectView(R.id.longitude)
        TextView longitude;

        @InjectView(R.id.accuracy)
        TextView accuracy;

        @InjectView(R.id.provider)
        TextView provider;

        @InjectView(R.id.time)
        TextView time;



        public ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
            convertView.setTag(this);
        }
    }


}
