package ir.roudi.littleneshan.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.R;
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

    private final LiveData<UserLocationUiModel> userLocation;

    private final MutableLiveData<Event<String>> navigationPath = new MutableLiveData<>();

    private final MutableLiveData<Event<AddressUiModel>> destinationAddress = new MutableLiveData<>();

    private final MutableLiveData<Event<Boolean>> navigateToNavigationScreen = new MutableLiveData<>();

    private final MutableLiveData<Event<Boolean>> switchTheme = new MutableLiveData<>(new Event<>(false));

    private final MutableLiveData<Event<Boolean>> focusOnUserLocation = new MutableLiveData<>(new Event<>(false));

    private Disposable navigationPathDisposable;
    private Disposable addressDisposable;

    private LocationModel startLocation;

    private LocationModel endLocation;

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

    public void navigate(LocationModel destination) {
        var source = userLocation.getValue();

        if (source == null || source.getLocation() == null || destination == null) {
            showError(R.string.something_went_wrong);
            return;
        }

        startLocation = source.getLocation();
        endLocation = destination;

        // TODO: Handle timeout situation
        navigationPathDisposable = navigationRepository
                .getDirection(startLocation, endLocation, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(direction -> {
                    navigationPath.postValue(new Event<>(direction.getOverviewPolyline()));

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
                                destinationAddress.postValue(new Event<>(value));
                            });
                });
    }

    public void navigateToNavigationScreen() {
        navigateToNavigationScreen.postValue(new Event<>(true));
    }

    public LiveData<Event<Boolean>> getSwitchThemeEvent() {
        return switchTheme;
    }

    public void switchTheme() {
        switchTheme.postValue(new Event<>(true));
    }

    public LiveData<Event<Boolean>> getFocusOnUserLocationEvent() {
        return focusOnUserLocation;
    }

    public void focusOnUserLocation() {
        focusOnUserLocation.postValue(new Event<>(true));
    }

    public LiveData<Event<AddressUiModel>> getDestinationAddress() {
        return destinationAddress;
    }

    public LiveData<Event<String>> getNavigationPath() {
        return navigationPath;
    }

    public LiveData<Event<Boolean>> getNavigateToNavigationScreen() {
        return navigateToNavigationScreen;
    }

    public LiveData<UserLocationUiModel> getUserLocation() {
        return userLocation;
    }

    public LocationModel getStartLocation() {
        return startLocation;
    }

    public LocationModel getEndLocation() {
        return endLocation;
    }

    public void clearNavigationData() {
        startLocation = null;
        endLocation = null;
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
