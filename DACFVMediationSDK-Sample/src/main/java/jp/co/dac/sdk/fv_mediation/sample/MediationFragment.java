package jp.co.dac.sdk.fv_mediation.sample;

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

import jp.co.dac.ad.manager.DACAdContainerView;
import jp.co.dac.ad.manager.DACAdRequestManager;
import jp.co.dac.dacadssdk.client.MediationAdClient;
import jp.co.dac.sdk.fv.client.DACSDKMAAdVideoPlayerClient;
import jp.co.dac.sdk.fv.widget.DACSDKMAAdVideoPlayer;

public class MediationFragment extends Fragment {

    private static final String TAG = MediationFragment.class.getSimpleName();

    private static final String ARG_AD_TAG_URL = "ARG_AD_TAG_URL";
    private static final String ARG_AD_PLACEMENT_ID = "ARG_AD_PLACEMENT_ID";
    private static final String ARG_AD_WIDTH = "ARG_AD_WIDTH";
    private static final String ARG_AD_HEIGHT = "ARG_AD_HEIGHT";

    public static MediationFragment newInstance(String adTagUrl, int placementId, int width, int height) {
        MediationFragment fragment = new MediationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AD_TAG_URL, adTagUrl);
        args.putInt(ARG_AD_PLACEMENT_ID, placementId);
        args.putInt(ARG_AD_WIDTH, width);
        args.putInt(ARG_AD_HEIGHT, height);
        fragment.setArguments(args);

        return fragment;
    }

    private DACAdContainerView dacAdContainerView;

    private String adTagUrl;
    private int mediationPlacementId;
    private int mediationWidth;
    private int mediationHeight;

    public MediationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adTagUrl = getArguments().getString(ARG_AD_TAG_URL);
        mediationPlacementId = getArguments().getInt(ARG_AD_PLACEMENT_ID);
        mediationWidth = getArguments().getInt(ARG_AD_WIDTH);
        mediationHeight = getArguments().getInt(ARG_AD_HEIGHT);

        return inflater.inflate(R.layout.fragment_mediation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        dacAdContainerView = (DACAdContainerView) view.findViewById(R.id.ad_container);

        DACSDKMAAdVideoPlayerClient dacSDKMAAdVideoPlayerClient = new DACSDKMAAdVideoPlayerClient.Builder(getContext(), adTagUrl)
                .fixedMaxHeight(getContentHeight()) // 最大の高さを設定
                .companionBanner((ViewGroup) view.findViewById(R.id.companion_ad_banner)) // 動画の下にバナー広告を表示する
                // Videoのサイズを受け取る
                .videoOrientationListener(new DACSDKMAAdVideoPlayer.VideoOrientationListener() {
                    @Override
                    public void onVideoSize(View containerView, int width, int height) {
                        Log.d("videoSize", width + "-" + height);
                    }
                })
                .middleOutScreenPercent(50) // 動画を再生/停止する範囲(0~100%)を指定
                .closeButton(true)          // 閉じるボタンを表示/非表示にする
                .build();

        MediationAdClient mediationAdClient = new MediationAdClient.Builder(
                    getContext(), mediationPlacementId, mediationWidth, mediationHeight)
                .build();

        final DACAdRequestManager manager = new DACAdRequestManager.Builder()
                .addClient(dacSDKMAAdVideoPlayerClient) // first try: video ad
                .addClient(mediationAdClient)           // second try: mediation(banner) ad
                .clientCallback(new DACAdRequestManager.ClientCallback() {
                    @Override
                    public void success(View adView, int adIndex) {
                        Log.d(TAG, "ad-onSuccess:" + adIndex);
                    }

                    @Override
                    public void failure() {
                        Log.d(TAG, "all-ad-failure");
                    }
                })
                .build();
        dacAdContainerView.loadAd(manager);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        dacAdContainerView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        dacAdContainerView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        dacAdContainerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        dacAdContainerView.onChangeOrientation();
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
