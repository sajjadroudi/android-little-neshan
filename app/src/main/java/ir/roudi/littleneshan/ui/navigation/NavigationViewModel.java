package ir.roudi.littleneshan.ui.navigation;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseViewModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.data.repository.location.PrecisionLocationRequest;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.utils.Event;
import ir.roudi.littleneshan.utils.ExceptionUtils;

@HiltViewModel
public class NavigationViewModel extends BaseViewModel {

    private final LocationRepository locationRepository;
    private final NavigationRepository navigationRepository;

    private final MutableLiveData<DirectionModel> direction = new MutableLiveData<>();

    private final MutableLiveData<List<LocationModel>> remainingPointsPath = new MutableLiveData<>(List.of());

    private final MutableLiveData<Event<Boolean>> reachedDestination = new MutableLiveData<>(new Event<>(false));

    private final MutableLiveData<Event<LocationModel>> focusOnUserLocationEvent = new MutableLiveData<>(new Event<>(null));

    private final LiveData<LocationModel> userLocation;

    private Disposable loadDirectionDisposable;

    private int lastReachedPointIndex = 0;

    private List<LocationModel> routingPoints = new ArrayList<>();

    @Inject
    public NavigationViewModel(
            LocationRepository locationRepository,
            NavigationRepository navigationRepository
    ) {
        this.locationRepository = locationRepository;
        this.navigationRepository = navigationRepository;

        userLocation = locationRepository.getCurrentLocation();
    }

    public void startLocationUpdates(OnTurnOnLocationResultListener callback) {
        locationRepository.subscribeToReceiveLocationUpdates(PrecisionLocationRequest.PRECISE, callback);
    }

    public void startNavigation(LocationModel startLocation, LocationModel endLocation) {
        loadDirection(startLocation, endLocation, 0);
    }

    private void loadDirection(
            LocationModel startLocation,
            LocationModel endLocation,
            int bearing
    ) {
        if(loadDirectionDisposable != null)
            loadDirectionDisposable.dispose();

        navigationRepository
                .getDirection(startLocation, endLocation, bearing)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<DirectionModel>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        loadDirectionDisposable = d;
                    }

                    @Override
                    public void onSuccess(@NonNull DirectionModel direction) {
                        lastReachedPointIndex = 0;

                        var routingPoints = toRoutingPoints(direction.getSteps());
                        NavigationViewModel.this.routingPoints = routingPoints;
                        remainingPointsPath.postValue(routingPoints);

                        NavigationViewModel.this.direction.setValue(direction);

                        updateUserProgress();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if(ExceptionUtils.isDisconnectedToServer(e)) {
                            showError(R.string.connection_to_server_error);
                        } else {
                            showError(R.string.something_went_wrong);
                        }
                    }
                });
    }

    public void updateUserProgress() {
        Log.i("rouditest", "updateUserProgress: 2: " + (routingPoints == null ? null : routingPoints.size()));
        if(routingPoints == null || routingPoints.size() < 2) {
            return;
        }

        var userLocation = this.userLocation.getValue();
        Log.i("rouditest", "updateUserProgress: 3: " + userLocation);
        if(userLocation == null) {
            return;
        }

        var currentPoint = routingPoints.get(lastReachedPointIndex);
        var nextPoint = routingPoints.get(lastReachedPointIndex + 1);

        Log.i("rouditest", "updateUserProgress: 4: current=" + currentPoint);
        Log.i("rouditest", "updateUserProgress: 5: next=" + nextPoint);

        var currentPointToNextPointDistance = currentPoint.distanceTo(nextPoint);
        var currentPointToUserPointDistance = currentPoint.distanceTo(userLocation);

        Log.i("rouditest", "updateUserProgress: 6: current-next=" + currentPointToNextPointDistance);
        Log.i("rouditest", "updateUserProgress: 7: current-user=" + currentPointToUserPointDistance);

        var userShouldBeLocatedOnTheFirstStep = (lastReachedPointIndex == 0);
        var userIsNotLocatedOnTheFirstStep = (currentPointToUserPointDistance > 50);

        Log.i("rouditest", "updateUserProgress: 8: userShouldBeLocatedOnTheFirstStep=" + userShouldBeLocatedOnTheFirstStep);
        Log.i("rouditest", "updateUserProgress: 9: userIsNotLocatedOnTheFirstStep=" + userIsNotLocatedOnTheFirstStep);

        if(userShouldBeLocatedOnTheFirstStep && userIsNotLocatedOnTheFirstStep) {
            return;
        }

        var userIsPassedCurrentPoint = (currentPointToUserPointDistance >= currentPointToNextPointDistance);
        Log.i("rouditest", "updateUserProgress: 10: userIsPassedCurrentStep=" + userIsPassedCurrentPoint);
        if(userIsPassedCurrentPoint) {
            lastReachedPointIndex++;

            Log.i("rouditest", "updateUserProgress: 11: lastReachedStepIndex=" + lastReachedPointIndex);
            var remaining = routingPoints.subList(lastReachedPointIndex, routingPoints.size());
            remainingPointsPath.postValue(remaining);

            var reachedDestination = (remaining.size() <= 2);
            Log.i("rouditest", "updateUserProgress: 12: reachedDestination=" + reachedDestination);
            if(reachedDestination) {
                lastReachedPointIndex = 0;
                this.reachedDestination.postValue(new Event<>(true));
            }
        }
    }

    public void focusOnUserLocation() {
        var location = userLocation.getValue();
        focusOnUserLocationEvent.postValue(new Event<>(location));
    }

    public LiveData<Event<LocationModel>> getFocusOnUserLocationEvent() {
        return focusOnUserLocationEvent;
    }

    public LiveData<DirectionModel> getDirection() {
        return direction;
    }

    public LiveData<List<LocationModel>> getRemainingPointsPath() {
        return remainingPointsPath;
    }

    public LiveData<Event<Boolean>> getReachedDestination() {
        return reachedDestination;
    }

    public LiveData<LocationModel> getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(LocationModel location) {
        locationRepository.setUserLocation(location);
    }

    private List<LocationModel> toRoutingPoints(List<StepModel> steps) {
        return steps.stream()
                .map(StepModel::getRoutingPoints)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    protected void onCleared() {

        locationRepository.unsubscribeFromReceivingLocationUpdates();

        super.onCleared();
    }
}
