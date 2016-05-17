package jp.co.dac.sdk.ma.sample;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.co.dac.sdk.ma.sample.TestUtil.takeScreenshot;
import static jp.co.dac.sdk.ma.sample.TestUtil.waitPlayerUntilPlayed;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SkippableVASTTest {

    private static final String adTagUrl = "https://saxp.zedo.com/jsc/xp2/fns.vast?n=2696&c=27/11&d=17&s=2&v=vast2&pu=__page-url__&ru=__referrer__&pw=__player-width__&ph=__player-height__&z=__random-number__";

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class, false, false);

    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.putExtra("ad_tag_url", adTagUrl);
        activity = activityRule.launchActivity(intent);
        instrumentation.waitForIdleSync();
    }

    /**
     * 1. start ad video
     * 2. will show skip button after 5 seconds
     * 3. skip Ad and start content when clicked skip button
     */
    @Test
    public void clickSkipButton_sendTrackingEvent() throws Exception {
        waitPlayerUntilPlayed((DACVideoPlayerView) activity.findViewById(R.id.ad_video_player));
        onView(withId(R.id.skip_button))
                .check(matches(not(isDisplayed())));
        takeScreenshot(instrumentation, activity, "clickSkipButton_sendTrackingEvent-not-show-skippable-button");

        Thread.sleep(3_000);
        onView(withId(R.id.skip_button))
                .check(matches(not(isDisplayed())));
        takeScreenshot(instrumentation, activity, "clickSkipButton_sendTrackingEvent-not-show-skippable-button-2");

        // will display skip button
        Thread.sleep(2_000);
        onView(withId(R.id.skip_button))
                .check(matches(isDisplayed()));
        takeScreenshot(instrumentation, activity, "clickSkipButton_sendTrackingEvent-show-skippable-button");

        // click skip button
        onView(withId(R.id.skip_button))
                .perform(click());
        Thread.sleep(100);
        takeScreenshot(instrumentation, activity, "clickSkipButton_sendTrackingEvent-clicked");
    }
}
