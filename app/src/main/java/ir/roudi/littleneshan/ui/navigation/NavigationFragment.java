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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.ResolvableApiException;

import ir.roudi.littleneshan.BuildConfig;
import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.BaseFragment;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnLocationResultListener;
import ir.roudi.littleneshan.databinding.FragmentNavigationBinding;
import ir.roudi.littleneshan.service.NavigationForegroundService;
import ir.roudi.littleneshan.ui.MainActivity;
import ir.roudi.littleneshan.utils.LiveDataUtils;

public class NavigationFragment extends BaseFragment<FragmentNavigationBinding, NavigationViewModel> {

    public static final String ACTION_STOP_NAVIGATION_SERVICE = BuildConfig.APPLICATION_ID + ".ACTION_STOP_NAVIGATION_SERVICE";

    private NavigationMap map;

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

        startLocationUpdates();

        registerBroadcastReceiver();

        NavigationForegroundService.startService(getContext());

        startNavigation();
    }

    private void startLocationUpdates() {
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
    }

    private void registerBroadcastReceiver() {
        var intentFilter = new IntentFilter(ACTION_STOP_NAVIGATION_SERVICE);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(
                        stopNavigationForegroundService,
                        intentFilter
                );
    }

    private void startNavigation() {
        var args = NavigationFragmentArgs.fromBundle(getArguments());
        viewModel.startNavigation(args.getStart(), args.getEnd());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setViewmodel(viewModel);

        setupNavigationMap();

        registerOnMapClickListener();

        registerObservers();
    }

    private void setupNavigationMap() {
        var args = NavigationFragmentArgs.fromBundle(getArguments());
        map = new NavigationMap(binding.map, args.getMapStyle());
        map.markDestinationOnMap(args.getEnd());
    }

    private void registerOnMapClickListener() {
        map.setOnMapClickListener(viewModel::setUserLocation);
    }

    private void registerObservers() {
        registerDirectionObserver();
        registerUserLocationObserver();
        registerReachedDestinationObserver();
        registerFocusOnUserLocationObserver();
        registerRemainingStepsObserver();
        registerInitialFocusOnUserLocation();
    }

    private void registerDirectionObserver() {
        viewModel.getDirection().observe(getViewLifecycleOwner(), direction -> {
            if (direction == null)
                return;

            binding.distance.setText(direction.getDistance().getText());

            binding.duration.setText(direction.getDuration().getText());

            var steps = direction.getSteps();

            if (steps == null || steps.isEmpty())
                return;

            var firstStepOfNavigation = steps.get(0);
            var text = "بعدی: " + firstStepOfNavigation.getName() + "\n" + firstStepOfNavigation.getDistance().getText() + " دیگر " + firstStepOfNavigation.getInstruction();
            binding.address.setText(text);

            LiveDataUtils.firstNonNull(viewModel.getUserLocation(), getViewLifecycleOwner(), userLocation -> {
                var startPointOfNavigationPath = firstStepOfNavigation.getStartPoint();

                var bearing = startPointOfNavigationPath.bearingTo(userLocation);
                var distance = userLocation.distanceTo(startPointOfNavigationPath);

                var thereIsMoreThanOneStep = (steps.size() > 1);
                var userIsTooCloseToStartPoint = (distance < 5);
                if(userIsTooCloseToStartPoint && thereIsMoreThanOneStep) {
                    var secondPointOfNavigationPath = steps.get(1).getStartPoint();
                    bearing = startPointOfNavigationPath.bearingTo(secondPointOfNavigationPath);
                }

                map.setBearing(bearing);
            });
        });
    }

    private void registerUserLocationObserver() {
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), userLocation -> {
            if (userLocation == null)
                return;

            viewModel.updateUserProgress();

            map.markUserOnMap(userLocation);
        });
    }

    private void registerReachedDestinationObserver() {
        viewModel.getReachedDestination().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(reachedDestination -> {
                if (reachedDestination) {
                    Toast.makeText(getContext(), "به مقصد رسیدید!", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        findNavController(NavigationFragment.this).navigateUp();
                    }, 3000);
                }
            });
        });
    }

    private void registerFocusOnUserLocationObserver() {
        viewModel.getFocusOnUserLocationEvent().observe(getViewLifecycleOwner(), event -> {
            event.doIfNotHandled(userLocation -> {
                map.focusOnLocation(userLocation);
            });
        });
    }

    private void registerRemainingStepsObserver() {
        viewModel.getRemainingSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps == null)
                return;

            map.showRemainingPathOnMap(steps);
            viewModel.focusOnUserLocation();
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
    public void onDestroy() {

        NavigationForegroundService.stopService(getContext());

        LocalBroadcastManager.getInstance(requireContext())
                        .unregisterReceiver(stopNavigationForegroundService);

        super.onDestroy();
    }
}
