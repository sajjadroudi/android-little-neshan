package ir.roudi.littleneshan.ui.navigation;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.carto.graphics.Color;
import com.carto.styles.LineStyle;
import com.carto.styles.LineStyleBuilder;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.google.android.gms.common.api.ResolvableApiException;

import org.neshan.common.utils.PolylineEncoding;
import org.neshan.mapsdk.internal.utils.BitmapUtils;
import org.neshan.mapsdk.model.Marker;
import org.neshan.mapsdk.model.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ir.roudi.littleneshan.BuildConfig;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseFragment;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.databinding.FragmentNavigationBinding;
import ir.roudi.littleneshan.service.NavigationForegroundService;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.utils.LittleNeshanBitmapUtils;

public class NavigationFragment extends BaseFragment<FragmentNavigationBinding, NavigationViewModel> {

    private Marker userLocationMarker;
    private Polyline remainingPathPolyline;

    public static final String ACTION_STOP_NAVIGATION_SERVICE = BuildConfig.APPLICATION_ID + ".ACTION_STOP_NAVIGATION_SERVICE";

    private final BroadcastReceiver stopNavigationForegroundService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_STOP_NAVIGATION_SERVICE.equals(intent.getAction())) {
                viewModel.navigateUp();
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.fragment_navigation;
    }

    @Override
    public Class<NavigationViewModel> getViewModelClass() {
        return NavigationViewModel.class;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.startLocationUpdates(new OnTurnOnLocationResultListener() {

            @Override
            public void onRequireResolution(ResolvableApiException exception) throws IntentSender.SendIntentException {
                exception.startResolutionForResult(getActivity(), MainActivity.LOCATION_SETTING_REQUEST_CODE);
            }

            @Override
            public void onSettingsChangeUnavailable() {
                viewModel.showError(R.string.inadequate_location_settings);
            }

            @Override
            public void onSendIntentException(IntentSender.SendIntentException exception) {
                viewModel.showError(R.string.something_went_wrong);
            }
        });

        var args = NavigationFragmentArgs.fromBundle(getArguments());
        viewModel.startNavigation(args.getStart(), args.getEnd());

        registerBroadcastReceiver();

        NavigationForegroundService.startService(getContext());
    }

    private void registerBroadcastReceiver() {
        var intentFilter = new IntentFilter(ACTION_STOP_NAVIGATION_SERVICE);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(
                        stopNavigationForegroundService,
                        intentFilter
                );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var args = NavigationFragmentArgs.fromBundle(getArguments());

        setupMapSetting(args.getMapStyle());

        binding.stop.setOnClickListener(v -> {
            viewModel.navigateUp();
        });

        viewModel.direction.observe(getViewLifecycleOwner(), direction -> {
            if (direction == null)
                return;

            binding.distance.setText(direction.getDistance().getText());

            binding.duration.setText(direction.getDuration().getText());

            if (direction.getSteps() == null || direction.getSteps().isEmpty())
                return;

            var nextStep = direction.getSteps().get(0);
            var text = "بعدی: " + nextStep.getName() + "\n" + nextStep.getDistance().getText() + " دیگر " + nextStep.getInstruction();
            binding.address.setText(text);
        });

        viewModel.userLocation.observe(getViewLifecycleOwner(), userLocation -> {
            if (userLocation == null)
                return;

            viewModel.updateUserProgress();

            updateLocationMarker(userLocation);
        });

        viewModel.reachedDestination.observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(reachedDestination -> {
                if (reachedDestination) {
                    Toast.makeText(getContext(), "به مقصد رسیدید!", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        findNavController(NavigationFragment.this).navigateUp();
                    }, 3000);
                }
            });
        });

        viewModel.remainingSteps.observe(getViewLifecycleOwner(), steps -> {
            if (steps == null)
                return;

            updatePathOnMap(steps);
        });

    }

    private void updatePathOnMap(List<StepModel> remainingSteps) {
        if (remainingPathPolyline != null) {
            binding.map.removePolyline(remainingPathPolyline);
        }

        var pointsOfRemainingPath = remainingSteps.stream()
                .map(step -> PolylineEncoding.decode(step.getEncodedPolyline()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        remainingPathPolyline = new Polyline(new ArrayList<>(pointsOfRemainingPath), buildLineStyle());

        binding.map.addPolyline(remainingPathPolyline);

        var userLocation = viewModel.userLocation.getValue();
        if (userLocation != null) {
            focusOnLocation(userLocation);
        }
    }

    private LineStyle buildLineStyle() {
        var builder = new LineStyleBuilder();
        builder.setColor(new Color(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDim75)));
        builder.setWidth(10f);
        builder.setStretchFactor(0f);
        return builder.buildStyle();
    }

    private void updateLocationMarker(LocationModel location) {
        if (userLocationMarker != null) {
            binding.map.removeMarker(userLocationMarker);
        }

        userLocationMarker = new Marker(location.toLatLng(), createMarkerStyle());

        binding.map.addMarker(userLocationMarker);
    }

    private MarkerStyle createMarkerStyle() {
        var markerBuilder = new MarkerStyleBuilder();
        markerBuilder.setSize(30f);

        var drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker);
        if (drawable != null) {
            var markerBitmap = BitmapUtils.createBitmapFromAndroidBitmap(
                    LittleNeshanBitmapUtils.toBitmap(drawable)
            );
            markerBuilder.setBitmap(markerBitmap);
        }

        return markerBuilder.buildStyle();
    }

    private void setupMapSetting(int mapStyle) {
        binding.map.setMapStyle(mapStyle);
        binding.map.setTrafficEnabled(true);
        binding.map.setPoiEnabled(true);
        binding.map.setTilt(40f, 0f);
    }

    private void focusOnLocation(LocationModel location) {
        binding.map.moveCamera(location.toLatLng(), 0.5f);
        if (binding.map.getZoom() != 18f) {
            binding.map.setZoom(18f, 0.5f);
        }
    }

    @Override
    public void onDestroy() {

        NavigationForegroundService.stopService(getContext());

        LocalBroadcastManager.getInstance(requireContext())
                        .unregisterReceiver(stopNavigationForegroundService);

        super.onDestroy();
    }
}
