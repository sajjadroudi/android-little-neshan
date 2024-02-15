package ir.roudi.littleneshan.ui.main;

import androidx.annotation.NonNull;

public class AddressUiModel {

    private final String title;
    private final String duration;
    private final String distance;
    private final String address;

    public AddressUiModel(String title, String duration, String distance, String address) {
        this.title = title;
        this.duration = duration;
        this.distance = distance;
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }

    @NonNull
    @Override
    public String toString() {
        return "AddressUiModel{" +
                "title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", distance='" + distance + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
