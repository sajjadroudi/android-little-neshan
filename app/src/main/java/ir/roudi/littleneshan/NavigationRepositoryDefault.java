package ir.roudi.littleneshan;

import io.reactivex.rxjava3.core.Single;

public class NavigationRepositoryDefault implements NavigationRepository {

    private final NavigationRemoteDataSource remoteDataSource;

    public NavigationRepositoryDefault(NavigationRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
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

}
