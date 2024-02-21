package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.carto.core.ScreenBounds;
import com.carto.core.ScreenPos;
import com.google.android.gms.common.api.ResolvableApiException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.neshan.common.model.LatLngBounds;
import org.neshan.common.utils.PolylineEncoding;
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
import ir.roudi.littleneshan.utils.LineUtils;
import ir.roudi.littleneshan.utils.MarkerUtils;

public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> {

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

        startLocationUpdates(false, false);

        registerObservers();

        registerOnMapClickListener();
    }

    private void startLocationUpdates(boolean showTurnOnLocationDialog, boolean showError) {
        viewModel.startLocationUpdates(new OnTurnOnLocationResultListener() {

            @Override
            public void onRequireResolution(ResolvableApiException exception) throws IntentSender.SendIntentException {
                if(showTurnOnLocationDialog) {
                    exception.startResolutionForResult(
                            getActivity(),
                            MainActivity.LOCATION_SETTING_REQUEST_CODE
                    );
                }
            }

            @Override
            public void onSettingsChangeUnavailable() {
                if(showError) {
                    viewModel.showError(R.string.inadequate_location_settings);
                }
            }

            @Override
            public void onSendIntentException(IntentSender.SendIntentException exception) {
                if(showError) {
                    viewModel.showError(R.string.something_went_wrong);
                }
            }
        });
    }

    private void registerObservers() {
        registerUserLocationObserver();
        registerSwitchThemeObserver();
        registerFocusOnUserLocationObserver();
        registerNavigationPathObserver();
        registerNavigateToDestinationDetailsBottomSheet();
        registerWhenNavigateToNavigationScreen();
        registerNavigateToNavigationScreen();
    }

    private void registerUserLocationObserver() {
        // TODO: Maybe it's better to observe only the first item.
        viewModel.userLocation.observe(getViewLifecycleOwner(), location -> {
            showLocation(location.getLocation(), location.isCached());
        });
    }

    private void registerSwitchThemeObserver() {
        viewModel.getSwitchThemeEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(switchTheme -> {
                if(switchTheme) {
                    boolean isNightMode = (binding.map.getMapStyle() == NeshanMapStyle.NESHAN);

                    var newTheme = isNightMode ? NeshanMapStyle.NESHAN_NIGHT : NeshanMapStyle.NESHAN;
                    binding.map.setMapStyle(newTheme);

                    var icon = isNightMode ? R.drawable.ic_light : R.drawable.ic_night;
                    binding.btnTheme.setImageResource(icon);

                    // TODO: Change color of buttons
                }
            });
        });
    }

    private void registerFocusOnUserLocationObserver() {
        viewModel.getFocusOnUserLocationEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(focusOnUserLocation -> {
                requestLocationPermission(true, true, true);

                var location = viewModel.userLocation.getValue();
                if (location != null) {
                    showLocation(location.getLocation(), location.isCached());
                }
            });
        });
    }

    private void registerNavigationPathObserver() {
        viewModel.navigationPath.observe(getViewLifecycleOwner(), pathEvent -> {
            pathEvent.doIfNotHandled(this::showPathOnMap);
        });
    }

    private void registerNavigateToDestinationDetailsBottomSheet() {
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

    private void registerWhenNavigateToNavigationScreen() {
        findNavController(this)
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
    }

    private void registerNavigateToNavigationScreen() {
        viewModel.navigateToNavigationScreen.observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(content -> {
                var navController = findNavController(MainFragment.this);

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

    private void registerOnMapClickListener() {
        binding.map.setOnMapLongClickListener(latLng -> {
            markDestinationOnMap(LocationModel.from(latLng));
        });
    }

    // TODO: Put delay to improve UX
    private void showPathOnMap(String pathString) {
        if (routingPathPolyLine != null) {
            binding.map.removePolyline(routingPathPolyLine);
        }

        var path = PolylineEncoding.decode(pathString);

        routingPathPolyLine = new Polyline(new ArrayList<>(path), LineUtils.buildLineStyle(getContext()));
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
        userMarker = new Marker(location.toLatLng(), MarkerUtils.buildMarkerStyle(getContext(), icon));
        binding.map.addMarker(userMarker);
    }

    private void markDestinationOnMap(LocationModel location) {
        if (destinationMarker != null) {
            binding.map.removeMarker(destinationMarker);
        }

        var style = MarkerUtils.buildMarkerStyle(getContext(), R.drawable.ic_destination);
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
        binding.setViewmodel(viewModel);
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

        requestLocationPermission(false, false, false);
    }

    private void requestLocationPermission(
            boolean openSettings, boolean showTurnOnLocationDialog, boolean showError
    ) {
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates(showTurnOnLocationDialog, showError);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied() && openSettings) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    private void openSettings() {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .check();
    }

}