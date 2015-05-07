package ru.jumatiy.trackersupervisor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;

import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 20:38.
 */
public class DetectorListAdapter extends ArrayAdapter<DetectorPoint> {

    public DetectorListAdapter(Context context, List<DetectorPoint> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.detector_list_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();
        DetectorPoint point = getItem(position);
        vh.name.setText(point.getName());
        vh.latLng.setText(point.toString());

        return convertView;
    }


    class ViewHolder {

        @InjectView(R.id.name)
        TextView name;

        @InjectView(R.id.latLng)
        TextView latLng;

        public ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
            convertView.setTag(this);
        }
    }
}
