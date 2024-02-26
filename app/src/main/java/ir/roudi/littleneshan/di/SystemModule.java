package ir.roudi.littleneshan.di;

import android.app.NotificationManager;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class SystemModule {

    @Provides
    public static NotificationManager provideNotificationManager(
            @ApplicationContext Context context
    ) {
        return context.getSystemService(NotificationManager.class);
    }

}
