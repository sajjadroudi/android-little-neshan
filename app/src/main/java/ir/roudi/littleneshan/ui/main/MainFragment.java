package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.view.View;

import com.carto.core.ScreenBounds;
import com.carto.core.ScreenPos;
import com.carto.graphics.Color;
import com.carto.styles.LineStyle;
import com.carto.styles.LineStyleBuilder;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.neshan.common.model.LatLng;
import org.neshan.common.model.LatLngBounds;
import org.neshan.common.utils.PolylineEncoding;
import org.neshan.mapsdk.MapView;
import org.neshan.mapsdk.model.Marker;
import org.neshan.mapsdk.model.Polyline;
import org.neshan.mapsdk.style.NeshanMapStyle;

import java.util.ArrayList;

import ir.roudi.littleneshan.BuildConfig;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseFragment;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.databinding.FragmentMainBinding;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.ui.navigation.NavigationFragmentArgs;
import ir.roudi.littleneshan.utils.LittleNeshanBitmapUtils;

public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> {

    private final OnTurnOnLocationResultListener locationSettingsResultListener = new OnTurnOnLocationResultListener() {

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
    };

    private Marker userMarker;
    private Marker destinationMarker;
    private Polyline routingPathPolyLine;

    @Override
    public Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.startLocationUpdates(locationSettingsResultListener);

        // TODO: Maybe it's better to observe only the first item.
        viewModel.userLocation.observe(getViewLifecycleOwner(), location -> {
            showLocation(location.getLocation(), location.isCached());
        });

        binding.btnLocation.setOnClickListener(v -> {
            viewModel.startLocationUpdates(locationSettingsResultListener);

            var location = viewModel.userLocation.getValue();
            if (location != null) {
                showLocation(location.getLocation(), location.isCached());
            }
        });

        binding.btnTheme.setOnClickListener(v -> {
            boolean isNightMode = (binding.map.getMapStyle() == NeshanMapStyle.NESHAN);

            var newTheme = isNightMode ? NeshanMapStyle.NESHAN_NIGHT : NeshanMapStyle.NESHAN;
            binding.map.setMapStyle(newTheme);

            var icon = isNightMode ? R.drawable.ic_light : R.drawable.ic_night;
            binding.btnTheme.setImageResource(icon);

            // TODO: Change color of buttons
        });

        binding.map.setOnMapLongClickListener(new MapView.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                markDestinationOnMap(LocationModel.from(latLng));
            }
        });

        viewModel.navigationPath.observe(getViewLifecycleOwner(), pathEvent -> {
            pathEvent.doIfNotHandled(this::showPathOnMap);
        });

        viewModel.address.observe(getViewLifecycleOwner(), addressEvent -> {
            addressEvent.doIfNotHandled(address -> {
                var bundle = new DestinationDetailsBottomSheetArgs.Builder(
                        address.getTitle(),
                        address.getDuration(),
                        address.getDistance(),
                        address.getAddress()
                )
                        .build()
                        .toBundle();

                findNavController(MainFragment.this)
                        .navigate(R.id.destination_detail, bundle);
            });
        });
    }

    // TODO: Put delay to improve UX
    private void showPathOnMap(String pathString) {
        if (routingPathPolyLine != null) {
            binding.map.removePolyline(routingPathPolyLine);
        }

        var path = PolylineEncoding.decode(pathString);

        routingPathPolyLine = new Polyline(new ArrayList<>(path), buildLineStyle());
        binding.map.addPolyline(routingPathPolyLine);

        // setup map camera to show whole path
        var latLngBounds = new LatLngBounds(
                viewModel.startLocation.toLatLng(),
                viewModel.endLocation.toLatLng()
        );
        float mapWidth = Math.min(binding.map.getWidth(), binding.map.getHeight());
        var screenBounds = new ScreenBounds(
                new ScreenPos(0F, 0F),
                new ScreenPos(mapWidth, mapWidth)
        );
        binding.map.moveToCameraBounds(latLngBounds, screenBounds, true, 0.5f);
    }

    private LineStyle buildLineStyle() {
        var lineStCr = new LineStyleBuilder();
        var color = new Color(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDim75));
        lineStCr.setColor(color);
        lineStCr.setWidth(10f);
        lineStCr.setStretchFactor(0f);
        return lineStCr.buildStyle();
    }

    private void showLocation(LocationModel location, boolean isCachedLocation) {
        focusOnLocation(location);
        markUserOnMap(location, isCachedLocation);
    }

    private void focusOnLocation(LocationModel location) {
        binding.map.moveCamera(location.toLatLng(), 0.25f);
        binding.map.setZoom(15, 0.25f);
    }

    private void markUserOnMap(LocationModel location, boolean isCachedLocation) {
        if (userMarker != null) {
            binding.map.removeMarker(userMarker);
        }
        int icon = isCachedLocation ? R.drawable.ic_marker_off : R.drawable.ic_marker;
        userMarker = new Marker(location.toLatLng(), buildUserMarkerStyle(icon));
        binding.map.addMarker(userMarker);
    }

    private MarkerStyle buildUserMarkerStyle(int iconResource) {
        var markStCr = new MarkerStyleBuilder();
        markStCr.setSize(30f);
        var drawable = ContextCompat.getDrawable(requireContext(), iconResource);
        if (drawable != null) {
            var markerBitmap = BitmapUtils.createBitmapFromAndroidBitmap(
                    LittleNeshanBitmapUtils.toBitmap(drawable)
            );
            markStCr.setBitmap(markerBitmap);
        }
        return markStCr.buildStyle();
    }

    private void markDestinationOnMap(LocationModel location) {
        if (destinationMarker != null) {
            binding.map.removeMarker(destinationMarker);
        }

        var style = buildUserMarkerStyle(R.drawable.ic_destination);
        destinationMarker = new Marker(location.toLatLng(), style);

        binding.map.addMarker(destinationMarker);

        viewModel.startLocation = viewModel.userLocation.getValue().getLocation();
        viewModel.endLocation = location;
        viewModel.navigate();
    }

    @Override
    public void onPause() {
        viewModel.stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        var navController = findNavController(this);

        navController
                .getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION)
                .observe(getViewLifecycleOwner(), o -> {
                    if (o instanceof Boolean) {
                        boolean doesStartNavigation = (Boolean) o;
                        if(doesStartNavigation) {
                            viewModel.navigateToNavigationScreen();
                        }
                    }
                });

        viewModel.navigateToNavigationScreen.observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(content -> {
                navController.getCurrentBackStackEntry()
                        .getSavedStateHandle()
                        .remove(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION);

                var args = new NavigationFragmentArgs.Builder(
                        binding.map.getMapStyle(),
                        viewModel.startLocation,
                        viewModel.endLocation
                )
                        .build()
                        .toBundle();

                removeMapObjects();

                navController.navigate(R.id.navigation_destination, args);
            });
        });

    }

    private void removeMapObjects() {
        viewModel.endLocation = null;

        if (routingPathPolyLine != null) {
            binding.map.removePolyline(routingPathPolyLine);
        }

        if(userMarker != null) {
            binding.map.removeMarker(userMarker);
        }

        if(destinationMarker != null) {
            binding.map.removeMarker(destinationMarker);
        }

        focusOnLocation(viewModel.startLocation);
    }

    @Override
    public void onResume() {
        super.onResume();

        requestLocationPermissionIfNeeded();
    }

    private void requestLocationPermissionIfNeeded() {
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        viewModel.startLocationUpdates(locationSettingsResultListener);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    private void openSettings() {
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .check();
    }

}