package ir.roudi.littleneshan.ui.navigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.core.BaseViewModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.utils.Event;

@HiltViewModel
public class NavigationViewModel extends BaseViewModel {

    private final LocationRepository locationRepository;
    private final NavigationRepository navigationRepository;

    private final MutableLiveData<DirectionModel> direction = new MutableLiveData<>();

    private final MutableLiveData<List<StepModel>> remainingSteps = new MutableLiveData<>(List.of());

    private final MutableLiveData<Event<Boolean>> reachedDestination = new MutableLiveData<>(new Event<>(false));

    private final MutableLiveData<Event<LocationModel>> focusOnUserLocationEvent = new MutableLiveData<>(new Event<>(null));

    private final LiveData<LocationModel> userLocation;

    private Disposable loadDirectionDisposable;

    private int lastReachedStepIndex = 0;

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
        locationRepository.subscribeToReceiveLocationUpdates(callback);
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

        loadDirectionDisposable = navigationRepository
                .getDirection(startLocation, endLocation, bearing)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(direction -> {
                    lastReachedStepIndex = 0;

                    remainingSteps.postValue(direction.getSteps());

                    this.direction.postValue(direction);
                });
    }

    public void updateUserProgress() {
        var direction = this.direction.getValue();
        if(direction == null) {
            // TODO: Handle error
            return;
        }

        List<StepModel> steps = direction.getSteps();

        if(steps == null || steps.size() < 2) {
            // TODO: Handle error
            return;
        }

        var userLocation = this.userLocation.getValue();
        if(userLocation == null) {
            // TODO: Handle error
            return;
        }

        var currentStep = steps.get(lastReachedStepIndex);
        var nextStep = steps.get(lastReachedStepIndex + 1);

        var currentStepToNextStepDistance = currentStep.getStartPoint().distanceTo(nextStep.getStartPoint());
        var currentStepToUserLocationDistance = currentStep.getStartPoint().distanceTo(userLocation);

        var userShouldBeLocatedOnTheFirstStep = (lastReachedStepIndex == 0);
        var userIsNotLocatedOnTheFirstStep = (currentStepToUserLocationDistance > 5);
        if(userShouldBeLocatedOnTheFirstStep && userIsNotLocatedOnTheFirstStep) {
            return;
        }

        var userIsPassedCurrentStep = (currentStepToUserLocationDistance >= currentStepToNextStepDistance);
        if(userIsPassedCurrentStep) {
            lastReachedStepIndex++;

            var remaining = steps.subList(lastReachedStepIndex, steps.size());
            remainingSteps.postValue(remaining);

            var reachedDestination = (remaining.size() <= 1);
            this.reachedDestination.postValue(new Event<>(reachedDestination));
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

    public LiveData<List<StepModel>> getRemainingSteps() {
        return remainingSteps;
    }

    public LiveData<Event<Boolean>> getReachedDestination() {
        return reachedDestination;
    }

    public LiveData<LocationModel> getUserLocation() {
        return userLocation;
    }
}
