package ir.roudi.littleneshan.ui.main;

import com.carto.core.ScreenBounds;
import com.carto.core.ScreenPos;

import org.neshan.common.model.LatLngBounds;
import org.neshan.common.utils.PolylineEncoding;
import org.neshan.mapsdk.MapView;
import org.neshan.mapsdk.model.Marker;
import org.neshan.mapsdk.model.Polyline;
import org.neshan.mapsdk.style.NeshanMapStyle;

import java.util.ArrayList;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.core.Config;
import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.utils.LineUtils;
import ir.roudi.littleneshan.utils.MarkerUtils;
import ir.roudi.littleneshan.utils.OnMapClickListener;
import ir.roudi.littleneshan.utils.PolylineEncoder;

public class LittleNeshanMap {

    public interface OnMapLongClickListener {
        void onMapLongClick(LocationModel location);
    }

    private Marker userMarker;
    private Marker destinationMarker;
    private Polyline routingPathPolyLine;

    private final MapView map;

    public LittleNeshanMap(MapView map) {
        this.map = map;

        setupMap();
    }

    private void setupMap() {
        map.setTrafficEnabled(!Config.USE_FAKE_USER_LOCATION);
        map.setPoiEnabled(!Config.USE_FAKE_USER_LOCATION);
    }

    public void showPathOnMap(String pathPolyline, LocationModel start, LocationModel end) {
        removePathIfExists();
        addPathToMap(pathPolyline);
        changeCameraToShowWholePath(pathPolyline, start, end);
    }

    private void removePathIfExists() {
        if (routingPathPolyLine != null) {
            map.removePolyline(routingPathPolyLine);
        }
    }

    private void addPathToMap(String pathPolyline) {
        var path = PolylineEncoding.decode(pathPolyline);
        var lineStyle = LineUtils.buildLineStyle(map.getContext());
        routingPathPolyLine = new Polyline(new ArrayList<>(path), lineStyle);
        map.addPolyline(routingPathPolyLine);
    }

    private void changeCameraToShowWholePath(String pathPolyline, LocationModel start, LocationModel end) {
        float mapWidth = Math.min(map.getWidth(), map.getHeight());
        var screenBounds = new ScreenBounds(
                new ScreenPos(0F, 0F),
                new ScreenPos(mapWidth, mapWidth)
        );
        map.moveToCameraBounds(extractBounds(pathPolyline, start, end), screenBounds, true, 0.5f);
    }

    private LatLngBounds extractBounds(String pathPolyline, LocationModel source, LocationModel destination) {
        var points = PolylineEncoder.decode(pathPolyline);
        points.add(source);
        points.add(destination);

        float maxDistance = 0;
        LocationModel start = null, end = null;
        for(int i = 0; i < points.size(); i++) {
            for(int j = i + 1; j < points.size(); j++) {
                var distance = points.get(i).distanceTo(points.get(j));
                if(distance > maxDistance) {
                    maxDistance = distance;
                    start = points.get(i);
                    end = points.get(j);
                }
            }
        }

        return new LatLngBounds(start.toLatLng(), end.toLatLng());
    }

    public void focusOnLocation(LocationModel location) {
        map.moveCamera(location.toLatLng(), 0.25f);
        map.setZoom(15, 0.25f);
    }

    public void markUserOnMap(LocationModel location, boolean isCachedLocation) {
        removeUserMarkerIfExists();
        addUserMarkerToMap(location, isCachedLocation);
    }

    private void removeUserMarkerIfExists() {
        if (userMarker != null) {
            map.removeMarker(userMarker);
        }
    }

    private void addUserMarkerToMap(LocationModel location, boolean isCachedLocation) {
        int icon = isCachedLocation ? R.drawable.ic_marker_off : R.drawable.ic_marker;
        var markerStyle = MarkerUtils.buildMarkerStyle(map.getContext(), icon);
        userMarker = new Marker(location.toLatLng(), markerStyle);
        map.addMarker(userMarker);
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

    public void clear() {
        removeDestinationMarkerIfExists();
        removePathIfExists();
    }

    public void switchTheme() {
        boolean isNightMode = (map.getMapStyle() == NeshanMapStyle.NESHAN);
        var newTheme = isNightMode ? NeshanMapStyle.NESHAN_NIGHT : NeshanMapStyle.NESHAN;
        map.setMapStyle(newTheme);
    }

    public boolean isNightTheme() {
        return map.getMapStyle() == NeshanMapStyle.NESHAN;
    }

    public int getMapStyle() {
        return map.getMapStyle();
    }

    public void setOnMapLongClickListener(OnMapLongClickListener listener) {
        map.setOnMapLongClickListener(latLng -> {
            listener.onMapLongClick(LocationModel.from(latLng));
        });
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        map.setOnMapClickListener(latLng -> listener.onClick(LocationModel.from(latLng)));
    }

}
