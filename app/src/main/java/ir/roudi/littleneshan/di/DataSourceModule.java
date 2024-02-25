package ir.roudi.littleneshan.di;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ir.roudi.littleneshan.data.remote.NavigationRemoteDataSource;
import ir.roudi.littleneshan.data.remote.NavigationRemoteDataSourceDefault;

@Module
@InstallIn(SingletonComponent.class)
public abstract class DataSourceModule {

    @Binds
    @Singleton
    public abstract NavigationRemoteDataSource provideNavigationRemoteDataSource(
            NavigationRemoteDataSourceDefault navigationRemoteDataSource
    );

}
