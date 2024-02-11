package ir.roudi.littleneshan;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectionResponse {
    @SerializedName("routes")
    private final List<RouteResponse> routes;

    public DirectionResponse(List<RouteResponse> routes) {
        this.routes = routes;
    }

    public DirectionResponse() {
        this(new ArrayList<>());
    }

    public List<RouteResponse> getRoutes() {
        return routes;
    }

    @NonNull
    @Override
    public String toString() {
        return "DirectionResponse{" +
                "routes=" + routes +
                '}';
    }

    public DirectionModel toDirectionModel() {
        RouteResponse route = routes.get(0);
        LegResponse leg = route.getLegs().get(0);
        return new DirectionModel(
                route.getOverviewPolyline().getEncodedPolyline(),
                leg.getSummary(),
                leg.getDuration().toDurationModel(),
                leg.getDistance().toDistanceModel(),
                StepResponse.toStepModels(leg.getSteps())
        );
    }

}

class RouteResponse {
    @SerializedName("overview_polyline")
    private final OverviewPolylineResponse overviewPolyline;

    @SerializedName("legs")
    private final List<LegResponse> legs;

    public RouteResponse(OverviewPolylineResponse overviewPolyline, List<LegResponse> legs) {
        this.overviewPolyline = overviewPolyline;
        this.legs = legs;
    }

    public OverviewPolylineResponse getOverviewPolyline() {
        return overviewPolyline;
    }

    public List<LegResponse> getLegs() {
        return legs;
    }

    @Override
    public String toString() {
        return "RouteResponse{" +
                "overviewPolyline=" + overviewPolyline +
                ", legs=" + legs +
                '}';
    }
}

class OverviewPolylineResponse {
    @SerializedName("points")
    private final String encodedPolyline;

    public OverviewPolylineResponse(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    @Override
    public String toString() {
        return "OverviewPolylineResponse{" +
                "encodedPolyline='" + encodedPolyline + '\'' +
                '}';
    }
}

class LegResponse {

    @SerializedName("summary")
    private final String summary;

    @SerializedName("distance")
    private final DistanceResponse distance;

    @SerializedName("duration")
    private final DurationResponse duration;

    @SerializedName("steps")
    private final List<StepResponse> steps;

    public LegResponse(String summary, DistanceResponse distance, DurationResponse duration, List<StepResponse> steps) {
        this.summary = summary;
        this.distance = distance;
        this.duration = duration;
        this.steps = steps;
    }

    public String getSummary() {
        return summary;
    }

    public DistanceResponse getDistance() {
        return distance;
    }

    public DurationResponse getDuration() {
        return duration;
    }

    public List<StepResponse> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "LegResponse{" +
                "summary='" + summary + '\'' +
                ", distance=" + distance +
                ", duration=" + duration +
                ", steps=" + steps +
                '}';
    }
}

class DistanceResponse {
    @SerializedName("value")
    private final int value;

    @SerializedName("text")
    private final String text;

    public DistanceResponse(int value, String text) {
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
        return "DistanceResponse{" +
                "value=" + value +
                ", text='" + text + '\'' +
                '}';
    }

    public DistanceModel toDistanceModel() {
        return new DistanceModel(value, text);
    }

}

class DurationResponse {

    @SerializedName("value")
    private final int value;

    @SerializedName("text")
    private final String text;

    public DurationResponse(int value, String text) {
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
        return "DurationResponse{" +
                "value=" + value +
                ", text='" + text + '\'' +
                '}';
    }

    public DurationModel toDurationModel() {
        return new DurationModel(value, text);
    }

}

class StepResponse {

    @SerializedName("name")
    private final String name;

    @SerializedName("instruction")
    private final String instruction;

    @SerializedName("duration")
    private final DurationResponse duration;

    @SerializedName("distance")
    private final DistanceResponse distance;

    @SerializedName("start_location")
    private final double[] startLocation;

    @SerializedName("polyline")
    private final String encodedPolyline;

    public StepResponse(String name, String instruction, DurationResponse duration, DistanceResponse distance, double[] startLocation, String encodedPolyline) {
        this.name = name;
        this.instruction = instruction;
        this.duration = duration;
        this.distance = distance;
        this.startLocation = startLocation;
        this.encodedPolyline = encodedPolyline;
    }

    @NonNull
    @Override
    public String toString() {
        return "StepResponse{" +
                "name='" + name + '\'' +
                ", instruction='" + instruction + '\'' +
                ", duration=" + duration +
                ", distance=" + distance +
                ", startLocation=" + Arrays.toString(startLocation) +
                ", encodedPolyline='" + encodedPolyline + '\'' +
                '}';
    }

    private StepModel toStepModel() {
        return new StepModel(
                name,
                instruction,
                distance.toDistanceModel(),
                duration.toDurationModel(),
                new LocationModel(startLocation),
                encodedPolyline
        );
    }

    public static List<StepModel> toStepModels(List<StepResponse> steps) {
        return steps.stream()
                .map(StepResponse::toStepModel)
                .collect(Collectors.toList());
    }

}