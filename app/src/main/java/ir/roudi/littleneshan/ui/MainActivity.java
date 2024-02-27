package ir.roudi.littleneshan.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.utils.PermissionUtils;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_SETTING_REQUEST_CODE = 123;

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {}
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionNotificationIfNeeded();
    }

    private void requestPermissionNotificationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var permission = Manifest.permission.POST_NOTIFICATIONS;
            var notifPermissionIsNotGranted = !PermissionUtils.isPermissionGranted(this, permission);
            if(notifPermissionIsNotGranted) {
                notificationPermissionLauncher.launch(permission);
            }
        }
    }

}