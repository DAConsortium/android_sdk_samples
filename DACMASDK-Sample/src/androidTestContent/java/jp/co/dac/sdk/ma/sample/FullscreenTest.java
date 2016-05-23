package jp.co.dac.sdk.ma.sample;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;
import static jp.co.dac.sdk.ma.sample.TestUtil.getAdVideoPlayer;
import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilPaused;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilPlayed;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class FullscreenTest {

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
     * FIXME: close tracking event
     *
     * 1. start ad video
     * 2. click Fullscreen Button
     * 3. open Fullscreen View and resume Ad Video.
     * 4. close Fullscreen when clicked `close button`
     * 5. resume Ad Video
     */
    @Test
    public void clickCloseButton_closeFullscreen_resumeAdVideo() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);

        // click full screen button
        onView(withId(R.id.fullscreen_button))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(300);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "close_button-start-fullscreen-view");

        // click close button
        onView(withId(R.id.close))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(300);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "comeback-starting-view-from-fullscreen-view");
    }

    /**
     * FIXME: mute tracking event
     *
     * 1. start ad video
     * 2. click Fullscreen Button
     * 3. open Fullscreen View and resume Ad Video.
     * 4. is unmute when clicked `mute button`
     * 5. mute state sync
     */
    @Test
    public void clickMuteButton_muteFullscreen_changeVolume() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);

        // click full screen button
        onView(withId(R.id.fullscreen_button))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(100);
        assertThat(adVideoPlayer.isMute()).isTrue();
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "mute_button-start-fullscreen-view");

        // click mute button
        onView(withId(R.id.volume))
                .check(matches(isDisplayed()))
                .perform(click());
        assertThat(adVideoPlayer.isMute()).isFalse();
        takeScreenshot(instrumentation, activity, "mute_button-change-mute-button-state");

        // take over mute state
        onView(withId(R.id.close))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(100);
        assertThat(adVideoPlayer.isMute()).isFalse();
        takeScreenshot(instrumentation, activity, "mute_button-original-display-change-mute-button-state");
    }

    /**
     * 1. start ad video
     * 2. click Fullscreen Button
     * 3. click Video Ad then opening landing page
     */
    @Test
    public void clickVideoPlayer_openLP() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);

        // click full screen button
        onView(withId(R.id.fullscreen_button))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(300);
        assertThat(adVideoPlayer.isMute()).isTrue();
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "click_video_player-start-fullscreen-view");

        // click video player
        onView(withId(R.id.video_player))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(1000);
        takeScreenshot(instrumentation, activity, "click_video_player-open-lp");
        Intents.intended(
                hasData("http://www.dac.co.jp/")
        );

        // cleanup
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(startMain);
    }

    /**
     * 1. start ad video
     * 2. click Fullscreen Button
     * 3. will completed video Ad
     * 4. restart Ad when will be clicked replay button
     */
    @Test
    public void clickReplayButton_restartAd() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        VideoPlayer adVideoPlayer = getAdVideoPlayer(activity);

        // click full screen button
        onView(withId(R.id.fullscreen_button))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(300);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "click_replay-start-fullscreen-view");

        // completed video player
        waitPlayerUntilPaused(adVideoPlayer);
        assertThat(adVideoPlayer.isPlaying()).isFalse();
        takeScreenshot(instrumentation, activity, "click_replay-completed-view");

        // click replay button
        onView(withId(R.id.replay_button))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(2000);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "click_replay-restart-view");
    }
}
