package jp.co.dac.sdk.ma.sample;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.support.test.uiautomator.UiDevice;

import com.squareup.spoon.Spoon;

import java.io.File;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

public final class TestUtil {
    public static void waitPlayer(DACVideoPlayerView videoPlayerView) throws Exception {
        while (true) {
            VideoPlayer player = videoPlayerView.getVideoPlayer();
            if (player != null && player.isPlaying()) break;
            Thread.sleep(50);
        }
    }

    public static void takeScreenshot(Instrumentation instrumentation, Activity activity, String tag) {
        final File screenshot = Spoon.screenshot(activity, tag);

        if (Build.VERSION.SDK_INT >= 18) {
            UiDevice device = UiDevice.getInstance(instrumentation);
            device.takeScreenshot(screenshot);
        } else {
            // TODO
        }

    }
}
