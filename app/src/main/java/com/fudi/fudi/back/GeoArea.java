package com.fudi.fudi.back;

import android.location.Location;

/**
 * Represents a geographical area, which, FOR NOW is a large square centered on a point.
 * The GeoArea in which a user is located is used to determine what Fud posts to load.
 * Created by chijioke on 4/19/16.
 */
public class GeoArea {

    public static final GeoArea WASHINGTON_DC = new GeoArea("Washington D.C.", 38.8977, -77.0365, 1),
            NEW_YORK = new GeoArea("New York City", 40.7128, -74.0059, 1);

    private double longitude;
    private double latitude;
    private String name;
    private double length;

    private GeoArea(String name, double lon, double lat, double length){
        this.name = name;
        longitude = lon;
        latitude = lat;
        this.length = length;
    }

    /**
     *
     * @param loc
     * @return true if the location is inside the GeoArea
     */
    public boolean isWithin(Location loc) {
        double localLat = loc.getLatitude();
        double localLong = loc.getLongitude();
        //TODO: determine if the passed in location is inside this GeoArea
        //For now want a circle around the passed in longitude and latitude point
        return true;
    }

    /**
     * Queries the database of GeoAreas and return the GeoArea, if there is one
     * @param lon
     * @param lat
     * @return the GeoArea, or null if none exists
     */
    public static GeoArea getGeoArea(double lon, double lat){
        //TODO: connect to the database and return the GeoArea that contains these coordinates

        return null;
    }

    public String getName() {
        return name;
    }
}
