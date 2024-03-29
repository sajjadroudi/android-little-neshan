package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.Manifest;
import android.app.AlertDialog;
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
import ir.roudi.littleneshan.service.NavigationForegroundService;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.ui.navigation.NavigationFragmentArgs;
import ir.roudi.littleneshan.utils.LiveDataUtils;
import ir.roudi.littleneshan.utils.PermissionUtils;

public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> {

    private MainMap map;

    @Override
    public Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigateToNavigationScreenIfNeeded();
    }

    private void navigateToNavigationScreenIfNeeded() {
        if(NavigationForegroundService.isRunning(getContext())) {
            viewModel.navigateToNavigationScreen();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        startLocationUpdates(false, false);

        registerOnMapClickListeners();
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

    private void registerOnMapClickListeners() {
        registerOnMapClickListener();
        registerOnMapLongClickListener();
    }

    private void registerOnMapClickListener() {
        map.setOnMapClickListener(viewModel::setUserLocation);
    }

    private void registerOnMapLongClickListener() {
        map.setOnMapLongClickListener(destination -> {
            map.markDestinationOnMap(destination);
            viewModel.navigate(destination);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = new MainMap(binding.map);

        binding.setViewmodel(viewModel);

        registerObservers();

    }

    private void registerObservers() {
        registerUserLocationObserver();
        registerSwitchThemeObserver();
        registerFocusOnUserLocationObserver();
        registerNavigationPathObserver();
        registerNavigateToDestinationDetailsBottomSheet();
        registerWhenNavigateToNavigationScreen();
        registerNavigateToNavigationScreen();
        registerInitialFocusOnUserLocation();
    }

    private void registerUserLocationObserver() {
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            map.markUserOnMap(location.getLocation(), location.isCached());
        });
    }

    private void registerSwitchThemeObserver() {
        viewModel.getSwitchThemeEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(switchTheme -> {
                if(switchTheme) {
                    map.switchTheme();

                    var icon = map.isNightTheme() ? R.drawable.ic_night : R.drawable.ic_light;
                    binding.btnTheme.setImageResource(icon);
                }
            });
        });
    }

    private void registerFocusOnUserLocationObserver() {
        viewModel.getFocusOnUserLocationEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(focusOnUserLocation -> {
                requestLocationPermission();

                var location = viewModel.getUserLocation().getValue();
                if (location != null) {
                    showLocation(location.getLocation(), location.isCached());
                }
            });
        });
    }

    private void showLocation(LocationModel location, boolean isCachedLocation) {
        map.focusOnLocation(location);
        map.markUserOnMap(location, isCachedLocation);
    }

    private void registerNavigationPathObserver() {
        viewModel.getNavigationPath().observe(getViewLifecycleOwner(), pathEvent -> {
            pathEvent.doIfNotHandled(navigationPath -> {
                var start = viewModel.getStartLocation();
                var end = viewModel.getEndLocation();
                if(start != null && end != null) {
                    map.showPathOnMap(navigationPath, start, end);
                }
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
        var currentBackStackEntry = findNavController(this)
                .getCurrentBackStackEntry();

        if(currentBackStackEntry == null)
            return;

        currentBackStackEntry
                .getSavedStateHandle()
                .getLiveData(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION)
                .observe(getViewLifecycleOwner(), object -> {
                    if (object instanceof Boolean) {
                        boolean doesStartNavigation = (Boolean) object;
                        if (doesStartNavigation) {
                            viewModel.navigateToNavigationScreen();
                        } else {
                            viewModel.clearNavigationData();
                            map.clear();
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

                var backStackEntry = navController.getCurrentBackStackEntry();

                if(backStackEntry != null) {
                    backStackEntry
                        .getSavedStateHandle()
                        .remove(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION);
                }

                var args = new NavigationFragmentArgs.Builder()
                        .setStart(viewModel.getStartLocation())
                        .setEnd(viewModel.getEndLocation())
                        .setMapStyle(map.getMapStyle())
                        .build()
                        .toBundle();

                navController.navigate(R.id.navigation_destination, args);
            });
        });
    }

    private void registerInitialFocusOnUserLocation() {
        LiveDataUtils.observeOnce(
                viewModel.getUserLocation(),
                getViewLifecycleOwner(),
                userLocation -> {
                    viewModel.focusOnUserLocation();
                }
        );
    }

    @Override
    public void onStop() {
        viewModel.stopLocationUpdates();
        super.onStop();
    }

    private void requestLocationPermission() {
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates(true, true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        var isPermissionPermanentlyDenied = PermissionUtils.isPermissionPermanentlyDenied(
                                MainFragment.this, response.getPermissionName()
                        );

                        var isPermissionGranted = PermissionUtils.isPermissionGranted(
                                getContext(), response.getPermissionName()
                        );

                        if(isPermissionGranted) {
                            startLocationUpdates(true, true);
                        } else if(isPermissionPermanentlyDenied) {
                            showGrantLocationPermission();
                        }
                    }

                    private void showGrantLocationPermission() {
                        new AlertDialog.Builder(getContext())
                                .setTitle("مجوز دسترسی به موقعیت")
                                .setMessage("جهت عملکرد صحیح نشان کوچولو، به دسترسی موقعیت نیاز داریم.")
                                .setPositiveButton("تنظیمات", (dialog, which) -> openSettings())
                                .setNegativeButton("لغو", null)
                                .create()
                                .show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    private void openSettings() {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setPackage(BuildConfig.APPLICATION_ID);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .check();
    }

}