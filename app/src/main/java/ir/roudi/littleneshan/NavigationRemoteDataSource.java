package ir.roudi.littleneshan;

import io.reactivex.rxjava3.core.Single;

public interface NavigationRemoteDataSource {
    Single<AddressResponse> getAddress(double lat, double lng);

    Single<DirectionResponse> getDirection(
            double sourceLat,
            double sourceLng,
            double destinationLat,
            double destinationLng,
            int bearing
    );
}
