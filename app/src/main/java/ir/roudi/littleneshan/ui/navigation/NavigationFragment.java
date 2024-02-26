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
        if(args.getStart() != null && args.getEnd() != null) {
            viewModel.startNavigation(args.getStart(), args.getEnd());
        }
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

        var destination = viewModel.getDestination();
        if(destination != null) {
            map.markDestinationOnMap(destination);
        }
    }

    private void registerOnMapClickListener() {
        map.setOnMapClickListener(viewModel::setUserLocation);
    }

    private void registerObservers() {
        registerUserLocationObserver();
        registerReachedDestinationObserver();
        registerFocusOnUserLocationObserver();
        registerRemainingStepsObserver();
        registerCurrentStepObserver();
        registerInitialFocusOnUserLocationObserver();
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
        viewModel.getRemainingPointsPath().observe(getViewLifecycleOwner(), routingPoints -> {
            if (routingPoints == null || routingPoints.isEmpty())
                return;

            var userLocation = viewModel.getUserLocation().getValue();
            if(userLocation != null) {
                float bearing = userLocation.bearingTo(routingPoints.get(0));
                map.setBearing(bearing);
            }

            map.showPathOnMap(routingPoints);
            viewModel.focusOnUserLocation();
        });
    }

    private void registerCurrentStepObserver() {
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), step -> {
            if(step == null)
                return;

            var text = "بعدی: " + step.getName() + "\n" + step.getDistance().getText() + " دیگر " + step.getInstruction();
            binding.address.setText(text);
        });
    }

    private void registerInitialFocusOnUserLocationObserver() {
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
        LocalBroadcastManager.getInstance(requireContext())
                        .unregisterReceiver(stopNavigationForegroundService);

        super.onDestroy();
    }
}
