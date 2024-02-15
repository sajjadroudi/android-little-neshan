package ir.roudi.littleneshan.data.repository.navigation;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.model.AddressModel;
import ir.roudi.littleneshan.data.model.DirectionModel;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.remote.NavigationRemoteDataSource;
import ir.roudi.littleneshan.data.remote.model.AddressResponse;
import ir.roudi.littleneshan.data.remote.model.DirectionResponse;

public class NavigationRepositoryDefault implements NavigationRepository {

    private final NavigationRemoteDataSource remoteDataSource;

    @Inject
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
