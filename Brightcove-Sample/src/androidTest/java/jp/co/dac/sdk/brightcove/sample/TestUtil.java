package jp.co.dac.sdk.brightcove.sample;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.support.test.uiautomator.UiDevice;
import android.view.View;

import com.brightcove.player.view.BrightcoveVideoView;
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
    }

    public static void waitPlayerUntilPlayed(BrightcoveVideoView videoPlayerView) throws Exception {
        while (true) {
            if (videoPlayerView.isPlaying()) break;
            Thread.sleep(50);
        }
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
    }

    public static void waitPlayerUntilPaused(DACVideoPlayerView videoPlayerView) throws Exception {
        while (true) {
            VideoPlayer player = videoPlayerView.getVideoPlayer();
            if (player == null || !player.isPlaying()) break;
            Thread.sleep(50);
        }
    }

    public static void waitAdVideoPlayerView(Activity activity) throws Exception {
        while (true) {
            if (activity.findViewById(R.id.ad_video_player) != null) break;
            Thread.sleep(16);
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

    public static VideoPlayer getAdVideoPlayer(Activity activity) {
        return ((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player)).getVideoPlayer();
    }
}
