package ir.roudi.littleneshan.data.remote;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ir.roudi.littleneshan.data.remote.model.AddressResponse;
import ir.roudi.littleneshan.data.remote.model.DirectionResponse;

public class NavigationRemoteDataSourceDefault implements NavigationRemoteDataSource {

    private final NeshanService service;

    @Inject
    public NavigationRemoteDataSourceDefault(NeshanService service) {
        this.service = service;
    }

    @Override
    public Single<AddressResponse> getAddress(double lat, double lng) {
        return service.getAddress(lat, lng)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<DirectionResponse> getDirection(
            double sourceLat,
            double sourceLng,
            double destinationLat,
            double destinationLng,
            int bearing
    ) {
        String startPoint = sourceLat + "," + sourceLng;
        String endPoint = destinationLat + "," + destinationLng;
        String type = "car";
        return service.getDirection(type, startPoint, endPoint, bearing)
                .subscribeOn(Schedulers.io());
    }

}
