package com.fudi.fudi.front;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fudi.fudi.R;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.GeoArea;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickActivity extends FragmentActivity implements OnMapReadyCallback {

    private LinearLayout buttonHolder;
    private FrameLayout mapHolder;
    private MapFragment mapFragment;
    private Button submit;

    private boolean clickedChoose;

    private Location zoom;
    private LatLng myLocation;
    private GeoArea geoArea;
    private Marker areaMarker;
    private Circle mapCircle;



    public static int HERE_SELECTED = 200;
    public static int GLOBAL_SELECTED = 201;
    public static int CHOOSE_SELECTED = 202;
    public static String SELECTED_GEO_AREA = "GeoArea Picked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_pick);

        buttonHolder = (LinearLayout) findViewById(R.id.locset_button_holder);
        mapHolder = (FrameLayout) findViewById(R.id.locset_map_holder);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.locset_map);

        ImageButton hereButton = (ImageButton) findViewById(R.id.locset_here_button);
        hereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(HERE_SELECTED);
                finish();
            }
        });

        ImageButton globalButton = (ImageButton) findViewById(R.id.locset_global_button);
        globalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(GLOBAL_SELECTED);
                finish();
            }
        });

        ImageButton chooseButton = (ImageButton) findViewById(R.id.locset_choose_button);

        mapFragment.getMapAsync(this);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do not set result just yet
                buttonHolder.setVisibility(View.GONE);
                mapHolder.setVisibility(View.VISIBLE);
                clickedChoose = true;
                //Log.i("Clicked Chosen", "Clicked Chosen");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.fudi_action_back){
            if(clickedChoose){
                clickedChoose = false;
                mapHolder.setVisibility(View.GONE);
                buttonHolder.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //this.map = googleMap;

        submit = (Button) findViewById(R.id.set_location_submit);
        submit.setVisibility(View.VISIBLE);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmLocation = new AlertDialog.Builder(LocationPickActivity.this);
                confirmLocation.setCancelable(true);
                confirmLocation.setIcon(R.drawable.fudi_icon);
                confirmLocation.setMessage(R.string.confirm_change_message);
                confirmLocation.setTitle(R.string.confirm_change_title);
                confirmLocation.setPositiveButton(R.string.remove_circle_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent result = new Intent();

                        if (geoArea != null) {
                            result.putExtra(SELECTED_GEO_AREA, geoArea);
                            setResult(CHOOSE_SELECTED, result);
                            finish();
                        }
                    }
                });
                confirmLocation.setNegativeButton(R.string.remove_circle_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                confirmLocation.create();
                confirmLocation.show();
            }
        });
        googleMap.getUiSettings().setAllGesturesEnabled(true);


        zoom = FudiApp.getInstance().FudiLocationManager(getApplicationContext()).
                    getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        myLocation = new LatLng(zoom.getLatitude(), zoom.getLongitude());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                if (mapCircle != null) {
                    AlertDialog.Builder removeCircle = new
                            AlertDialog.Builder(LocationPickActivity.this);

                    removeCircle.setMessage(R.string.remove_circle_warning);
                    removeCircle.setIcon(R.drawable.fudi_icon);
                    removeCircle.setTitle(R.string.remove_circle_title);

                    removeCircle.setNegativeButton(R.string.remove_circle_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    removeCircle.setPositiveButton(R.string.remove_circle_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mapCircle.remove();
                            areaMarker.remove();

                            geoArea = new GeoArea("Current", latLng.latitude,
                                    latLng.longitude, 10000);

                            areaMarker = googleMap.addMarker(new MarkerOptions()
                                            .draggable(true)
                                            .position(latLng)
                                            .visible(true)
                            );

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                            mapCircle = googleMap.addCircle(new CircleOptions()
                                            .fillColor(R.color.fudi_text_color)
                                            .strokeColor(Color.TRANSPARENT)
                                            .radius(10000)
                                            .center(latLng)
                            );
                        }
                    });
                    removeCircle.create();
                    removeCircle.show();
                } else {
                    geoArea = new GeoArea("Current", latLng.latitude,
                            latLng.longitude, 10000);

                    areaMarker = googleMap.addMarker(new MarkerOptions()
                                    .draggable(true)
                                    .position(latLng)
                                    .visible(true)
                    );

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                    mapCircle = googleMap.addCircle(new CircleOptions()
                                    .fillColor(R.color.fudi_text_color)
                                    .strokeColor(Color.TRANSPARENT)
                                    .radius(10000)
                                    .center(latLng)
                    );
                }
            }
        });

       googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
           @Override
           public void onMarkerDragStart(Marker marker) {
               mapCircle.setVisible(false);
           }

           @Override
           public void onMarkerDrag(Marker marker) {

           }

           @Override
           public void onMarkerDragEnd(Marker marker) {
               mapCircle.remove();

               geoArea = new GeoArea("Current", marker.getPosition().latitude,
                       marker.getPosition().longitude, 10000);

               googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 11));


               mapCircle = googleMap.addCircle(new CircleOptions()
                               .fillColor(R.color.fudi_text_color)
                               .strokeColor(Color.TRANSPARENT)
                               .radius(10000)
                               .center(marker.getPosition())
               );

           }
       });


        //To be used for changing circle radius
       googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
                /*temp.setLatitude(latLng.latitude);
                temp.setLongitude(latLng.longitude);
                if (geoArea.contains(temp)) {
                    final ArrayAdapter<Double> arrayAdapter = new ArrayAdapter<Double>
                            (LocationPickActivity.this,R.layout.change_radius_list);
                    for (Double d = 0.0; d < 50000; d++) {
                        arrayAdapter.add(d);
                    }

                    AlertDialog.Builder changeRadius =
                            new AlertDialog.Builder(LocationPickActivity.this);
                    changeRadius.setTitle(R.string.change_radius);
                    changeRadius.setCancelable(true);
                    changeRadius.setIcon(R.drawable.fudi_icon);
                    changeRadius.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    changeRadius.create();
                    changeRadius.show();
                }
                }
            }*/
        });
    }
}
