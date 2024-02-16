package ir.roudi.littleneshan.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import dagger.hilt.android.AndroidEntryPoint;
import ir.roudi.littleneshan.R;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionLocationIfNotGranted();
    }

    private void requestPermissionLocationIfNotGranted() {
        if(!isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityResultLauncher<String> result = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    // TODO: Handle later
                    Toast.makeText(
                            MainActivity.this,
                            "Location Permission Status: " + isGranted,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
            result.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

}