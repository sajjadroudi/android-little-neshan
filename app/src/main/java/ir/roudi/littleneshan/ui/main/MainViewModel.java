package ir.roudi.littleneshan.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final LocationRepository locationRepository;

    public final LiveData<UserLocationUiModel> userLocation;

    @Inject
    public MainViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;

        userLocation = buildUserLocationLiveData();
    }

    private LiveData<UserLocationUiModel> buildUserLocationLiveData() {
        var userLocation = new MediatorLiveData<UserLocationUiModel>();

        userLocation.addSource(locationRepository.getCurrentLocation(), location -> {
            var value = new UserLocationUiModel(location, false);
            userLocation.setValue(value);
        });

        userLocation.addSource(locationRepository.getLastLocation(), location -> {
            var prevLocation = userLocation.getValue();
            if(prevLocation != null && prevLocation.isCached()) {
                var value = new UserLocationUiModel(location, true);
                userLocation.setValue(value);
            }
        });

        return userLocation;
    }

    public void startLocationUpdates(OnTurnOnGpsCallback callback) {
        locationRepository.subscribeToReceiveLocationUpdates(callback);
    }

    public void stopLocationUpdates() {
        locationRepository.unsubscribeFromReceivingLocationUpdates();
    }

}
