package ir.roudi.littleneshan.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MainInterceptor implements Interceptor {

    private static final String KEY_API_KEY = "Api-Key";
    private static final String VALUE_API_KEY = "service.9c22024b43d14d749c794c2a280a90d6";

    @Inject
    public MainInterceptor() {

    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        Headers headers = request.headers()
                .newBuilder()
                .add(KEY_API_KEY, VALUE_API_KEY)
                .build();

        Request newRequest = request.newBuilder()
                .headers(headers)
                .build();

        return chain.proceed(newRequest);
    }

}
