package jp.co.dac.sdk.ma.sample.player;


import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;

public final class ScrollViewUtil {

    private ScrollViewUtil() {}

    /**
     * Retrieve parent ScrollView from {@code targetView}.
     *
     * @return parent ScrollView, null not hit.
     */
    public static ScrollView getParentScrollView(final View targetView) {
        ViewParent parent = targetView.getParent();
        while (parent != null) {
            if (parent instanceof ScrollView) {
                return (ScrollView) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * {@code child} can be shown, return true, false otherwise.
     */
    public static boolean inScrollBounds(final ScrollView parent, final View child) {
        if (parent == null || child == null) {
            return false;
        }

        Rect bounds = new Rect();
        child.getHitRect(bounds);
        Rect scrollBounds = new Rect(
                parent.getScrollX(), parent.getScrollY(),
                parent.getScrollX() + parent.getWidth(), parent.getScrollY() + parent.getHeight());
        return Rect.intersects(scrollBounds, bounds);
    }
}
