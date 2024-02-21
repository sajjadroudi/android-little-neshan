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

import com.google.android.gms.common.api.ResolvableApiException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import ir.roudi.littleneshan.BuildConfig;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseFragment;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.databinding.FragmentMainBinding;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.ui.navigation.NavigationFragmentArgs;

public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> {

    private LittleNeshanMap map;

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
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            showLocation(location.getLocation(), location.isCached());
        });
    }

    private void registerSwitchThemeObserver() {
        viewModel.getSwitchThemeEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(switchTheme -> {
                if(switchTheme) {
                    map.switchTheme();

                    var icon = map.isNightTheme() ? R.drawable.ic_light : R.drawable.ic_night;
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

                var location = viewModel.getUserLocation().getValue();
                if (location != null) {
                    showLocation(location.getLocation(), location.isCached());
                }
            });
        });
    }

    private void registerNavigationPathObserver() {
        viewModel.getNavigationPath().observe(getViewLifecycleOwner(), pathEvent -> {
            pathEvent.doIfNotHandled(navigationPath -> {
                map.showPathOnMap(navigationPath, viewModel.startLocation, viewModel.endLocation);
            });
        });
    }

    private void registerNavigateToDestinationDetailsBottomSheet() {
        viewModel.getDestinationAddress().observe(getViewLifecycleOwner(), addressEvent -> {
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
        viewModel.getNavigateToNavigationScreen().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(navigate -> {
                if(!navigate)
                    return;

                var navController = findNavController(MainFragment.this);

                navController.getCurrentBackStackEntry()
                        .getSavedStateHandle()
                        .remove(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION);

                var args = new NavigationFragmentArgs.Builder(
                        map.getMapStyle(),
                        viewModel.startLocation,
                        viewModel.endLocation
                )
                        .build()
                        .toBundle();

                clearMap();

                navController.navigate(R.id.navigation_destination, args);
            });
        });
    }

    private void registerOnMapClickListener() {
        map.setOnMapLongClickListener(location -> {
            map.markDestinationOnMap(location);

            viewModel.startLocation = viewModel.getUserLocation().getValue().getLocation();
            viewModel.endLocation = location;
            viewModel.navigate();
        });
    }

    private void showLocation(LocationModel location, boolean isCachedLocation) {
        map.focusOnLocation(location);
        map.markUserOnMap(location, isCachedLocation);
    }

    @Override
    public void onPause() {
        viewModel.stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        map = new LittleNeshanMap(binding.map);
        binding.setViewmodel(viewModel);
    }

    private void clearMap() {
        viewModel.endLocation = null;
        map.clear();
        map.focusOnLocation(viewModel.startLocation);
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