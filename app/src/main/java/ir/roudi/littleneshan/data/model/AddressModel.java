package ir.roudi.littleneshan.data.model;

import androidx.annotation.NonNull;

public class AddressModel {
    private final String routeName;
    private final String address;

    public AddressModel(String routeName, String address) {
        this.routeName = routeName;
        this.address = address;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getAddress() {
        return address;
    }

    @NonNull
    @Override
    public String toString() {
        return "AddressModel{" +
                "routeName='" + routeName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
