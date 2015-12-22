package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.view.View;

import jp.co.dac.ma.sdk.api.DACMASDKAdDisplayContainer;
import jp.co.dac.ma.sdk.api.DACMASDKAdErrorEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsLoader;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManagerLoadedEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsRequest;
import jp.co.dac.ma.sdk.api.DACMASDKFactory;

public class VideoPlayerController implements DACMASDKAdErrorEvent.AdErrorListener,
        DACMASDKAdsLoader.AdsLoadedListener, DACMASDKAdEvent.AdEventListener {

    private static final String TAG = VideoPlayerController.class.getSimpleName();

    private final DACMASDKAdsLoader mAdsLoader;
    private final VideoPlayerWithAdPlayback mVideoPlayerNoContentPlayback;
    private final String mDefaultAdTagUrl;
    private final DACMASDKFactory mDacMaSdkFactory;

    private DACMASDKAdsManager mAdsManager;
    private DACMASDKAdDisplayContainer mAdDisplayContainer;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public VideoPlayerController(Context context, VideoPlayerWithAdPlayback videoPlayerWithAdPlayback){
        mVideoPlayerNoContentPlayback = videoPlayerWithAdPlayback;
        mDefaultAdTagUrl = context.getString(R.string.ad_tag_url);

        mDacMaSdkFactory = DACMASDKFactory.getInstance();
        mAdsLoader = mDacMaSdkFactory.createAdsLoader(context);
        mAdsLoader.addAdsLoadedListener(this);
        mAdsLoader.addAdErrorListener(this);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(String adTagUrl) {
        mAdDisplayContainer = mDacMaSdkFactory.createAdDisplayContainer();
        mAdDisplayContainer.setPlayer(mVideoPlayerNoContentPlayback.getVideoAdPlayer());

        DACMASDKAdsRequest request = mDacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(mAdDisplayContainer);
        request.setAdVideoView(mVideoPlayerNoContentPlayback.getVideoPlayerContainer());

        mAdsLoader.requestAds(request);
    }

    // 広告の読み込みが再生した際にAdsManagerを呼び出し、広告の再生を始める
    @Override
    public void onAdsManagerLoaded(DACMASDKAdsManagerLoadedEvent adsManagerLoadedEvent) {
        mAdsManager = adsManagerLoadedEvent.getAdsManager();
        mAdsManager.addAdEventListener(this);
        mAdsManager.addAdErrorListener(this);
        mAdsManager.init();
    }

    // 広告再生中のイベント
    @Override
    public void onAdEvent(DACMASDKAdEvent adEvent) {
        switch (adEvent.getType()) {
            case LOADED:
                if (mAdsManager != null) {
                    mVideoPlayerNoContentPlayback.setAdsManager(mAdsManager);
                    mAdsManager.start();
                }
                break;
            case CONTENT_PAUSE_REQUESTED:
                mVideoPlayerNoContentPlayback.pauseContentForAdPlayback();
                break;
            case ALL_ADS_COMPLETED:
                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAdError(DACMASDKAdErrorEvent adErrorEvent) {
        // hide ad View
        mVideoPlayerNoContentPlayback.setVisibility(View.GONE);
    }

    void play() {
        requestAds(mDefaultAdTagUrl);
    }

    void resume() {
        mVideoPlayerNoContentPlayback.restorePosition();
        if (mAdsManager != null &&
                !mVideoPlayerNoContentPlayback.isAdCompleted() &&
                mVideoPlayerNoContentPlayback.inScroll()) {
            mAdsManager.resume();
        }
    }

    void destroy() {
        mVideoPlayerNoContentPlayback.restorePosition();
        if (mAdsManager != null) {
            mAdsManager.destroy();
        }
    }

    void pause() {
        mVideoPlayerNoContentPlayback.savePosition();
        if (mAdsManager != null &&
                !mVideoPlayerNoContentPlayback.isAdCompleted()) {
            mAdsManager.pause();
        }
    }
}
