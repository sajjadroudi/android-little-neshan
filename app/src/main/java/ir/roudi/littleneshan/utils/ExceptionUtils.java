package ir.roudi.littleneshan.utils;

import java.net.UnknownHostException;

public class ExceptionUtils {

    private ExceptionUtils() {

    }

    public static boolean isDisconnectedToInternet(Throwable throwable) {
        return throwable instanceof UnknownHostException;
    }

}
