package ir.roudi.littleneshan.data.model;

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