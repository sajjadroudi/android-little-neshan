package ir.roudi.littleneshan.data.repository.location;

import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.TimeUnit;

public enum PrecisionLocationRequest {

    APPROXIMATE(
            TimeUnit.SECONDS.toMillis(30),
            LocationRequest.PRIORITY_HIGH_ACCURACY
    ),
    PRECISE(
            TimeUnit.MILLISECONDS.toMillis(500),
            LocationRequest.PRIORITY_HIGH_ACCURACY
    );

    private final long intervalMillis;
    private final int priority;

    PrecisionLocationRequest(long intervalMillis, int priority) {
        this.intervalMillis = intervalMillis;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public LocationRequest toLocationRequest() {
        var request = new LocationRequest();
        request.setInterval(intervalMillis);
        request.setPriority(priority);
        return request;
    }

}
