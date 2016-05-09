package com.fudi.fudi.back;

import android.graphics.Color;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fudi.fudi.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * Represents a geographical area, which,  is a large circle centered on a point.
 * The GeoArea in which a user is located is used to determine what Fud posts to load.
 * Created by chijioke on 4/19/16.
 */

public class GeoArea implements Parcelable{

    public static final GeoArea WASHINGTON_DC = new GeoArea("Washington D.C.", 38.8977, -77.0365, 10000),
            NEW_YORK = new GeoArea("New York City", 40.7128, -74.0059, 10000),
            GLOBAL = new GeoArea("Global", 0, 0, 18000000);



    private final double longitude;
    private final  double latitude;
    private final String name;
    private double radius;
    private final LatLng latlng;

    public GeoArea(String name, double lat, double lon, double radius) {
        this.name = name;
        longitude = lon;
        latitude = lat;
        this.radius = radius;
        latlng = new LatLng(latitude, longitude);
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

    public String getName() {return name;}

    public double getRadius() {return radius;}

    public double getLatitude() {return latitude;}

    public double getLongitude() {return longitude;}

    /**
     * Sets the radius this GeoArea is covering
     * @param radius Radius
     */
    public void setRadius(double radius) {this.radius = radius;}

    /**
     * Draws this geoArea on the map passed by the user
     * @param map Map to be drawn on
     */
    public Circle drawGeoAreaCircle(GoogleMap map) {
        Log.i("Drawing", "Drawing Marker");
        map.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title("Current")
                        .draggable(true)
                        .visible(true)
        );

        Log.i("Drawing", "Drawing Circle");
        Circle geoCircle = map.addCircle(new CircleOptions()
            .center(latlng)
            .radius(radius)
            .fillColor(R.color.fudi_text_color)
            .visible(true)
        );

        return geoCircle;
    }

    @Override
    public String toString() {
        return "GeoArea{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", name='" + name + '\'' +
                ", radius=" + radius +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(radius);
        dest.writeString(name);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new GeoArea(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new GeoArea[size];
        }
    };

    private GeoArea(Parcel p) {
        latitude = p.readDouble();
        longitude = p.readDouble();
        radius = p.readDouble();
        name = p.readString();
        latlng = new LatLng(latitude, longitude);
    }
}
