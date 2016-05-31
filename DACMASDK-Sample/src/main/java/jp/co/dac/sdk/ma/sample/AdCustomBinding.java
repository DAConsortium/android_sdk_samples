package jp.co.dac.sdk.ma.sample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.IntRange;
import android.view.View;
import android.view.ViewGroup;

public class AdCustomBinding {

    public static void setVerticalPercentage(View view, @IntRange(from = 0, to = 100) int verticalPercentage) {
        Context context = view.getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Point size = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(size);

            final int height = (int) (size.y * (verticalPercentage / 100.0));
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                params.height = height;
                view.setLayoutParams(params);
            }
        }
    }
}
