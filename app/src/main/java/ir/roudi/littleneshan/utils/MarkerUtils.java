package ir.roudi.littleneshan.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;

public class MarkerUtils {

    private MarkerUtils() {

    }

    public static MarkerStyle buildMarkerStyle(Context context, int iconResource) {
        var markStCr = new MarkerStyleBuilder();
        markStCr.setSize(30f);

        var drawable = ContextCompat.getDrawable(context, iconResource);
        if (drawable != null) {
            var markerBitmap = BitmapUtils.createBitmapFromAndroidBitmap(
                    LittleNeshanBitmapUtils.toBitmap(drawable)
            );
            markStCr.setBitmap(markerBitmap);
        }

        return markStCr.buildStyle();
    }

}
