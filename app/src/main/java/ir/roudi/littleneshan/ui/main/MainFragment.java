package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;

import org.neshan.mapsdk.model.Marker;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;
import ir.roudi.littleneshan.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private Marker userMarker;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();
    }

    private void setupViewModel() {
        var backStackEntry = findNavController(this)
                .getBackStackEntry(R.id.nav_main);

        var factory = HiltViewModelFactory.create(requireContext(), backStackEntry);

        viewModel = new ViewModelProvider(backStackEntry, factory)
                .get(MainViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.startLocationUpdates(new OnTurnOnGpsCallback() {
            @Override
            public void turnOnGps(Exception exception) {

            }
        });

        // TODO: Prevent current location from getting replaced by last location

        viewModel.lastLocation.observe(getViewLifecycleOwner(), location -> {
            showLocation(location, true);
        });

        viewModel.currentLocation.observe(getViewLifecycleOwner(), location -> {
            showLocation(location, true);
        });
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
        if(userMarker != null) {
            binding.map.removeMarker(userMarker);
        }
        userMarker = new Marker(location.toLatLng(), buildUserMarkerStyle(isCachedLocation));
        binding.map.addMarker(userMarker);
    }

    private MarkerStyle buildUserMarkerStyle(boolean isCachedLocation) {
        MarkerStyleBuilder markStCr = new MarkerStyleBuilder();
        markStCr.setSize(30f);
        var drawable = isCachedLocation ? R.drawable.ic_marker_off : R.drawable.ic_marker;
        var androidBitmap = BitmapFactory.decodeResource(getResources(), drawable);
        var bitmap = BitmapUtils.createBitmapFromAndroidBitmap(androidBitmap);
        markStCr.setBitmap(bitmap);
        return markStCr.buildStyle();
    }

    @Override
    public void onPause() {
        viewModel.stopLocationUpdates();
        super.onPause();
    }
}