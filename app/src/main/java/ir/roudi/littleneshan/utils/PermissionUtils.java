package ir.roudi.littleneshan.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionUtils {

    private PermissionUtils() {

    }

    public static boolean isPermissionPermanentlyDenied(Fragment fragment, String permission) {
        if(fragment.getContext() == null)
            return true;

        var isPermissionDenied = ContextCompat.checkSelfPermission(fragment.getContext(), permission)
                != PackageManager.PERMISSION_GRANTED;

        return isPermissionDenied && !fragment.shouldShowRequestPermissionRationale(permission);
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

}
