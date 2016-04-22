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

    public static void waitPlayerUntilPlayed(DACVideoPlayerView videoPlayerView) throws Exception {
        while (true) {
            VideoPlayer player = videoPlayerView.getVideoPlayer();
            if (player != null && player.isPlaying()) break;
            Thread.sleep(50);
        }
        safety();
    }

    public static void waitPlayerUntilNextPlayed(DACVideoPlayerView videoPlayerView) throws Exception {
        int previousPosition = videoPlayerView.getVideoPlayer().getCurrentPosition();

        while (true) {
            VideoPlayer player = videoPlayerView.getVideoPlayer();
            if (player != null && player.isPlaying()) {
                if (previousPosition > player.getCurrentPosition()) {
                    break;
                }
                previousPosition = player.getCurrentPosition();
            }
            Thread.sleep(50);
        }
        safety();
    }

    public static void waitPlayerUntilPaused(DACVideoPlayerView videoPlayerView) throws Exception {
        while (true) {
            VideoPlayer player = videoPlayerView.getVideoPlayer();
            if (player == null || !player.isPlaying()) break;
            Thread.sleep(50);
        }
        safety();
    }

    public static void takeScreenshot(Instrumentation instrumentation, Activity activity, String tag) {
        try {
            final File screenshot = Spoon.screenshot(activity, tag);

            if (Build.VERSION.SDK_INT >= 18) {
                UiDevice device = UiDevice.getInstance(instrumentation);
                device.takeScreenshot(screenshot);
            } else {
                // TODO
            }
        } catch (Exception e) {
            // no-op
        }
    }

    public static VideoPlayer getAdVideoPlayer(Activity activity) {
        return ((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player)).getVideoPlayer();
    }

    public static VideoPlayer getContentVideoPlayer(Activity activity) {
        return ((DACVideoPlayerView) activity.findViewById(R.id.content_video_player)).getVideoPlayer();
    }

    private static void safety() throws InterruptedException {
        Thread.sleep(6 * 16);
    }
}
