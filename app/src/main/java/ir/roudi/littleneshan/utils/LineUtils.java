package ir.roudi.littleneshan.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.carto.graphics.Color;
import com.carto.styles.LineStyle;
import com.carto.styles.LineStyleBuilder;

import ir.roudi.littleneshan.R;

public class LineUtils {

    private LineUtils() {

    }

    public static LineStyle buildLineStyle(Context context) {
        var builder = new LineStyleBuilder();
        var color = new Color(ContextCompat.getColor(context, R.color.blue));
        builder.setColor(color);
        builder.setWidth(10f);
        builder.setStretchFactor(0f);
        return builder.buildStyle();
    }

}
