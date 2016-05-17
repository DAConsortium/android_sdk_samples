package jp.co.dac.sdk.ma.sample;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;
import static jp.co.dac.sdk.ma.sample.TestUtil.getAdVideoPlayer;
import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
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
    }

    /**
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
        Thread.sleep(100);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "start-fullscreen-view");

        // click close button
        onView(withId(R.id.close))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(100);
        assertThat(adVideoPlayer.isPlaying()).isTrue();
        takeScreenshot(instrumentation, activity, "comeback-starting-view-from-fullscreen-view");
    }
}
