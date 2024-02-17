package ir.roudi.littleneshan.ui.navigation;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;

import org.neshan.mapsdk.internal.utils.BitmapUtils;
import org.neshan.mapsdk.model.Marker;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;
import ir.roudi.littleneshan.databinding.FragmentNavigationBinding;
import ir.roudi.littleneshan.utils.LittleNeshanBitmapUtils;

public class NavigationFragment extends Fragment {

    private FragmentNavigationBinding binding;
    private NavigationViewModel viewModel;

    private Marker userLocationMarker;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();

        viewModel.startLocationUpdates(new OnTurnOnGpsCallback() {
            @Override
            public void turnOnGps(Exception exception) {
                // TODO
            }
        });

       var args = NavigationFragmentArgs.fromBundle(getArguments());
       viewModel.startNavigation(args.getStart(), args.getEnd());
    }

    private void setupViewModel() {
        var backStackEntry = findNavController(this)
                .getBackStackEntry(R.id.nav_main);

        var factory = HiltViewModelFactory.create(requireContext(), backStackEntry);

        viewModel = new ViewModelProvider(backStackEntry, factory)
                .get(NavigationViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var args = NavigationFragmentArgs.fromBundle(getArguments());

        setupMapSetting(args.getMapStyle());

        binding.stop.setOnClickListener(v -> {
            findNavController(this).navigateUp();
        });

        viewModel.direction.observe(getViewLifecycleOwner(), direction -> {
            if(direction == null)
                return;

            binding.distance.setText(direction.getDistance().getText());

            binding.duration.setText(direction.getDuration().getText());

            if(direction.getSteps() == null || direction.getSteps().isEmpty())
                return;

            var nextStep = direction.getSteps().get(0);
            var text =  "بعدی: " + nextStep.getName() + "\n" + nextStep.getDistance().getText() + " دیگر " + nextStep.getInstruction();
            binding.address.setText(text);
        });

        viewModel.userLocation.observe(getViewLifecycleOwner(), userLocation -> {
            if(userLocation == null)
                return;

            updateLocationMarker(userLocation);
        });
    }

    private void updateLocationMarker(LocationModel location) {
        if(userLocationMarker != null) {
            binding.map.removeMarker(userLocationMarker);
        }

        userLocationMarker = new Marker(location.toLatLng(), createMarkerStyle());

        binding.map.addMarker(userLocationMarker);
    }

    private MarkerStyle createMarkerStyle() {
        var markerBuilder = new MarkerStyleBuilder();
        markerBuilder.setSize(30f);

        var drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker);
        if(drawable != null) {
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
        binding.map.setTilt(40f, 0.25f);
    }

    private void focusOnLocation(LocationModel location) {
        binding.map.moveCamera(location.toLatLng(), 0.5f);
        if (binding.map.getZoom() != 18f) {
            binding.map.setZoom(18f, 0.5f);
        }
    }

}
