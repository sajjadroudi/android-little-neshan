package ir.roudi.littleneshan.ui.navigation;

import ir.roudi.littleneshan.data.model.LocationModel;
import ir.roudi.littleneshan.data.model.StepModel;

public class NavigationPointModel {

    private final LocationModel point;
    private final StepModel step;

    public NavigationPointModel(LocationModel point, StepModel step) {
        this.point = point;
        this.step = step;
    }

    public LocationModel getPoint() {
        return point;
    }

    public StepModel getStep() {
        return step;
    }

    public float distanceTo(NavigationPointModel another) {
        return distanceTo(another.point);
    }

    public float distanceTo(LocationModel location) {
        return point.distanceTo(location);
    }

}
