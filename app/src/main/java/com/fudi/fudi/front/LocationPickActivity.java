package com.fudi.fudi.front;

import android.content.Intent;
import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fudi.fudi.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class LocationPickActivity extends FragmentActivity implements OnMapReadyCallback {

    private LinearLayout buttonHolder;
    private FrameLayout mapHolder;
    private MapFragment mapFragment;

    private GoogleMap map;

    private boolean clickedChoose;

    public static int HERE_SELECTED = 200;
    public static int GLOBAL_SELECTED = 201;
    public static int CHOOSE_SELECTED = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_pick);

        buttonHolder = (LinearLayout) findViewById(R.id.locset_button_holder);
        mapHolder = (FrameLayout) findViewById(R.id.locset_map_holder);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.locset_map);
        mapFragment.getMapAsync(this);
        if(map != null){
            //TODO whatever
        }

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
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do not set result just yet
                buttonHolder.setVisibility(View.GONE);
                mapHolder.setVisibility(View.VISIBLE);
                clickedChoose = true;
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
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
    }
}
