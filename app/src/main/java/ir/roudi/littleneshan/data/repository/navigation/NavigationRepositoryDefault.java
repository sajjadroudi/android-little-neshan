package ir.roudi.littleneshan.data.repository.navigation;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.model.AddressModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.data.remote.NavigationRemoteDataSource;
import ir.roudi.littleneshan.data.remote.model.AddressResponse;
import ir.roudi.littleneshan.data.remote.model.DirectionResponse;
import ir.roudi.littleneshan.service.NavigationForegroundService;
import ir.roudi.littleneshan.ui.navigation.NavigationPointModel;

public class NavigationRepositoryDefault implements NavigationRepository {

    private final Context appContext;

    private final NavigationRemoteDataSource remoteDataSource;

    private final MutableLiveData<List<NavigationPointModel>> remainingNavigationPoints = new MutableLiveData<>(List.of());

    private int lastReachedPointIndex = 0;

    private List<NavigationPointModel> routingPoints = new ArrayList<>();

    private LocationModel destination;

    @Inject
    public NavigationRepositoryDefault(
            NavigationRemoteDataSource remoteDataSource,
            @ApplicationContext Context context
    ) {
        this.remoteDataSource = remoteDataSource;
        this.appContext = context;
    }

    @Override
    public Single<AddressModel> getAddress(LocationModel location) {
        return remoteDataSource.getAddress(location.getLatitude(), location.getLongitude())
                .map(AddressResponse::toAddressModel);
    }

    @Override
    public Single<DirectionModel> getDirection(
            LocationModel source,
            LocationModel destination,
            int bearing
    ) {
        return remoteDataSource.getDirection(
                source.getLatitude(),
                source.getLongitude(),
                destination.getLatitude(),
                destination.getLongitude(),
                bearing
        ).map(DirectionResponse::toDirectionModel);
    }

    @Override
    public Completable startNavigation(LocationModel start, LocationModel end, LocationModel userLocation) {
        destination = end;

        var single = getDirection(start, end, 0)
                .doOnSuccess(direction -> {
                    lastReachedPointIndex = 0;

                    var routingPoints = toRoutingPoints(direction.getSteps());
                    NavigationRepositoryDefault.this.routingPoints = routingPoints;
                    remainingNavigationPoints.postValue(routingPoints);

                    updateUserProgress(userLocation);

                    NavigationForegroundService.startService(appContext);
                });

        return Completable.fromSingle(single);
    }

    @Override
    public LiveData<List<NavigationPointModel>> getRemainingNavigationPoints() {
        return remainingNavigationPoints;
    }

    @Override
    public boolean updateUserProgress(LocationModel userLocation) {
        if(routingPoints == null || routingPoints.size() < 2) {
            return false;
        }

        if(userLocation == null)
            return false;

        var currentPoint = getCurrentPointOfNavigationPath();
        if(currentPoint == null)
            return false;

        var nextPoint = getNextPointOfNavigationPath();
        if(nextPoint == null)
            return false;

        var currentPointToNextPointDistance = currentPoint.distanceTo(nextPoint);
        var currentPointToUserPointDistance = currentPoint.distanceTo(userLocation);

        var userIsPassedCurrentPoint = (currentPointToUserPointDistance >= currentPointToNextPointDistance);
        if(userIsPassedCurrentPoint) {
            lastReachedPointIndex++;

            var remaining = routingPoints.subList(lastReachedPointIndex, routingPoints.size());
            remainingNavigationPoints.postValue(remaining);

            var reachedDestination = (remaining.size() <= 2);

            if(reachedDestination) {
                finishRouting();
            }

            return reachedDestination;
        }

        return false;
    }

    private LocationModel getCurrentPointOfNavigationPath() {
        if(lastReachedPointIndex >= routingPoints.size())
            return null;
        return routingPoints.get(lastReachedPointIndex).getPoint();
    }

    private LocationModel getNextPointOfNavigationPath() {
        var index = lastReachedPointIndex + 1;
        if(index >= routingPoints.size())
            return null;
        return routingPoints.get(index).getPoint();
    }

    @Override
    public void finishRouting() {
        lastReachedPointIndex = 0;
        routingPoints = new ArrayList<>();
        remainingNavigationPoints.postValue(List.of());

        NavigationForegroundService.stopService(appContext);
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

    @Override
    public LocationModel getDestination() {
        return destination;
    }
}
