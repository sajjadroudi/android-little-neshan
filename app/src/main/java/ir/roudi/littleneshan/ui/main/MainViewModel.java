package ir.roudi.littleneshan.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.core.BaseViewModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.utils.Event;

@HiltViewModel
public class MainViewModel extends BaseViewModel {

    private final LocationRepository locationRepository;

    private final NavigationRepository navigationRepository;

    public final LiveData<UserLocationUiModel> userLocation;

    private final MutableLiveData<Event<String>> _navigationPath = new MutableLiveData<>();
    public final LiveData<Event<String>> navigationPath = _navigationPath;

    private final MutableLiveData<Event<AddressUiModel>> _address = new MutableLiveData<>();
    public final LiveData<Event<AddressUiModel>> address = _address;

    private final MutableLiveData<Event<Object>> _navigateToNavigationScreen = new MutableLiveData<>();
    public final LiveData<Event<Object>> navigateToNavigationScreen = _navigateToNavigationScreen;

    private final MutableLiveData<Event<Boolean>> _switchTheme = new MutableLiveData<>(new Event<>(false));

    private final MutableLiveData<Event<Boolean>> focusOnUserLocation = new MutableLiveData<>(new Event<>(false));

    private Disposable navigationPathDisposable;
    private Disposable addressDisposable;

    // TODO: Define getter and setter for startLocation and endLocation
    public LocationModel startLocation;

    public LocationModel endLocation;

    @Inject
    public MainViewModel(LocationRepository locationRepository, NavigationRepository navigationRepository) {
        this.locationRepository = locationRepository;
        this.navigationRepository = navigationRepository;

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
            if (prevLocation != null && prevLocation.isCached()) {
                var value = new UserLocationUiModel(location, true);
                userLocation.setValue(value);
            }
        });

        return userLocation;
    }

    public void startLocationUpdates(OnTurnOnLocationResultListener callback) {
        locationRepository.subscribeToReceiveLocationUpdates(callback);
    }

    public void stopLocationUpdates() {
        locationRepository.unsubscribeFromReceivingLocationUpdates();
    }

    public void navigate() {
        if (startLocation == null || endLocation == null) {
            // TODO: Handle error
            return;
        }

        // TODO: Handle timeout situation
        navigationPathDisposable = navigationRepository
                .getDirection(startLocation, endLocation, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(direction -> {
                    _navigationPath.postValue(new Event<>(direction.getOverviewPolyline()));

                    // TODO: Handle worse case scenarios when data gotten from server is null or invalid.
                    addressDisposable = navigationRepository
                            .getAddress(endLocation)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(address -> {
                                var routeName = (address.getRouteName() == null) ? "معبر بدون نام" : address.getRouteName();
                                var value = new AddressUiModel(
                                        routeName,
                                        direction.getDuration().getText(),
                                        direction.getDistance().getText(),
                                        address.getAddress()
                                );
                                _address.postValue(new Event<>(value));
                            });
                });
    }

    public void navigateToNavigationScreen() {
        _navigateToNavigationScreen.postValue(new Event<>(new Object()));
    }

    public LiveData<Event<Boolean>> getSwitchThemeEvent() {
        return _switchTheme;
    }

    public void switchTheme() {
        _switchTheme.postValue(new Event<>(true));
    }

    public LiveData<Event<Boolean>> getFocusOnUserLocationEvent() {
        return focusOnUserLocation;
    }

    public void focusOnUserLocation() {
        focusOnUserLocation.postValue(new Event<>(true));
    }

    @Override
    protected void onCleared() {
        if(navigationPathDisposable != null)
            navigationPathDisposable.dispose();

        if(addressDisposable != null)
            addressDisposable.dispose();

        super.onCleared();
    }
}
