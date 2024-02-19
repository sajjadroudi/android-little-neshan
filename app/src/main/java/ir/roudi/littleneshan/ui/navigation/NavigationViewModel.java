package ir.roudi.littleneshan.ui.navigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.utils.Event;

@HiltViewModel
public class NavigationViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final NavigationRepository navigationRepository;

    private final MutableLiveData<DirectionModel> _direction = new MutableLiveData<>();
    public final LiveData<DirectionModel> direction = _direction;

    private final MutableLiveData<List<StepModel>> _remainingSteps = new MutableLiveData<>(List.of());
    public final LiveData<List<StepModel>> remainingSteps = _remainingSteps;

    private final MutableLiveData<Event<Boolean>> _reachedDestination = new MutableLiveData<>(new Event<>(false));
    public final LiveData<Event<Boolean>> reachedDestination = _reachedDestination;

    private final MutableLiveData<Event<Boolean>> _navigateUpAction = new MutableLiveData<>(new Event<>(false));
    public final LiveData<Event<Boolean>> navigateUpAction = _navigateUpAction;

    private LocationModel startLocation;
    private LocationModel endLocation;
    private Disposable loadDirectionDisposable;
    public final LiveData<LocationModel> userLocation;
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
                    lastReachedStepIndex = 0;

                    _remainingSteps.postValue(direction.getSteps());

                    _direction.postValue(direction);
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
            _remainingSteps.postValue(remaining);

            var reachedDestination = (remaining.size() <= 1);
            _reachedDestination.postValue(new Event<>(reachedDestination));
        }
    }

    public void navigateUp() {
        _navigateUpAction.postValue(new Event<>(true));
    }

}
