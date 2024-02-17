package ir.roudi.littleneshan.ui.navigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;

@HiltViewModel
public class NavigationViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final NavigationRepository navigationRepository;

    private final MutableLiveData<DirectionModel> _direction = new MutableLiveData<>();
    public final LiveData<DirectionModel> direction = _direction;
    private LocationModel startLocation;
    private LocationModel endLocation;
    private Disposable loadDirectionDisposable;
    public final LiveData<LocationModel> userLocation;

    @Inject
    public NavigationViewModel(
            LocationRepository locationRepository,
            NavigationRepository navigationRepository
    ) {
        this.locationRepository = locationRepository;
        this.navigationRepository = navigationRepository;

        userLocation = locationRepository.getCurrentLocation();
    }

    public void startLocationUpdates(OnTurnOnGpsCallback callback) {
        locationRepository.subscribeToReceiveLocationUpdates(callback);
    }

    public void stopLocationUpdates() {
        locationRepository.unsubscribeFromReceivingLocationUpdates();
    }

    public void startNavigation(LocationModel startLocation, LocationModel endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;

        loadDirection(startLocation, endLocation, 0);
    }

    private void loadDirection(
            LocationModel startLocation,
            LocationModel endLocation,
            int bearing
    ) {
        if(loadDirectionDisposable != null)
            loadDirectionDisposable.dispose();

        loadDirectionDisposable = navigationRepository
                .getDirection(startLocation, endLocation, bearing)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(direction -> {
                    _direction.postValue(direction);
                });
    }

}
