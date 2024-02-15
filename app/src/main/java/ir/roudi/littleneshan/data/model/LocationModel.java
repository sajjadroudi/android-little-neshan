package ir.roudi.littleneshan.data.model;

import android.location.Location;

import androidx.annotation.NonNull;

public class LocationModel {

    private final double latitude;

    private final double longitude;

    public LocationModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationModel(double[] location) {
        this(location[0], location[1]);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationModel{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public static LocationModel from(Location location) {
        return new LocationModel(location.getLatitude(), location.getLongitude());
    }

}