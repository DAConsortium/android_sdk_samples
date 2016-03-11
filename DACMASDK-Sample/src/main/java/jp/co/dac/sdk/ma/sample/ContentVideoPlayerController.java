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
import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;

public class ContentVideoPlayerController implements DACMASDKAdErrorEvent.AdErrorListener,
        DACMASDKAdsLoader.AdsLoadedListener, DACMASDKAdEvent.AdEventListener,
        VideoPlayerContentWithAdPlayback.OnContentCompleteListener {

    private static final String TAG = ContentVideoPlayerController.class.getSimpleName();

    private final DACMASDKAdsLoader adsLoader;
    private final VideoPlayerContentWithAdPlayback videoPlayerAdPlayback;
    private final String adTagUrl;
    private final DACMASDKFactory dacMaSdkFactory;

    private DACMASDKAdsManager adsManager;
    private DACMASDKAdDisplayContainer adDisplayContainer;

    private boolean isAllAdCompleted = false;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public ContentVideoPlayerController(Context context, VideoPlayerContentWithAdPlayback videoPlayerAdPlayback, String adTagUrl){
        this.videoPlayerAdPlayback = videoPlayerAdPlayback;
        this.videoPlayerAdPlayback.setOnContentCompleteListener(this);
        this.adTagUrl = adTagUrl;

        dacMaSdkFactory = DACMASDKFactory.getInstance();
        adsLoader = dacMaSdkFactory.createAdsLoader(context);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);

        isAllAdCompleted = false;
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(String adTagUrl) {
        adDisplayContainer = dacMaSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setExtensionPlayer(videoPlayerAdPlayback.getVideoAdExtensionPlayer());
        adDisplayContainer.setPlayer(videoPlayerAdPlayback.getVideoAdPlayer());

        DACMASDKAdsRequest request = dacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setContentProgressProvider(videoPlayerAdPlayback.getContentProgressProvider());
        request.setAdVideoView(videoPlayerAdPlayback.getVideoPlayerContainer());
        request.setContentProgressProvider(videoPlayerAdPlayback.getContentProgressProvider());

        videoPlayerAdPlayback.setOnContentCompleteListener(new VideoPlayerContentWithAdPlayback.OnContentCompleteListener() {
            @Override
            public void onContentComplete() {
                adsLoader.contentComplete();
            }
        });

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

    //広告再生中のイベント。
    @Override
    public void onAdEvent(DACMASDKAdEvent adEvent) {
        Log.d(TAG, "onAdEvent: " + adEvent.getType());

        switch (adEvent.getType()) {
            case LOADED:
                if (adsManager != null) {
                    videoPlayerAdPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                break;
            case CONTENT_RESUME_REQUESTED:
                resumeContent();
                break;
            case CONTENT_PAUSE_REQUESTED:
                pauseContent();
                break;
            case ALL_ADS_COMPLETED:
                if (adsManager != null) {
                    adsManager.destroy();
                    adsManager = null;
                }
                isAllAdCompleted = true;
                break;
            default:
                break;
        }
        videoPlayerAdPlayback.setAllAdCompleted(isAllAdCompleted);
    }

    private void pauseContent() {
        videoPlayerAdPlayback.pauseContentForAdPlayback();
    }

    private void resumeContent() {
        videoPlayerAdPlayback.resumeContentAfterAdPlayback();
    }

    @Override
    public void onAdError(DACMASDKAdErrorEvent adErrorEvent) {
        Log.e(TAG, "DACMASDKAd Error: " + adErrorEvent.getError().getMessage());
    }

    public void onContentComplete() {
        adsLoader.contentComplete();
    }

    public void setContentVideoPlayer(DACVideoPlayerView videoPlayerView, String contentUrl) {
        videoPlayerAdPlayback.setContentVideoPlayer(videoPlayerView, contentUrl);
    }


    public void play() {
        requestAds(adTagUrl);
    }

    public void resume() {
        videoPlayerAdPlayback.restorePosition();
        if (adsManager != null && !videoPlayerAdPlayback.isAdCompleted()) {
            adsManager.resume();
        }

        videoPlayerAdPlayback.resumeContent();
    }

    public void destroy() {
        videoPlayerAdPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
        }

        videoPlayerAdPlayback.pauseContent();
    }

    public void pause() {
        videoPlayerAdPlayback.savePosition();
        if (adsManager != null && !videoPlayerAdPlayback.isAdCompleted()) {
            adsManager.pause();
        }

        videoPlayerAdPlayback.pauseContent();
    }
}
