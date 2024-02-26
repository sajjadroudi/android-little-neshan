package ir.roudi.littleneshan.ui.navigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseViewModel;
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

    private final MutableLiveData<Event<Boolean>> reachedDestination = new MutableLiveData<>(new Event<>(false));

    private final MutableLiveData<Event<LocationModel>> focusOnUserLocationEvent = new MutableLiveData<>(new Event<>(null));

    private final LiveData<List<LocationModel>> remainingPointsPath;

    private final LiveData<List<StepModel>> remainingSteps;

    private final LiveData<StepModel> currentStep;

    private final LiveData<String> remainingDistance;

    private final LiveData<String> remainingDuration;

    private final LiveData<LocationModel> userLocation;

    private Disposable navigationDisposable;

    @Inject
    public NavigationViewModel(
            LocationRepository locationRepository,
            NavigationRepository navigationRepository
    ) {
        this.locationRepository = locationRepository;
        this.navigationRepository = navigationRepository;

        userLocation = locationRepository.getCurrentLocation();

        var remaining = navigationRepository.getRemainingNavigationPoints();

        remainingPointsPath = Transformations.map(remaining, points -> {
            return points.stream()
                    .map(NavigationPointModel::getPoint)
                    .collect(Collectors.toList());
        });

        remainingSteps = Transformations.map(remaining, points -> {
            return points.stream()
                    .map(NavigationPointModel::getStep)
                    .distinct()
                    .collect(Collectors.toList());
        });

        currentStep = Transformations.map(remainingSteps, steps -> {
                    return (steps == null || steps.isEmpty()) ? null : steps.get(0);
                }
        );

        remainingDistance = Transformations.map(remainingSteps, steps -> {
            var distanceInMeter = steps.stream()
                    .map(it -> it.getDistance().getValue())
                    .reduce(Integer::sum)
                    .orElse(0);
            return distanceInMeter + " متر";
        });

        remainingDuration = Transformations.map(remainingSteps, steps -> {
            var durationInSeconds = steps.stream()
                    .map(it -> it.getDuration().getValue())
                    .reduce(Integer::sum)
                    .orElse(0);
            var durationInMinutes = durationInSeconds / 60;
            if(durationInMinutes == 0) {
                return "کمتر از ۱ دقیقه";
            }
            return durationInMinutes + " دقیقه";
        });
    }

    public void startLocationUpdates(OnTurnOnLocationResultListener callback) {
        locationRepository.subscribeToReceiveLocationUpdates(PrecisionLocationRequest.PRECISE, callback);
    }

    public void startNavigation(LocationModel startLocation, LocationModel endLocation) {
        if(navigationDisposable != null)
            navigationDisposable.dispose();

        navigationRepository.startNavigation(startLocation, endLocation, userLocation.getValue())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        navigationDisposable = d;
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (ExceptionUtils.isDisconnectedToServer(e)) {
                            showError(R.string.connection_to_server_error);
                        } else {
                            showError(R.string.something_went_wrong);
                        }

                        navigateUp();
                    }
                });
    }

    public void updateUserProgress() {
        boolean reachedDestination = navigationRepository.updateUserProgress(userLocation.getValue());
        this.reachedDestination.postValue(new Event<>(reachedDestination));
    }

    @Override
    public void navigateUp() {
        super.navigateUp();
        navigationRepository.finishRouting();
    }

    public void focusOnUserLocation() {
        var location = userLocation.getValue();
        focusOnUserLocationEvent.postValue(new Event<>(location));
    }

    public LiveData<Event<LocationModel>> getFocusOnUserLocationEvent() {
        return focusOnUserLocationEvent;
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

    private List<NavigationPointModel> toRoutingPoints(List<StepModel> steps) {
        return steps.stream()
                .map(step -> step.getRoutingPoints()
                        .stream()
                        .map(location -> new NavigationPointModel(location, step))
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public LiveData<StepModel> getCurrentStep() {
        return currentStep;
    }

    public LiveData<String> getRemainingDuration() {
        return remainingDuration;
    }

    public LiveData<String> getRemainingDistance() {
        return remainingDistance;
    }

    public LocationModel getDestination() {
        return navigationRepository.getDestination();
    }

    @Override
    protected void onCleared() {

        locationRepository.unsubscribeFromReceivingLocationUpdates();

        super.onCleared();
    }
}
