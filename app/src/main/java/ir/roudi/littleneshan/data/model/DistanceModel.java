package ir.roudi.littleneshan.data.model;

import androidx.annotation.NonNull;

public class DistanceModel {
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