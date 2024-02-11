package ir.roudi.littleneshan;

import io.reactivex.rxjava3.core.Single;

public interface NavigationRepository {
    Single<AddressModel> getAddress(LocationModel location);

    Single<DirectionModel> getDirection(
            LocationModel source,
            LocationModel destination,
            int bearing
    );
}
