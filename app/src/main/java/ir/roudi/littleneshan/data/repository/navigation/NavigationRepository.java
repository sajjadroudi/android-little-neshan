package ir.roudi.littleneshan.data.repository.navigation;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.model.AddressModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.ui.navigation.NavigationPointModel;

public interface NavigationRepository {
    Single<AddressModel> getAddress(LocationModel location);

    Single<DirectionModel> getDirection(
            LocationModel source,
            LocationModel destination,
            int bearing
    );

    Completable startNavigation(LocationModel start, LocationModel end, LocationModel userLocation);

    LiveData<List<NavigationPointModel>> getRemainingNavigationPoints();

    boolean updateUserProgress(LocationModel userLocation);

    void cleanUpRouting();

    LocationModel getDestination();

}
