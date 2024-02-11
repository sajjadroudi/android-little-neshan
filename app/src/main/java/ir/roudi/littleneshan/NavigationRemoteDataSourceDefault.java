package ir.roudi.littleneshan;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NavigationRemoteDataSourceDefault implements NavigationRemoteDataSource {

    private final NeshanService service;

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
