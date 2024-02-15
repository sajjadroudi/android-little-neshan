package ir.roudi.littleneshan.di;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ir.roudi.littleneshan.data.remote.MainInterceptor;
import ir.roudi.littleneshan.data.remote.NeshanService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    public static NeshanService provideNeshanService(Retrofit retrofit) {
        return retrofit.create(NeshanService.class);
    }

    public static Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl("https://api.neshan.org/v4/")
                .client(client)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static OkHttpClient provideOkHttpClient(
            MainInterceptor interceptor
    ) {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .callTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
    }

}