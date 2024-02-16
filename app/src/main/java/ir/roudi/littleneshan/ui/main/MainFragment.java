package ir.roudi.littleneshan.ui.main;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.carto.core.ScreenBounds;
import com.carto.core.ScreenPos;
import com.carto.graphics.Color;
import com.carto.styles.LineStyle;
import com.carto.styles.LineStyleBuilder;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;

import org.neshan.common.model.LatLng;
import org.neshan.common.model.LatLngBounds;
import org.neshan.common.utils.PolylineEncoding;
import org.neshan.mapsdk.MapView;
import org.neshan.mapsdk.model.Marker;
import org.neshan.mapsdk.model.Polyline;
import org.neshan.mapsdk.style.NeshanMapStyle;

import java.util.ArrayList;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.repository.location.OnTurnOnGpsCallback;
import ir.roudi.littleneshan.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private Marker userMarker;
    private Marker destinationMarker;
    private Polyline routingPathPolyLine;

    private MainViewModel viewModel;

    // TODO: Add search bar

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // TODO: Add appropriate widow flags to all screens
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
                // TODO
            }
        });

        // TODO: Maybe it's better to observe only the first item.
        viewModel.userLocation.observe(getViewLifecycleOwner(), location -> {
            showLocation(location.getLocation(), location.isCached());
        });

        binding.btnLocation.setOnClickListener(v -> {
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

        viewModel.navigationPath.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String pathString) {
                showPathOnMap(pathString);
            }
        });

        viewModel.address.observe(getViewLifecycleOwner(), address -> {
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
            var markerBitmap = BitmapUtils.createBitmapFromAndroidBitmap(toBitmap(drawable));
            markStCr.setBitmap(markerBitmap);
        }
        return markStCr.buildStyle();
    }

    private Bitmap toBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        var bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        var canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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

        findNavController(this)
                .getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData(DestinationDetailsBottomSheet.KEY_DOES_START_NAVIGATION)
                .observe(getViewLifecycleOwner(), new Observer<Object>() {

                    @Override
                    public void onChanged(Object o) {
                        if (o instanceof Boolean) {
                            boolean doesStartNavigation = (Boolean) o;
                            Toast.makeText(getContext(), "doesStartNavigation: " + doesStartNavigation, Toast.LENGTH_SHORT).show();
                            // TODO: Handle starting navigation
                        }
                    }
                });
    }
}