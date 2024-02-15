package ir.roudi.littleneshan.ui.main;

import androidx.annotation.NonNull;

import ir.roudi.littleneshan.data.model.LocationModel;

public class UserLocationUiModel {

    private final LocationModel location;
    private final boolean isCached;

    public UserLocationUiModel(LocationModel location, boolean isCached) {
        this.location = location;
        this.isCached = isCached;
    }

    public LocationModel getLocation() {
        return location;
    }

    public boolean isCached() {
        return isCached;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserLocationUiModel{" +
                "location=" + location +
                ", isCached=" + isCached +
                '}';
    }
}
