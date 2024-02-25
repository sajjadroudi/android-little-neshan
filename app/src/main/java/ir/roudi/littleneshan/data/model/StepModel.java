package ir.roudi.littleneshan.data.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ir.roudi.littleneshan.utils.PolylineEncoder;

public class StepModel {
    private final String name;
    private final String instruction;
    private final DistanceModel distance;
    private final DurationModel duration;
    private final LocationModel startPoint;
    private final String encodedPolyline;
    private final List<LocationModel> routingPoints;

    public StepModel(
            String name,
            String instruction,
            DistanceModel distance,
            DurationModel duration,
            LocationModel startPoint,
            String encodedPolyline
    ) {
        this.name = name;
        this.instruction = instruction;
        this.distance = distance;
        this.duration = duration;
        this.startPoint = startPoint;
        this.encodedPolyline = encodedPolyline;
        this.routingPoints = PolylineEncoder.decode(encodedPolyline);
    }

    public String getName() {
        return name;
    }

    public String getInstruction() {
        return instruction;
    }

    public DistanceModel getDistance() {
        return distance;
    }

    public DurationModel getDuration() {
        return duration;
    }

    public LocationModel getStartPoint() {
        return startPoint;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public List<LocationModel> getRoutingPoints() {
        return new ArrayList<>(routingPoints);
    }

    @NonNull
    @Override
    public String toString() {
        return "StepModel{" +
                "name='" + name + '\'' +
                ", instruction='" + instruction + '\'' +
                ", distance=" + distance +
                ", duration=" + duration +
                ", startPoint=" + startPoint +
                ", encodedPolyline='" + encodedPolyline + '\'' +
                '}';
    }

}