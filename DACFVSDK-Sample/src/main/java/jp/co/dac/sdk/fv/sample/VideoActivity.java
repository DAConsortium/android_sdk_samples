package jp.co.dac.sdk.fv.sample;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.sdk.fv.sample.databinding.ActivityVideoBinding;
import jp.co.dac.sdk.fv.widget.DACSDKMAAdVideoPlayer;

public class VideoActivity extends AppCompatActivity {

    private static final String ARG_AD_TAG_URL = "ARG_AD_TAG_URL";
    private static final String ARG_AD_HEADER_IMG_SRC = "ARG_AD_HEADER_IMG_SRC";
    private static final String ARG_AD_AUTO_CLOSE = "ARG_AD_AUTO_CLOSE";
    private static final String ARG_AD_INVIEW_PERCENT = "ARG_AD_INVIEW_PERCENT";
    private static final String ARG_AD_IS_INTERSTITIAL = "ARG_AD_IS_INTERSTITIAL";
    private static final String ARG_AD_IS_CLOSE_BUTTON = "ARG_AD_IS_CLOSE_BUTTON";
    private static final String ARG_AD_IS_COMPANION_BANNER = "ARG_AD_IS_COMPANION_BANNER";
    private static final String ARG_AD_IS_SCROLL_STOP = "ARG_AD_IS_SCROLL_STOP";
    private static final String ARG_AD_IS_FIRST_VIEW = "ARG_AD_IS_FIRST_VIEW";

    static Intent getCallingIntent(Context context, String adTagUrl, String headerImgSrc,
                                   int autoClose, int inViewPercent,
                                   boolean interstitial, boolean closeButton,
                                   boolean isCompanionBanner, boolean isScrollStop, boolean isFirstView) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(ARG_AD_TAG_URL, adTagUrl);
        intent.putExtra(ARG_AD_HEADER_IMG_SRC, headerImgSrc);
        intent.putExtra(ARG_AD_AUTO_CLOSE, autoClose);
        intent.putExtra(ARG_AD_INVIEW_PERCENT, inViewPercent);
        intent.putExtra(ARG_AD_IS_INTERSTITIAL, interstitial);
        intent.putExtra(ARG_AD_IS_CLOSE_BUTTON, closeButton);
        intent.putExtra(ARG_AD_IS_COMPANION_BANNER, isCompanionBanner);
        intent.putExtra(ARG_AD_IS_SCROLL_STOP, isScrollStop);
        intent.putExtra(ARG_AD_IS_FIRST_VIEW, isFirstView);
        return intent;
    }

    private DACSDKMAAdVideoPlayer videoContainer;

    private ActivityVideoBinding binding;

    private String adTagUrl;
    private String headerImgSrc;
    private int autoClose;
    private int inViewPercent;
    private boolean isInterstitial;
    private boolean isCloseButton;
    private boolean isCompanionBanner;
    private boolean isScrollStop;
    private boolean isFirstView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adTagUrl = getIntent().getStringExtra(ARG_AD_TAG_URL);
        headerImgSrc = getIntent().getStringExtra(ARG_AD_HEADER_IMG_SRC);
        autoClose = getIntent().getIntExtra(ARG_AD_AUTO_CLOSE, 0);
        inViewPercent = getIntent().getIntExtra(ARG_AD_INVIEW_PERCENT, 0);
        isInterstitial = getIntent().getBooleanExtra(ARG_AD_IS_INTERSTITIAL, false);
        isCompanionBanner = getIntent().getBooleanExtra(ARG_AD_IS_COMPANION_BANNER, false);
        isCloseButton = getIntent().getBooleanExtra(ARG_AD_IS_CLOSE_BUTTON, false);
        isScrollStop = getIntent().getBooleanExtra(ARG_AD_IS_SCROLL_STOP, false);
        isFirstView = getIntent().getBooleanExtra(ARG_AD_IS_FIRST_VIEW, true);

        final ViewGroup videoParentView;
        final DACVideoPlayerView playerView;
        final ViewGroup companionAdBanner;
        if (isFirstView) {
            videoParentView = binding.content.firstviewAd;
            videoContainer = binding.content.videoContainer;
            playerView = binding.content.videoPlayer;
            companionAdBanner = binding.content.companionAdBanner;
        } else {
            videoParentView = binding.content.noFirstviewAd;
            videoContainer = binding.content.videoContainer2;
            playerView = binding.content.videoPlayer2;
            companionAdBanner = binding.content.companionAdBanner2;
        }

        // set video params
        DACSDKMAAdVideoPlayer.SettingsBuilder settings = new DACSDKMAAdVideoPlayer.SettingsBuilder()
                .closeButton(isCloseButton)
                .scrollStop(isScrollStop, videoParentView)
                .middleOutScreenPercent(inViewPercent)
                .videoOrientationListener(new DACSDKMAAdVideoPlayer.VideoOrientationListener() {
                    @Override
                    public void onVideoSize(View containerView, int width, int height) {
                        Log.d("videoSize", width + "-" + height);
                    }
                })
                .autoClose(autoClose, TimeUnit.SECONDS);
        if (adTagUrl.equals("http://54.178.193.21:10080/tategumi.xml")) {
            // 縦型広告の場合
            settings.interstitial(isInterstitial, DACSDKMAAdVideoPlayer.INTERSTITIAL_VERTICAL);
        } else {
            // 横型広告の場合
            settings.interstitial(isInterstitial, DACSDKMAAdVideoPlayer.INTERSTITIAL_HORIZONTAL);
        }
        if (!isInterstitial) {
            settings = settings.fixedMaxHeight(getContentHeight() - 200);
        }
        if (isCompanionBanner) {
            settings = settings.companionBanner(companionAdBanner);
        }
        if (!TextUtils.isEmpty(headerImgSrc)) {
            settings = settings.headerImgSrc(headerImgSrc);
        }
        settings.apply(videoContainer);

        videoContainer.initialize(adTagUrl, playerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        videoContainer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        videoContainer.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        videoContainer.onDestroy();
    }

    private int getContentHeight() {
        return getDisplayHeight() - getStatusBarHeight() - getActionBarHeight() - ((int) (50 * getDensity()));
    }

    private int getDisplayHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    private float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    private int getActionBarHeight() {
        final TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        return 0;
    }
}
