package ru.jumatiy.trackersupervisor.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import ru.jumatiy.trackersupervisor.R;
import ru.jumatiy.trackersupervisor.api.Api;
import ru.jumatiy.trackersupervisor.db.DetectorPointDao;
import ru.jumatiy.trackersupervisor.db.TrackLocationDao;
import ru.jumatiy.trackersupervisor.dialog.AddDetectorDialog;
import ru.jumatiy.trackersupervisor.dialog.DatePickerDialog;
import ru.jumatiy.trackersupervisor.event.Event;
import ru.jumatiy.trackersupervisor.event.TrackerEvent;
import ru.jumatiy.trackersupervisor.model.DetectorPoint;
import ru.jumatiy.trackersupervisor.model.TrackLocation;
import ru.jumatiy.trackersupervisor.service.GetTrackService;
import uz.droid.orm.DBCallback;
import uz.droid.orm.DatabaseQueue;
import uz.droid.orm.criteria.Criteria;
import uz.droid.orm.criteria.DBOrder;
import uz.droid.orm.criteria.Expression;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 26.04.2015 19:52.
 */
public class MapActivity extends BaseActivity {

    private GoogleMap map;

    @Inject
    DatabaseQueue db;

    @Inject
    TrackLocationDao trackLocationDao;

    @Inject
    DetectorPointDao detectorPointDao;

    @Inject
    Api api;

    @InjectView(R.id.root_view)
    View rootView;

    @InjectView(R.id.start_time)
    TextView startTimeText;

    @InjectView(R.id.end_time)
    TextView endTimeText;

    private Date startTime;
    private Date endTime;

    private LatLngBounds bounds = null;
    private boolean mapViewReady = false;

    private Handler handler = new Handler();

    private boolean isFirstTrackDraw = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_activity);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mapViewReady = true;
                if (bounds != null) {
                    fitToBounds(bounds);
                }
            }
        });

//        Criteria c = detectorPointDao.createCriteria();
//        db.deleteAll(detectorPointDao, c, null);

        startService(new Intent(getApplicationContext(), GetTrackService.class));

        initTime();
        initMap();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();

        if (map != null) {
            UiSettings mapSettings = map.getUiSettings();
            mapSettings.setCompassEnabled(false);
            mapSettings.setMyLocationButtonEnabled(true);
            mapSettings.setZoomControlsEnabled(true);
            mapSettings.setScrollGesturesEnabled(true);
            mapSettings.setZoomGesturesEnabled(true);
            mapSettings.setTiltGesturesEnabled(false);
            mapSettings.setRotateGesturesEnabled(false);

            map.setMyLocationEnabled(true);


            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    new AddDetectorDialog().show(getSupportFragmentManager(), latLng);
                }
            });

            addEventListeners(TrackerEvent.TRACK_UPDATED);
            drawDetectors();
            updateTrack();
        }
    }


    private boolean noMoveMap = false;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event.getId() == TrackerEvent.TRACK_UPDATED) {
            noMoveMap = true;
            updateTrack();
        }
    }

    public void addDetector(DetectorPoint dp) {
        db.save(detectorPointDao, dp, null);
        drawDetectors();
    }

    private List<Circle> circles = new ArrayList<>();

    private void drawDetectors() {
        if (map == null) {
            return;
        }
        db.getAll(detectorPointDao, new DBCallback() {
            @Override
            public void onComplete(final Object obj) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Circle circle : circles) {
                            circle.remove();
                        }

                        circles = new ArrayList<>();

                        List<DetectorPoint> points = (List<DetectorPoint>) obj;
                        for (DetectorPoint point : points) {
                            LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
                            CircleOptions circleOptions = new CircleOptions().center(position)
                                    .radius(point.getRadius())
                                    .fillColor(SHADE_COLOR)
                                    .strokeColor(STROKE_COLOR)
                                    .strokeWidth(STROKE_WIDTH);
                            Circle circle = map.addCircle(circleOptions);
                            circles.add(circle);
                        }
                    }
                });

            }
        });
    }

    public static final int STROKE_COLOR = 0xffff0000; //red outline
    public static final int SHADE_COLOR = 0x44ff0000; //opaque red fill
    public static final float STROKE_WIDTH = 4; //opaque red fill

    private void initTime() {
        Calendar cal = Calendar.getInstance();
        endTime = cal.getTime();

        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        startTime = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd.MM.yyyy");
        startTimeText.setText(sdf.format(startTime));
        endTimeText.setText(sdf.format(endTime));
    }

    @OnClick({R.id.start_time, R.id.end_time})
    void pickTime(View v) {
        boolean isStartTime = v.getId() == R.id.start_time;
        Date val = isStartTime ? startTime : endTime;
        new DatePickerDialog().show(getSupportFragmentManager(), isStartTime, val);
    }

    @OnClick(R.id.buttonSettings)
    void openSettings() {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

    public void onDateTimePicked(Date val, boolean isStartTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd.MM.yyyy");

        if (isStartTime) {
            startTime = val;
            startTimeText.setText(sdf.format(startTime));
        } else {
            endTime = val;
            endTimeText.setText(sdf.format(endTime));
        }

        noMoveMap = false;
        updateTrack();

    }


    private void updateTrack() {
        long start = startTime.getTime() / 1000;
        long end = endTime.getTime() / 1000;

        Criteria c = trackLocationDao.createCriteria();
        c.add(Expression.greateOrEq("time", start));
        c.add(Expression.lessOrEq("time", end));
        c.addOrder(DBOrder.desc("time"));

        db.getAll(trackLocationDao, c, new DBCallback() {
            @Override
            public void onComplete(Object obj) {
                final List<TrackLocation> list = (List<TrackLocation>) obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawTrack(list);
                    }
                });
            }
        });


    }


    private Polyline polyline;
    private Marker marker;

    private void drawTrack(List<TrackLocation> trackLocations) {
        if (map == null) {
            return;
        }

        List<LatLng> points = new ArrayList<>(1000);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int inc = (int) Math.ceil(trackLocations.size() / 500d);

        for (int i = 0; i < trackLocations.size(); i += inc) {
            TrackLocation t = trackLocations.get(i);

            if (t == null || t.getTime() == null) {
                continue;
            }

            LatLng latLng = new LatLng(t.getLatitude(), t.getLongitude());
            points.add(latLng);
            builder.include(latLng);
        }


        LatLng[] pArr = points.toArray(new LatLng[points.size()]);
        if (polyline != null) {
            polyline.remove();
        }

        polyline = map.addPolyline((new PolylineOptions())
                        .add(pArr)
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)
        );

        if (!noMoveMap) {
            if (points.size() > 1) {
                bounds = builder.build();
                if (mapViewReady) {
                    fitToBounds(bounds);
                }
            } else if (points.size() == 1) {
                if (isFirstTrackDraw) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 12f));
                    isFirstTrackDraw = false;
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 12f));
                }
            }
        }

        if (points.size() > 0) {
            if (marker != null) {
                marker.remove();
            }
            marker = map.addMarker(new MarkerOptions().position(points.get(0)));
        }
    }

    private void fitToBounds(LatLngBounds bounds) {
        if (map == null) {
            return;
        }

        if (isFirstTrackDraw) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            isFirstTrackDraw = false;
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawDetectors();
    }
}
