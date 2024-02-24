package ir.roudi.littleneshan.data.repository.location;

import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.TimeUnit;

public enum PrecisionLocationRequest {

    APPROXIMATE(
            TimeUnit.SECONDS.toMillis(30),
            TimeUnit.SECONDS.toMillis(60),
            TimeUnit.MINUTES.toMillis(2),
            LocationRequest.PRIORITY_HIGH_ACCURACY
    ),
    PRECISE(
            TimeUnit.MILLISECONDS.toMillis(500),
            TimeUnit.MILLISECONDS.toMillis(250),
            TimeUnit.SECONDS.toMillis(1),
            LocationRequest.PRIORITY_HIGH_ACCURACY
    );

    private final long intervalMillis;
    private final long fastestIntervalMillis;
    private final long maxWaitTimeMillis;
    private final int priority;

    PrecisionLocationRequest(long intervalMillis, long fastestIntervalMillis, long maxWaitTimeMillis, int priority) {
        this.intervalMillis = intervalMillis;
        this.fastestIntervalMillis = fastestIntervalMillis;
        this.maxWaitTimeMillis = maxWaitTimeMillis;
        this.priority = priority;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public long getFastestIntervalMillis() {
        return fastestIntervalMillis;
    }

    public long getMaxWaitTimeMillis() {
        return maxWaitTimeMillis;
    }

    public int getPriority() {
        return priority;
    }

    public LocationRequest toLocationRequest() {
        var request = new LocationRequest();
        // TODO: Maybe only need to set interval and priority
        request.setInterval(intervalMillis);
        request.setFastestInterval(fastestIntervalMillis);
        request.setMaxWaitTime(maxWaitTimeMillis);
        request.setPriority(priority);
        return request;
    }

}
