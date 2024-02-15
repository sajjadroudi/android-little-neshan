package ir.roudi.littleneshan.data.repository.location;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import ir.roudi.littleneshan.data.model.LocationModel;

public class LocationRepositoryDefault implements LocationRepository {

    private FusedLocationProviderClient locationClient;

    private SettingsClient locationSettingsClient;

    private final MutableLiveData<LocationModel> _lastLocation = new MutableLiveData<>();
    public LiveData<LocationModel> lastLocation = _lastLocation;

    private final MutableLiveData<LocationModel> _currentLocation = new MutableLiveData<>();
    public LiveData<LocationModel> currentLocation = _currentLocation;

    private final LocationCallback currentLocationListener = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location location = locationResult.getLastLocation();
            if (location != null) {
                _currentLocation.postValue(LocationModel.from(location));
            }
        }
    };

    @Override
    public void subscribeToReceiveLocationUpdates(
            OnTurnOnGpsCallback turnOnGpsCallback
    ) {
        subscribeToReceiveLocationUpdates(buildDefaultLocationRequest(), turnOnGpsCallback);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void subscribeToReceiveLocationUpdates(
            LocationRequest locationRequest,
            OnTurnOnGpsCallback turnOnGpsCallback
    ) {
        // TODO: Maybe turnOnGpsCallback is not going to turn GPS on

        // TODO: Maybe need to unsubscribe first

        // TODO: Maybe need to cache same object of location settings response task
        buildDefaultLocationSettingsResponseTask(locationRequest)
                .addOnFailureListener(turnOnGpsCallback::turnOnGps);

        locationClient.requestLocationUpdates(
                locationRequest,
                currentLocationListener,
                Looper.getMainLooper()
        );

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                _lastLocation.postValue(LocationModel.from(location));
            }
        });
    }

    @Override
    public void unsubscribeFromReceivingLocationUpdates() {
        locationClient.removeLocationUpdates(currentLocationListener);
    }

    private LocationRequest buildDefaultLocationRequest() {
        return new LocationRequest.Builder(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                TimeUnit.SECONDS.toMillis(60)
        )
                .build()
                .setFastestInterval(TimeUnit.SECONDS.toMillis(30))
                .setMaxWaitTime(TimeUnit.MINUTES.toMillis(2));
    }

    private Task<LocationSettingsResponse> buildDefaultLocationSettingsResponseTask(
            LocationRequest request
    ) {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .build();

        return locationSettingsClient.checkLocationSettings(settingsRequest);
    }

}