package ir.roudi.littleneshan.di;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.LocationRepositoryDefault;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepositoryDefault;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    // TODO: Check di modules to choose the best components to install in

    @Binds
    public abstract LocationRepository bindLocationRepository(
            LocationRepositoryDefault locationRepository
    );

    @Binds
    public abstract NavigationRepository bindNavigationRepository(
            NavigationRepositoryDefault navigationRepository
    );


}
