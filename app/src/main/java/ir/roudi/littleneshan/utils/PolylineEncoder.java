package ir.roudi.littleneshan.utils;

import org.neshan.common.utils.PolylineEncoding;

import java.util.List;
import java.util.stream.Collectors;

import ir.roudi.littleneshan.data.model.LocationModel;

public class PolylineEncoder {

    private PolylineEncoder() {}

    public static List<LocationModel> decode(String polyline) {
        return PolylineEncoding.decode(polyline)
                .stream()
                .map(LocationModel::from)
                .collect(Collectors.toList());
    }

}
