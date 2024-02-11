package ir.roudi.littleneshan.data.repository.navigation;

import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.model.AddressModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;

public interface NavigationRepository {
    Single<AddressModel> getAddress(LocationModel location);

    Single<DirectionModel> getDirection(
            LocationModel source,
            LocationModel destination,
            int bearing
    );
}
