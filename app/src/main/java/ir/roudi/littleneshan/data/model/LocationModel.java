package ir.roudi.littleneshan.data.model;

import android.location.Location;

import androidx.annotation.NonNull;

import org.neshan.common.model.LatLng;

import java.io.Serializable;

public class LocationModel implements Serializable {

    private static final float DEFAULT_ACCURACY = 0f;
    private static final float DEFAULT_BEARING = 0f;

    private final double latitude;

    private final double longitude;

    private final float bearing;

    private final float accuracy;

    public LocationModel(double latitude, double longitude, float bearing, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
        this.accuracy = accuracy;
    }

    public LocationModel(double latitude, double longitude) {
        this(latitude, longitude, DEFAULT_BEARING, DEFAULT_ACCURACY);
    }

    public LocationModel(double[] location) {
        this(location[0], location[1], DEFAULT_BEARING, DEFAULT_ACCURACY);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getBearing() {
        return bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationModel{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", bearing=" + bearing +
                ", accuracy=" + accuracy +
                '}';
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    public float distanceTo(LocationModel destination) {
        return toLocation().distanceTo(destination.toLocation());
    }

    public Location toLocation() {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setBearing(bearing);
        location.setAccuracy(accuracy);
        return location;
    }

    public float bearingTo(LocationModel location) {
        float bearing = toLocation().bearingTo(location.toLocation());
        float normalizedBearing = (bearing + 360) % 360;
        return normalizedBearing;
    }

    public static LocationModel from(Location location) {
        return new LocationModel(location.getLatitude(), location.getLongitude(), location.getBearing(), location.getAccuracy());
    }

    public static LocationModel from(LatLng location) {
        return new LocationModel(location.getLatitude(), location.getLongitude());
    }

}
