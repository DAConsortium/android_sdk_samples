package jp.co.dac.sdk.ma.sample;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

import static com.google.common.truth.Truth.assertThat;
import static jp.co.dac.sdk.ma.sample.TestUtil.getAdVideoPlayer;
import static jp.co.dac.sdk.ma.sample.TestUtil.getContentVideoPlayer;
import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilPlayed;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScenarioVASTTest {

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = activityRule.getActivity();
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
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));

        final VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);
        takeScreenshot(instrumentation, activity, "playing-ad-video");

        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.content_video_player));
        Thread.sleep(200);
        takeScreenshot(instrumentation, activity, "playing-content-video");
        assertThat(adVideoPlayer.isPlaying()).isFalse();

        final VideoPlayer contentVideoPlayer = getContentVideoPlayer(activity);
        assertThat(contentVideoPlayer.isPlaying()).isTrue();
    }
}
