package ir.roudi.littleneshan.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final LocationRepository locationRepository;

    public final LiveData<LocationModel> currentLocation;
    public final LiveData<LocationModel> lastLocation;

    @Inject
    public MainViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;

        currentLocation = locationRepository.getCurrentLocation();
        lastLocation = locationRepository.getLastLocation();
    }

    public void startLocationUpdates(OnTurnOnGpsCallback callback) {
        locationRepository.subscribeToReceiveLocationUpdates(callback);
    }

    public void stopLocationUpdates() {
        locationRepository.unsubscribeFromReceivingLocationUpdates();
    }

}
