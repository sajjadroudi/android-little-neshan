package ir.roudi.littleneshan;

import androidx.annotation.NonNull;

import java.util.List;

public class DirectionModel {

    private final String overviewPolyline;
    private final String summary;
    private final DurationModel duration;
    private final DistanceModel distance;
    private final List<StepModel> steps;

    public DirectionModel(
            String overviewPolyline,
            String summary,
            DurationModel duration,
            DistanceModel distance,
            List<StepModel> steps
    ) {
        this.overviewPolyline = overviewPolyline;
        this.summary = summary;
        this.duration = duration;
        this.distance = distance;
        this.steps = steps;
    }

    public String getOverviewPolyline() {
        return overviewPolyline;
    }

    public String getSummary() {
        return summary;
    }

    public DurationModel getDuration() {
        return duration;
    }

    public DistanceModel getDistance() {
        return distance;
    }

    public List<StepModel> getSteps() {
        return steps;
    }

    @NonNull
    @Override
    public String toString() {
        return "DirectionModel{" +
                "overviewPolyline='" + overviewPolyline + '\'' +
                ", summary='" + summary + '\'' +
                ", duration=" + duration +
                ", distance=" + distance +
                ", steps=" + steps +
                '}';
    }
}

class DurationModel {
    private final int value;
    private final String text;

    public DurationModel(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    @NonNull
    @Override
    public String toString() {
        return "DurationModel{" +
                "value=" + value +
                ", text='" + text + '\'' +
                '}';
    }
}

class DistanceModel {
    private final int value;
    private final String text;

    public DistanceModel(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    @NonNull
    @Override
    public String toString() {
        return "DistanceModel{" +
                "value=" + value +
                ", text='" + text + '\'' +
                '}';
    }
}

class StepModel {
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