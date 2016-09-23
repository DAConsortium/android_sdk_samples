package jp.co.dac.sdk.fv.sample;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.dac.sdk.fv.widget.DACSDKMAAdVideoPlayer;

public class MainFragment extends Fragment {

    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String ARG_AD_TAG_URL = "ARG_AD_TAG_URL";
    private static final String ARG_AD_SCROLL_OUT_PERCENT = "ARG_AD_SCROLL_OUT_PERCENT";

    private boolean isInitialize = false;

    public static MainFragment newInstance(String adTagUrl, int scrollOutPercent) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AD_TAG_URL, adTagUrl);
        args.putInt(ARG_AD_SCROLL_OUT_PERCENT, scrollOutPercent);
        fragment.setArguments(args);

        return fragment;
    }

    private DACSDKMAAdVideoPlayer videoPlayerContainer;

    private String adTagUrl;
    private int adScrollOutPercent;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adTagUrl = getArguments().getString(ARG_AD_TAG_URL);
        adScrollOutPercent = getArguments().getInt(ARG_AD_SCROLL_OUT_PERCENT);

        return inflater.inflate(R.layout.fragment_main_basic, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoPlayerContainer = (DACSDKMAAdVideoPlayer) view.findViewById(R.id.video_container);

        // basic settings
        new DACSDKMAAdVideoPlayer.SettingsBuilder()
                .fixedMaxHeight(getContentHeight())
                .companionBanner((ViewGroup) view.findViewById(R.id.companion_ad_banner))
                .middleOutScreenPercent(adScrollOutPercent)
                .videoOrientationListener(new DACSDKMAAdVideoPlayer.VideoOrientationListener() {
                    @Override
                    public void onVideoSize(View containerView, int width, int height) {
                        Log.d("videoSize", width + "-" + height);
                    }
                })
                .closeButton(true)
                .apply(videoPlayerContainer);

        if (getUserVisibleHint()) {
            isInitialize = true;
            videoPlayerContainer.initialize(adTagUrl);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        videoPlayerContainer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        videoPlayerContainer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        videoPlayerContainer.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");

        new DACSDKMAAdVideoPlayer.SettingsBuilder()
                .fixedMaxHeight(getContentHeight())
                .apply(videoPlayerContainer);
        videoPlayerContainer.onChangeOrientation();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (videoPlayerContainer == null) {
            return;
        }

        if (isVisibleToUser && !isInitialize) {
            isInitialize = true;
            videoPlayerContainer.initialize(adTagUrl);
            videoPlayerContainer.onResume();
        } else if (isVisibleToUser && isInitialize) {
            videoPlayerContainer.onResume();
        } else if (!isVisibleToUser && isInitialize) {
            videoPlayerContainer.onPause();
        }
    }

    private int getContentHeight() {
        return getDisplayHeight() - getStatusBarHeight() - getActionBarHeight() - ((int) (50 * getDensity()));
    }

    private int getDisplayHeight() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        return 0;
    }
}
