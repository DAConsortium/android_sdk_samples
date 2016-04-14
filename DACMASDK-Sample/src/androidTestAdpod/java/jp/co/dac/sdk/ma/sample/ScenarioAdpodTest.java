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

import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilNextPlayed;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilPlayed;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScenarioAdpodTest {

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
        instrumentation.waitForIdleSync();

        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    /**
     * 1. VAST(Adpod) request success
     *  1.1. load video player
     *
     * 2. waiting...
     *  2.1. start next ad based on adpods.
     */
    @Test
    public void successRequest_playTwoAds_afterPlayContent() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        takeScreenshot(instrumentation, activity, "playing-first-ad-video");

        waitPlayerUntilNextPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        takeScreenshot(instrumentation, activity, "playing-second-ad-video");

        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.content_video_player));
        takeScreenshot(instrumentation, activity, "playing-content-video");
    }
}
