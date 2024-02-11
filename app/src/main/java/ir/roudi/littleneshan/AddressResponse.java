package ir.roudi.littleneshan;

import com.google.gson.annotations.SerializedName;

public class AddressResponse {

    @SerializedName("formatted_address")
    private final String formattedAddress;

    @SerializedName("route_name")
    private final String routeName;

    @SerializedName("route_type")
    private final String routeType;

    @SerializedName("neighbourhood")
    private final String neighbourhood;

    @SerializedName("city")
    private final String city;

    @SerializedName("place")
    private final String place;

    @SerializedName("municipality_zone")
    private final String municipalityZone;

    @SerializedName("in_traffic_zone")
    private final String inTrafficZone;

    @SerializedName("in_odd_even_zone")
    private final String inOddEvenZone;

    @SerializedName("village")
    private final String village;

    @SerializedName("district")
    private final String district;

    public AddressResponse(String formattedAddress, String routeName, String routeType, String neighbourhood, String city, String place, String municipalityZone, String inTrafficZone, String inOddEvenZone, String village, String district) {
        this.formattedAddress = formattedAddress;
        this.routeName = routeName;
        this.routeType = routeType;
        this.neighbourhood = neighbourhood;
        this.city = city;
        this.place = place;
        this.municipalityZone = municipalityZone;
        this.inTrafficZone = inTrafficZone;
        this.inOddEvenZone = inOddEvenZone;
        this.village = village;
        this.district = district;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteType() {
        return routeType;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public String getCity() {
        return city;
    }

    public String getPlace() {
        return place;
    }

    public String getMunicipalityZone() {
        return municipalityZone;
    }

    public String getInTrafficZone() {
        return inTrafficZone;
    }

    public String getInOddEvenZone() {
        return inOddEvenZone;
    }

    public String getVillage() {
        return village;
    }

    public String getDistrict() {
        return district;
    }

    @Override
    public String toString() {
        return "AddressResponse{" +
                "formattedAddress='" + formattedAddress + '\'' +
                ", routeName='" + routeName + '\'' +
                ", routeType='" + routeType + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", city='" + city + '\'' +
                ", place='" + place + '\'' +
                ", municipalityZone='" + municipalityZone + '\'' +
                ", inTrafficZone='" + inTrafficZone + '\'' +
                ", inOddEvenZone='" + inOddEvenZone + '\'' +
                ", village='" + village + '\'' +
                ", district='" + district + '\'' +
                '}';
    }

    public AddressModel toAddressModel() {
        return new AddressModel(routeName, formattedAddress);
    }

}