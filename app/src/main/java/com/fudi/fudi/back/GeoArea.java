package com.fudi.fudi.back;

import android.graphics.Color;
import android.location.Location;

import com.fudi.fudi.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


/**
 * Represents a geographical area, which,  is a large circle centered on a point.
 * The GeoArea in which a user is located is used to determine what Fud posts to load.
 * Created by chijioke on 4/19/16.
 */

public class GeoArea {

    public static final GeoArea WASHINGTON_DC = new GeoArea("Washington D.C.", 38.8977, -77.0365, 1),
            NEW_YORK = new GeoArea("New York City", 40.7128, -74.0059, 1);

    private final double longitude;
    private final  double latitude;
    private final String name;
    private double radius;
    private final LatLng latlng;

    public GeoArea(String name, double lon, double lat, double radius){
        this.name = name;
        longitude = lon;
        latitude = lat;
        latlng = new LatLng(latitude, longitude);
        this.radius = radius;
    }

    /**
     *
     * @param loc The location to check is within the GeoArea
     * @return true if the location is inside the GeoArea
     */
    public boolean contains(Location loc) {
        double localLat = loc.getLatitude();
        double localLong = loc.getLongitude();

        return ((localLat <= latitude + radius && localLat >= latitude - radius)
                && (localLong <= longitude + radius && localLong >= longitude - radius));
    }

    /**
     * Queries the database of GeoAreas and return the GeoArea, if there is one
     * @param lon Longitude
     * @param lat Latitude
     * @return the GeoArea, or null if none exists
     */
    public static GeoArea getGeoArea(double lon, double lat){
        //TODO: connect to the database and return the GeoArea that contains these coordinates

        return null;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the radius this GeoArea is covering
     * @param radius Radius
     */
    public void setRadius(double radius) {this.radius = radius;}

    /**
     * Draws this geoArea on the map passed by the user
     * @param map Map to be drawn on
     */
    public void drawGeoAreaCircle(GoogleMap map) {
            map.addCircle(new CircleOptions()
            .center(latlng)
            .radius(radius)
            .fillColor(R.color.fudi_text_color)
            .strokeColor(Color.TRANSPARENT)
        );
    }
}
