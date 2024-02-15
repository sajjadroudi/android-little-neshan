package ir.roudi.littleneshan.data.repository.location;

import android.annotation.SuppressLint;

import com.google.android.gms.location.LocationRequest;

public interface LocationRepository {
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
