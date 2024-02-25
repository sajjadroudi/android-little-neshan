package ir.roudi.littleneshan.di;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ir.roudi.littleneshan.core.Config;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.location.LocationRepositoryDefault;
import ir.roudi.littleneshan.data.repository.location.LocationRepositoryFake;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepositoryDefault;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Provides
    @Singleton
    public static LocationRepository provideLocationRepository(
            LocationRepositoryDefault defaultLocationRepo,
            LocationRepositoryFake fakeLocationRepo
    ) {
        if(Config.USE_FAKE_USER_LOCATION) {
            return fakeLocationRepo;
        } else {
            return defaultLocationRepo;
        }
    }

    @Binds
    @Singleton
    public abstract NavigationRepository bindNavigationRepository(
            NavigationRepositoryDefault navigationRepository
    );

}
