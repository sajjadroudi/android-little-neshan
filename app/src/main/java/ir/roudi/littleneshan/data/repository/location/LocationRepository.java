package ir.roudi.littleneshan.data.repository.location;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;

import com.google.android.gms.location.LocationRequest;

import ir.roudi.littleneshan.data.model.LocationModel;

public interface LocationRepository {

    LiveData<LocationModel> getLastLocation();

    LiveData<LocationModel> getCurrentLocation();

    void subscribeToReceiveLocationUpdates(
            OnTurnOnLocationResultListener resultListener
    );

    @SuppressLint("MissingPermission")
    void subscribeToReceiveLocationUpdates(
            PrecisionLocationRequest request,
            OnTurnOnLocationResultListener resultListener
    );

    void unsubscribeFromReceivingLocationUpdates();
}
