package ir.roudi.littleneshan;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NeshanService {

    @GET("reverse")
    Single<AddressResponse> getAddress(@Query("lat") double lat, @Query("lng") double lng);

}
