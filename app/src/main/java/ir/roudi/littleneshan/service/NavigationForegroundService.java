package ir.roudi.littleneshan.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NavigationForegroundService extends Service {

    @Inject
    public NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel buildNotificationChannel() {
        return new NotificationChannel(
                "little-neshan-navigation-notification-channel",
                "Navigation",
                NotificationManager.IMPORTANCE_DEFAULT
        );
    }

}
