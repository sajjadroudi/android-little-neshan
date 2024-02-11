package ir.roudi.littleneshan.data.remote;

import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.remote.model.AddressResponse;
import ir.roudi.littleneshan.data.remote.model.DirectionResponse;

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
