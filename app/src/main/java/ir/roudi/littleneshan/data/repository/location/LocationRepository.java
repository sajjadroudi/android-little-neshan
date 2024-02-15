package ir.roudi.littleneshan.data.repository.location;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;

import com.google.android.gms.location.LocationRequest;

import ir.roudi.littleneshan.data.model.LocationModel;

public interface LocationRepository {

    LiveData<LocationModel> getLastLocation();

    LiveData<LocationModel> getCurrentLocation();

    void subscribeToReceiveLocationUpdates(
            OnTurnOnGpsCallback turnOnGpsCallback
    );

    @SuppressLint("MissingPermission")
    void subscribeToReceiveLocationUpdates(
            LocationRequest locationRequest,
            OnTurnOnGpsCallback turnOnGpsCallback
    );

    void unsubscribeFromReceivingLocationUpdates();
}
