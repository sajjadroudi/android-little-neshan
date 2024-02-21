package ir.roudi.littleneshan.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import ir.roudi.littleneshan.R;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public static final int LOCATION_SETTING_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}