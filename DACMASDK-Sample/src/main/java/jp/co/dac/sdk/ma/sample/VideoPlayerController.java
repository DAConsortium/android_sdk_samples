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

    protected final DACMASDKAdsLoader adsLoader;
    protected final VideoPlayerWithAdPlayback videoPlayerNoContentPlayback;
    protected final String defaultAdTagUrl;
    protected final DACMASDKFactory dacMaSdkFactory;

    protected DACMASDKAdsManager adsManager;
    protected DACMASDKAdDisplayContainer adDisplayContainer;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public VideoPlayerController(Context context, VideoPlayerWithAdPlayback videoPlayerWithAdPlayback){
        videoPlayerNoContentPlayback = videoPlayerWithAdPlayback;
        defaultAdTagUrl = context.getString(R.string.ad_tag_url);

        dacMaSdkFactory = DACMASDKFactory.getInstance();
        adsLoader = dacMaSdkFactory.createAdsLoader(context);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(String adTagUrl) {
        adDisplayContainer = dacMaSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setPlayer(videoPlayerNoContentPlayback.getVideoAdPlayer());
        adDisplayContainer.setExtensionPlayer(videoPlayerNoContentPlayback.getVideoAdExtensionPlayer());

        DACMASDKAdsRequest request = dacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setAdVideoView(videoPlayerNoContentPlayback.getVideoPlayerContainer());

        adsLoader.requestAds(request);
    }

    // 広告の読み込みが再生した際にAdsManagerを呼び出し、広告の再生を始める
    @Override
    public void onAdsManagerLoaded(DACMASDKAdsManagerLoadedEvent adsManagerLoadedEvent) {
        adsManager = adsManagerLoadedEvent.getAdsManager();
        adsManager.addAdEventListener(this);
        adsManager.addAdErrorListener(this);
        adsManager.init();
    }

    // 広告再生中のイベント
    @Override
    public void onAdEvent(DACMASDKAdEvent adEvent) {
        switch (adEvent.getType()) {
            case LOADED:
                if (adsManager != null) {
                    videoPlayerNoContentPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                break;
            case CONTENT_PAUSE_REQUESTED:
                videoPlayerNoContentPlayback.pauseContentForAdPlayback();
                break;
            case ALL_ADS_COMPLETED:
                if (adsManager != null) {
                    adsManager.destroy();
                    adsManager = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAdError(DACMASDKAdErrorEvent adErrorEvent) {
        // hide ad View
        videoPlayerNoContentPlayback.setVisibility(View.GONE);
    }

    void play() {
        requestAds(defaultAdTagUrl);
    }

    void resume() {
        videoPlayerNoContentPlayback.restorePosition();
        if (adsManager != null &&
                !videoPlayerNoContentPlayback.isAdCompleted() &&
                videoPlayerNoContentPlayback.inScroll()) {
            adsManager.resume();
        }
    }

    void destroy() {
        videoPlayerNoContentPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
        }
    }

    void pause() {
        videoPlayerNoContentPlayback.savePosition();
        if (adsManager != null &&
                !videoPlayerNoContentPlayback.isAdCompleted()) {
            adsManager.pause();
        }
    }
}
