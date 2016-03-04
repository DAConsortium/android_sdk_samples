package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import jp.co.dac.ma.sdk.api.DACMASDKAdDisplayContainer;
import jp.co.dac.ma.sdk.api.DACMASDKAdErrorEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsLoader;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManagerLoadedEvent;
import jp.co.dac.ma.sdk.api.DACMASDKAdsRequest;
import jp.co.dac.ma.sdk.api.DACMASDKCompanionAdSlot;
import jp.co.dac.ma.sdk.api.DACMASDKFactory;

public class VideoPlayerController implements DACMASDKAdErrorEvent.AdErrorListener,
        DACMASDKAdsLoader.AdsLoadedListener, DACMASDKAdEvent.AdEventListener {

    private static final String TAG = VideoPlayerController.class.getSimpleName();

    protected final DACMASDKAdsLoader adsLoader;
    protected final VideoPlayerWithAdPlayback videoPlayerPlayback;
    protected final String defaultAdTagUrl;
    protected final DACMASDKFactory dacMaSdkFactory;

    protected DACMASDKAdsManager adsManager;
    protected DACMASDKAdDisplayContainer adDisplayContainer;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public VideoPlayerController(Context context, VideoPlayerWithAdPlayback videoPlayerWithAdPlayback){
        videoPlayerPlayback = videoPlayerWithAdPlayback;
        defaultAdTagUrl = context.getString(R.string.ad_tag_url);

        dacMaSdkFactory = DACMASDKFactory.getInstance();
        adsLoader = dacMaSdkFactory.createAdsLoader(context);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(String adTagUrl) {
        adDisplayContainer = dacMaSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setPlayer(videoPlayerPlayback.getVideoAdPlayer());
        adDisplayContainer.setAdContainer(videoPlayerPlayback);
        adDisplayContainer.showAdPosition();
        adDisplayContainer.setExtensionPlayer(videoPlayerPlayback.getVideoAdExtensionPlayer());

        List<DACMASDKCompanionAdSlot> companionAdSlots = new ArrayList<>();
        DACMASDKCompanionAdSlot companionAdSlot = dacMaSdkFactory.createCompanionAdSlot();
        companionAdSlot.setContainer(videoPlayerPlayback.getCompanionView());
        companionAdSlots.add(companionAdSlot);
        adDisplayContainer.setCompanionSlots(companionAdSlots);

        DACMASDKAdsRequest request = dacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setAdVideoView(videoPlayerPlayback.getVideoPlayerContainer());

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
                    videoPlayerPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                break;
            case CONTENT_PAUSE_REQUESTED:
                videoPlayerPlayback.pauseContentForAdPlayback();
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
        videoPlayerPlayback.setVisibility(View.GONE);
    }

    void play() {
        requestAds(defaultAdTagUrl);
    }

    void resume() {
        videoPlayerPlayback.restorePosition();
        if (adsManager != null &&
                !videoPlayerPlayback.isAdCompleted() &&
                videoPlayerPlayback.inScroll()) {
            adsManager.resume();
        }
    }

    void destroy() {
        videoPlayerPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
        }
    }

    void pause() {
        videoPlayerPlayback.savePosition();
        if (adsManager != null &&
                !videoPlayerPlayback.isAdCompleted()) {
            adsManager.pause();
        }
    }
}
