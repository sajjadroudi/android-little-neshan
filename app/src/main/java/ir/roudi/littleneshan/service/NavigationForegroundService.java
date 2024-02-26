package ir.roudi.littleneshan.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ir.roudi.littleneshan.BuildConfig;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.ui.navigation.NavigationFragment;

@AndroidEntryPoint
public class NavigationForegroundService extends Service {

    @Inject
    public NotificationManager notificationManager;

    private static final int NOTIFICATION_ID = 9876;
    private static final String CHANNEL_ID = "little-neshan-navigation-notification-channel";
    private static final String CHANNEL_NAME = "Navigation";
    private static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".STOP_NAVIGATION_SERVICE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        postNotification();
    }

    private void postNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = buildNotificationChannel();
            notificationManager.createNotificationChannel(channel);
        }
        
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("نشان کوچولو در حال مسیریابی")
                .setSmallIcon(R.drawable.ic_navigation)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(buildContentIntent())
                .setOngoing(true)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(0, "پایان", buildEndActionIntent())
                .build();
    }

    private PendingIntent buildContentIntent() {
        var launchActivityIntent = new Intent(this, MainActivity.class);
        launchActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchActivityIntent.setAction(Intent.ACTION_VIEW);

        return PendingIntent.getActivity(
                this,
                0,
                launchActivityIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private PendingIntent buildEndActionIntent() {
        var intent = new Intent(this, NavigationForegroundService.class);
        intent.setAction(ACTION_STOP);
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel buildNotificationChannel() {
        return new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
    }

    public static void startService(Context context) {
        var intent = new Intent(context, NavigationForegroundService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void stopService(Context context) {
        var intent = new Intent(context, NavigationForegroundService.class);
        context.stopService(intent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (var service : manager.getRunningServices(Integer.MAX_VALUE)) {
            var serviceClassName = NavigationForegroundService.class.getName();
            if (serviceClassName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        var intent = new Intent(NavigationFragment.ACTION_STOP_NAVIGATION_SERVICE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

}
