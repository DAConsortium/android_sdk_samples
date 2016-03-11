package jp.co.dac.sdk.ma.sample;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

import static com.google.common.truth.Truth.assertThat;
import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayer;


@RunWith(Enclosed.class)
@LargeTest
public class ScenarioVASTTest {

    private final static String AD_TAG_URL = "http://webdemo.dac.co.jp/sdfactory/stsn/top.php?filepath=stsn/unit/fruitsbear.xml";

    public static class OnlyAdTest {

        private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        @Rule
        public final ActivityTestRule<MainActivity> activityRule =
                new ActivityTestRule<>(MainActivity.class);

        @Rule
        public final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

        private MainActivity activity;

        @Before
        public void setUp() throws Exception {
            activity = activityRule.getActivity();
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    activity.populateOnlyAdFragment(AD_TAG_URL);
                }
            });
            instrumentation.waitForIdleSync();

            Intents.init();
        }

        @After
        public void tearDown() throws Exception {
            Intents.release();
        }

        /**
         * 1. start ad video
         * 2. completed ad video and paused video
         */
        @Test
        public void startAdVideo_afterPauseVideoPlayer() throws Exception {
            waitPlayer((DACVideoPlayerView) activity.findViewById(R.id.video_player));

            final VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);
            takeScreenshot(instrumentation, activity, "playing-ad-video");

            Thread.sleep(20_000);

            takeScreenshot(instrumentation, activity, "paused-ad-video");
            assertThat(adVideoPlayer.isPlaying()).isFalse();
            assertThat(adVideoPlayer).isEqualTo(getAdVideoPlayer(activity));
        }
    }

    public static class WithContentTest {

        private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        @Rule
        public final ActivityTestRule<MainActivity> activityRule =
                new ActivityTestRule<>(MainActivity.class);

        @Rule
        public final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

        private MainActivity activity;

        @Before
        public void setUp() throws Exception {
            activity = activityRule.getActivity();
            instrumentation.runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    activity.populateWithContentFragment(AD_TAG_URL);
                }
            });
            instrumentation.waitForIdleSync();

            Intents.init();
        }

        @After
        public void tearDown() throws Exception {
            Intents.release();
        }

        /**
         * 1. start ad video
         * 2. start content video after completed ad video
         */
        @Test
        public void startAdVideo_afterStartContentVideo() throws Exception {
            waitPlayer((DACVideoPlayerView) activity.findViewById(R.id.video_player));

            final VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);
            takeScreenshot(instrumentation, activity, "playing-ad-video");

            Thread.sleep(20_000);

            takeScreenshot(instrumentation, activity, "playing-content-video");
            assertThat(adVideoPlayer.isPlaying()).isFalse();

            final VideoPlayer contentVideoPlayer = getContentVideoPlayer(activity);
            assertThat(contentVideoPlayer.isPlaying()).isTrue();
        }
    }

    private static VideoPlayer getAdVideoPlayer(Activity activity) {
        return ((DACVideoPlayerView) activity.findViewById(R.id.video_player)).getVideoPlayer();
    }

    private static VideoPlayer getContentVideoPlayer(Activity activity) {
        return ((DACVideoPlayerView) activity.findViewById(R.id.content_video_player)).getVideoPlayer();
    }
}
