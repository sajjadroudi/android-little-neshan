package ir.roudi.littleneshan.di;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class OsModule {

    public static FusedLocationProviderClient provideLocationClient(
            @ApplicationContext Context context
    ) {
        return LocationServices.getFusedLocationProviderClient(context);
    }

    public static SettingsClient provideLocationSettingsClient(
            @ApplicationContext Context context
    ) {
        return LocationServices.getSettingsClient(context);
    }

}
