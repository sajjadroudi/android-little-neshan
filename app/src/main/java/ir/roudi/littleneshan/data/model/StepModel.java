package ir.roudi.littleneshan.data.model;

import androidx.annotation.NonNull;

public class StepModel {
    private final String name;
    private final String instruction;
    private final DistanceModel distance;
    private final DurationModel duration;
    private final LocationModel startPoint;
    private final String encodedPolyline;

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