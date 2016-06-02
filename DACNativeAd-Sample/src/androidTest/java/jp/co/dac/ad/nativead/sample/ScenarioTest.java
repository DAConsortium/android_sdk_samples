package jp.co.dac.ad.nativead.sample;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.FlakyTest;
import android.view.ViewGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import jp.co.dac.ad.nativead.DACNativeContentAdView;

import static android.app.Instrumentation.ActivityResult;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class ScenarioTest {

    private static final int TOLERANCE_WAIT_MILLIS = 5_000;

    private static final int VALID_PLACEMENT_ID = 31332;
    private static final int INVALID_PLACEMENT_ID = 99999;

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
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

    @Test
    @FlakyTest(tolerance = 3)
    public void 正しいURLの時_記事が表示される() throws Exception {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.prepareWebView(VALID_PLACEMENT_ID);
            }
        });
        Thread.sleep(TOLERANCE_WAIT_MILLIS);

        ViewGroup adPlaceholder = activity.binding.adPlaceholder;
        assertThat(adPlaceholder.getChildCount()).isEqualTo(1);

        DACNativeContentAdView adContent = (DACNativeContentAdView) adPlaceholder.getChildAt(0);
        assertThat(adContent).isEqualTo(activity.contentBinding.getRoot());

        assertThat(activity.contentAd.getTitle().length()).isGreaterThan(5);
        assertThat(activity.contentBinding.title.getText().length()).isGreaterThan(5);

        assertThat(activity.contentAd.getAdvertiser().length()).isGreaterThan(5);
        assertThat(activity.contentBinding.advertiser.getText().length()).isGreaterThan(5);

        assertThat(activity.contentAd.getDescription().length()).isGreaterThan(5);
        assertThat(activity.contentBinding.description.getText().length()).isGreaterThan(5);

        assertThat(activity.contentAd.getImageUrl()).startsWith("http");
        assertThat(activity.contentBinding.image.getDrawable()).isNotNull();
    }

    @Ignore
    @Test
    @FlakyTest(tolerance = 3)
    public void 記事の読み込みに失敗した時_エラーがコールバックされる() throws Exception {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.prepareWebView(INVALID_PLACEMENT_ID);
            }
        });
        Thread.sleep(30_000);

        assertThat(activity.adException).isNotNull();
    }

    @Test
    @FlakyTest(tolerance = 3)
    public void 記事をクリックした時_ブラウザを開く() throws Exception {
        Intent finishIntent = new Intent();
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, finishIntent);
        Intents.intending(allOf(hasAction(Intent.ACTION_VIEW)))
                .respondWith(result);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.prepareWebView(VALID_PLACEMENT_ID);
            }
        });
        Thread.sleep(TOLERANCE_WAIT_MILLIS);

        onView(withId(R.id.content_ad))
                .perform(click());
        Intents.intended(hasData((String) activity.contentAd.get("click_url")));
    }
}
