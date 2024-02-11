package ir.roudi.littleneshan.data.remote;

import io.reactivex.rxjava3.core.Single;
import ir.roudi.littleneshan.data.remote.model.AddressResponse;
import ir.roudi.littleneshan.data.remote.model.DirectionResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NeshanService {

    @GET("reverse")
    Single<AddressResponse> getAddress(
        @Query("lat") double lat,
        @Query("lng") double lng
    );

    @GET("direction")
    Single<DirectionResponse> getDirection(
        @Query("type") String type,
        @Query("origin") String startPoint,
        @Query("destination") String endPoint,
        @Query("bearing") int bearing
    );

}
