package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.util.Log;

import jp.co.dac.ma.sdk.api.DACMASDKAdDisplayContainer;
import jp.co.dac.ma.sdk.api.DACMASDKAdErrorEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsLoader;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManagerLoadedEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsRequest;
import jp.co.dac.ma.sdk.api.DACMASDKFactory;

public class ContentVideoPlayerController implements DACMASDKAdErrorEvent.AdErrorListener,
        DACMASDKAdsLoader.AdsLoadedListener, DACMASDKAdEvent.AdEventListener,
        VideoPlayerContentWithAdPlayback.OnContentCompleteListener {

    private static final String TAG = ContentVideoPlayerController.class.getSimpleName();

    private final DACMASDKAdsLoader mAdsLoader;
    private final VideoPlayerContentWithAdPlayback mVideoPlayerWithAdPlayback;
    private final String mDefaultAdTagUrl;
    private final DACMASDKFactory mDacMaSdkFactory;

    private DACMASDKAdsManager mAdsManager;
    private DACMASDKAdDisplayContainer mAdDisplayContainer;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public ContentVideoPlayerController(Context context, VideoPlayerContentWithAdPlayback videoPlayerWithAdPlayback){
        mVideoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
        mVideoPlayerWithAdPlayback.setOnContentCompleteListener(this);
        mDefaultAdTagUrl = context.getString(R.string.ad_tag_url);

        mDacMaSdkFactory = DACMASDKFactory.getInstance();
        mAdsLoader = mDacMaSdkFactory.createAdsLoader(context);
        mAdsLoader.addAdsLoadedListener(this);
        mAdsLoader.addAdErrorListener(this);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(String adTagUrl) {
        mAdDisplayContainer = mDacMaSdkFactory.createAdDisplayContainer();
        mAdDisplayContainer.setPlayer(mVideoPlayerWithAdPlayback.getVideoAdPlayer());

        DACMASDKAdsRequest request = mDacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(mAdDisplayContainer);
        request.setContentProgressProvider(mVideoPlayerWithAdPlayback.getContentProgressProvider());
        request.setAdVideoView(mVideoPlayerWithAdPlayback.getVideoPlayerContainer());

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

    //広告再生中のイベント。
    @Override
    public void onAdEvent(DACMASDKAdEvent adEvent) {
        Log.d(TAG, "onAdEvent: " + adEvent.getType());

        switch (adEvent.getType()) {
            case LOADED:
                if (mAdsManager != null) {
                    mVideoPlayerWithAdPlayback.setAdsManager(mAdsManager);
                    mAdsManager.start();
                }
                break;
            case CONTENT_PAUSE_REQUESTED:
                mVideoPlayerWithAdPlayback.pauseContentForAdPlayback();
                break;
            case CONTENT_RESUME_REQUESTED:
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
        Log.e(TAG, "DACMASDKAd Error: " + adErrorEvent.getError().getMessage());
    }

    public void onContentComplete() {
        mAdsLoader.contentComplete();
    }

    public void setContentVideo(String videoPath) {
        mVideoPlayerWithAdPlayback.setContentVideoPath(videoPath);
    }

    public void play() {
        requestAds(mDefaultAdTagUrl);
    }

    public void resume() {
        mVideoPlayerWithAdPlayback.restorePosition();
        if (mAdsManager != null && !mVideoPlayerWithAdPlayback.isAdCompleted()) {
            mAdsManager.resume();
        }
    }

    public void destroy() {
        mVideoPlayerWithAdPlayback.restorePosition();
        if (mAdsManager != null) {
            mAdsManager.destroy();
        }
    }

    public void pause() {
        mVideoPlayerWithAdPlayback.savePosition();
        if (mAdsManager != null && !mVideoPlayerWithAdPlayback.isAdCompleted()) {
            mAdsManager.pause();
        }
    }
}
