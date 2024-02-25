package ir.roudi.littleneshan.data.repository.location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import ir.roudi.littleneshan.data.model.LocationModel;

public class LocationRepositoryFake implements LocationRepository {

    @Inject
    public LocationRepositoryFake() {

    }

    private static final LocationModel INITIAL_LOCATION =
            new LocationModel(36.32696629834568, 59.53865276231169);

    private final MutableLiveData<LocationModel> userLocation =
            new MutableLiveData<>(INITIAL_LOCATION);

    @Override
    public LiveData<LocationModel> getLastLocation() {
        return new MutableLiveData<>(INITIAL_LOCATION);
    }

    @Override
    public LiveData<LocationModel> getCurrentLocation() {
        return userLocation;
    }

    @Override
    public void subscribeToReceiveLocationUpdates(OnTurnOnLocationResultListener resultListener) {

    }

    @Override
    public void subscribeToReceiveLocationUpdates(PrecisionLocationRequest request, OnTurnOnLocationResultListener resultListener) {

    }

    @Override
    public void unsubscribeFromReceivingLocationUpdates() {

    }

    @Override
    public void setUserLocation(LocationModel location) {
        userLocation.postValue(location);
    }
}
