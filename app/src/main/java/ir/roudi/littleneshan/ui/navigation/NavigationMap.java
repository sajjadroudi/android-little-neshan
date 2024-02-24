package ir.roudi.littleneshan.ui.navigation;

import androidx.annotation.NonNull;

import org.neshan.common.model.LatLng;
import org.neshan.common.utils.PolylineEncoding;
import org.neshan.mapsdk.MapView;
import org.neshan.mapsdk.model.Marker;
import org.neshan.mapsdk.model.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;
import ir.roudi.littleneshan.ui.Config;
import ir.roudi.littleneshan.utils.LineUtils;
import ir.roudi.littleneshan.utils.MarkerUtils;

public class NavigationMap {

    private final MapView map;

    private Marker userLocationMarker;

    private Polyline remainingPathPolyline;

    private Marker destinationMarker;

    public NavigationMap(MapView map, int mapStyle) {
        this.map = map;

        setupMap(mapStyle);
    }

    private void setupMap(int mapStyle) {
        map.setMapStyle(mapStyle);
        map.setTrafficEnabled(true);
        map.setPoiEnabled(true);
        map.setTilt(40f, 0f);
        map.setMyLocationEnabled(Config.SHOW_USER_LOCATION_BY_NESHAN);
    }

    public void showRemainingPathOnMap(List<StepModel> remainingSteps) {
        removePathIfExists();
        addPathToMap(remainingSteps);
    }

    private void removePathIfExists() {
        if (remainingPathPolyline != null) {
            map.removePolyline(remainingPathPolyline);
        }
    }

    private void addPathToMap(List<StepModel> remainingSteps) {
        remainingPathPolyline = buildPolyline(remainingSteps);
        map.addPolyline(remainingPathPolyline);
    }

    private Polyline buildPolyline(List<StepModel> remainingSteps) {
        ArrayList<LatLng> pointsOfRemainingPath = extractPointsOfPath(remainingSteps);
        var lineStyle = LineUtils.buildLineStyle(map.getContext());
        return new Polyline(pointsOfRemainingPath, lineStyle);
    }

    @NonNull
    private static ArrayList<LatLng> extractPointsOfPath(List<StepModel> remainingSteps) {
        var pointsOfRemainingPath = remainingSteps.stream()
                .map(step -> PolylineEncoding.decode(step.getEncodedPolyline()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new ArrayList<>(pointsOfRemainingPath);
    }

    public void focusOnLocation(LocationModel location) {
        map.moveCamera(location.toLatLng(), 0.5f);
        if (map.getZoom() != 18f) {
            map.setZoom(18f, 0.5f);
        }
    }

    public void markUserOnMap(LocationModel location) {
        if(Config.SHOW_USER_LOCATION_BY_NESHAN) {
            map.pauseLocationViewer();
            map.onLocationChanged(location.toLocation());
        } else {
            removeUserMarkerIfExists();
            addUserMarkerToMap(location);
        }
    }

    private void removeUserMarkerIfExists() {
        if (userLocationMarker != null) {
            map.removeMarker(userLocationMarker);
        }
    }

    private void addUserMarkerToMap(LocationModel location) {
        userLocationMarker = buildUserMarker(location);
        map.addMarker(userLocationMarker);
    }

    private Marker buildUserMarker(LocationModel location) {
        var markerStyle = MarkerUtils.buildMarkerStyle(map.getContext(), R.drawable.ic_marker);
        return new Marker(location.toLatLng(), markerStyle);
    }

    public void setBearing(float bearing) {
        map.setBearing(bearing, 0.7f);
    }

    public void markDestinationOnMap(LocationModel location) {
        removeDestinationMarkerIfExists();
        addDestinationMarkerToMap(location);
    }

    private void removeDestinationMarkerIfExists() {
        if (destinationMarker != null) {
            map.removeMarker(destinationMarker);
        }
    }

    private void addDestinationMarkerToMap(LocationModel location) {
        var style = MarkerUtils.buildMarkerStyle(map.getContext(), R.drawable.ic_destination);
        destinationMarker = new Marker(location.toLatLng(), style);
        map.addMarker(destinationMarker);
    }

}
