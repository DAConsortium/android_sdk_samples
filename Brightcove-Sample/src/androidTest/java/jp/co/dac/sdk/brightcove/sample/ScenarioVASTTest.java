package jp.co.dac.sdk.brightcove.sample;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

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
import static jp.co.dac.sdk.brightcove.sample.TestUtil.getAdVideoPlayer;
import static jp.co.dac.sdk.brightcove.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.brightcove.sample.TestUtil.waitAdVideoPlayerView;
import static jp.co.dac.sdk.brightcove.sample.TestUtil.waitPlayerUntilPaused;
import static jp.co.dac.sdk.brightcove.sample.TestUtil.waitPlayerUntilPlayed;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScenarioVASTTest {

    private final static String AD_TAG_URL = "http://webdemo.dac.co.jp/sdfactory/stsn/top.php?filepath=stsn/unit/fruitsbear.xml";

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
                activity.loadAdTagUrl(AD_TAG_URL);
            }
        });

        waitAdVideoPlayerView(activity);
        instrumentation.waitForIdleSync();

        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    /*
     * 1. start ad video(fruits bear)
     * 2. start content video after completed ad video
     */
    @Test
    public void startAdVideoFruitsBear_afterStartContentVideo() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));

        final VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);
        takeScreenshot(instrumentation, activity, "playing-ad-video");

        assertThat(activity.getBrightcoveVideoView().isPlaying()).isFalse();

        waitPlayerUntilPaused((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        waitPlayerUntilPlayed(activity.getBrightcoveVideoView());

        takeScreenshot(instrumentation, activity, "playing-content-video");
        assertThat(adVideoPlayer.isPlaying()).isFalse();
    }
}
