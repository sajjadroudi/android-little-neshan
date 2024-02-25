package ir.roudi.littleneshan.data.repository.location;

import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ir.roudi.littleneshan.data.model.LocationModel;

public class LocationRepositoryDefault implements LocationRepository {

    private final FusedLocationProviderClient locationClient;

    private final SettingsClient locationSettingsClient;

    private final MutableLiveData<LocationModel> lastLocation = new MutableLiveData<>();

    private final MutableLiveData<LocationModel> currentLocation = new MutableLiveData<>();

    private final LocationCallback currentLocationListener = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location location = locationResult.getLastLocation();
            if (location != null) {
                currentLocation.postValue(LocationModel.from(location));
            }
        }
    };

    @Inject
    public LocationRepositoryDefault(
            FusedLocationProviderClient locationClient,
            SettingsClient settingsClient
    ) {
        this.locationClient = locationClient;
        this.locationSettingsClient = settingsClient;
    }

    @Override
    public LiveData<LocationModel> getLastLocation() {
        return lastLocation;
    }

    @Override
    public LiveData<LocationModel> getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void subscribeToReceiveLocationUpdates(
            OnTurnOnLocationResultListener resultListener
    ) {
        subscribeToReceiveLocationUpdates(PrecisionLocationRequest.APPROXIMATE, resultListener);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void subscribeToReceiveLocationUpdates(
            PrecisionLocationRequest request,
            OnTurnOnLocationResultListener resultListener
    ) {
        unsubscribeFromReceivingLocationUpdates();

        // TODO: Maybe need to cache same object of location settings response task
        buildDefaultLocationSettingsResponseTask(request.toLocationRequest())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(!(e instanceof ApiException))
                            return;

                        final int statusCode = ((ApiException) e).getStatusCode();
                        if(statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                if(e instanceof ResolvableApiException) {
                                    final ResolvableApiException rae = (ResolvableApiException) e;
                                    resultListener.onRequireResolution(rae);
                                }
                            } catch (IntentSender.SendIntentException sie) {
                                resultListener.onSendIntentException(sie);
                            }
                        } else if(statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            resultListener.onSettingsChangeUnavailable();
                        }

                    }
                });

        locationClient.requestLocationUpdates(
                request.toLocationRequest(),
                currentLocationListener,
                Looper.getMainLooper()
        );

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastLocation.postValue(LocationModel.from(location));
            }
        });
    }

    @Override
    public void unsubscribeFromReceivingLocationUpdates() {
        locationClient.removeLocationUpdates(currentLocationListener);
    }

    private Task<LocationSettingsResponse> buildDefaultLocationSettingsResponseTask(
            LocationRequest request
    ) {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .build();

        return locationSettingsClient.checkLocationSettings(settingsRequest);
    }

    @Override
    public void setUserLocation(LocationModel location) {

    }
}