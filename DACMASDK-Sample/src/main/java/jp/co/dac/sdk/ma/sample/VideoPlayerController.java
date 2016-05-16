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
import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;

public class VideoPlayerController implements DACMASDKAdErrorEvent.AdErrorListener,
        DACMASDKAdsLoader.AdsLoadedListener, DACMASDKAdEvent.AdEventListener {

    private static final String TAG = VideoPlayerController.class.getSimpleName();

    protected final DACMASDKAdsLoader adsLoader;
    protected final VideoPlayerWithAdPlayback videoPlayerPlayback;
    protected final DACMASDKFactory dacMaSdkFactory;
    protected final String adTagUrl;

    protected DACMASDKAdsManager adsManager;
    protected DACMASDKAdDisplayContainer adDisplayContainer;

    private boolean isAllAdsCompleted = false;

    /**
     * SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
     */
    public VideoPlayerController(Context context, VideoPlayerWithAdPlayback videoPlayerWithAdPlayback, String adTagUrl) {
        this.videoPlayerPlayback = videoPlayerWithAdPlayback;
        this.adTagUrl = adTagUrl;

        dacMaSdkFactory = DACMASDKFactory.getInstance();
        adsLoader = dacMaSdkFactory.createAdsLoader(context);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);
    }

    /**
     * AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
     */
    private void requestAds(String adTagUrl) {
        adDisplayContainer = dacMaSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setPlayer(videoPlayerPlayback.getVideoAdPlayer());
        adDisplayContainer.setAdContainer(videoPlayerPlayback);
        adDisplayContainer.showAdPosition();
        adDisplayContainer.setExtensionPlayer(videoPlayerPlayback.getVideoAdExtensionPlayer());

        List<DACMASDKCompanionAdSlot> companionAdSlots = new ArrayList<>();
        companionAdSlots.add(createMediaAdSlot());
        companionAdSlots.add(createCompanionBanner());

        adDisplayContainer.setCompanionSlots(companionAdSlots);

        DACMASDKAdsRequest request = dacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setAdVideoView(videoPlayerPlayback.getVideoPlayerContainer());
        request.setContentProgressProvider(videoPlayerPlayback.getContentProgressProvider());

        videoPlayerPlayback.setOnContentCompleteListener(new VideoPlayerWithAdPlayback.OnContentCompleteListener() {
            @Override
            public void onContentComplete() {
                adsLoader.contentComplete();
            }
        });

        adsLoader.requestAds(request);

        isAllAdsCompleted = false;
    }

    private DACMASDKCompanionAdSlot createMediaAdSlot() {
        DACMASDKCompanionAdSlot mediaAdSlot = dacMaSdkFactory.createCompanionAdSlot();
        mediaAdSlot.setContainer(videoPlayerPlayback.getVideoPlayerImage());
        mediaAdSlot.setAppropriateCompanion(new DACMASDKCompanionAdSlot.AppropriateCompanion() {
            @Override
            public boolean isValidRange(int width, int height) {
                int s, l;
                if (width >= height) {
                    s = height;
                    l = width;
                } else {
                    s = width;
                    l = height;
                }

                // 3:4, 9:16のようなcompanionを受け入れる
                return ((float) l) / s < 2.0;
            }
        });

        return mediaAdSlot;
    }

    private DACMASDKCompanionAdSlot createCompanionBanner() {
        DACMASDKCompanionAdSlot companionAdSlot = dacMaSdkFactory.createCompanionAdSlot();
        companionAdSlot.setContainer(videoPlayerPlayback.getCompanionView());
        return companionAdSlot;
    }

    /**
     * 広告の読み込みが再生した際にAdsManagerを呼び出し、広告の再生を始める
     */
    @Override
    public void onAdsManagerLoaded(DACMASDKAdsManagerLoadedEvent adsManagerLoadedEvent) {
        adsManager = adsManagerLoadedEvent.getAdsManager();
        adsManager.addAdEventListener(this);
        adsManager.addAdErrorListener(this);
        adsManager.init();
    }

    /**
     * 広告再生中のイベント
     */
    @Override
    public void onAdEvent(DACMASDKAdEvent adEvent) {
        switch (adEvent.getType()) {
            case LOADED:
                if (adsManager != null) {
                    videoPlayerPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                isAllAdsCompleted = false;
                break;
            case CONTENT_RESUME_REQUESTED:
                resumeContent();
                break;
            case CONTENT_PAUSE_REQUESTED:
                pauseContent();
                break;
            case ALL_ADS_COMPLETED:
                if (adsManager != null && !videoPlayerPlayback.hasContent()) {
                    adsManager.destroy();
                    adsManager = null;
                }
                isAllAdsCompleted = true;
                break;
            default:
                break;
        }

        videoPlayerPlayback.setAllAdsCompleted(isAllAdsCompleted);
    }

    private void pauseContent() {
        videoPlayerPlayback.pauseContentForAdPlayback();
    }

    private void resumeContent() {
        videoPlayerPlayback.resumeContentAfterAdPlayback();
    }

    public void setContentVideoPlayer(DACVideoPlayerView videoPlayerView, String contentUrl) {
        videoPlayerPlayback.setContentVideoPlayer(videoPlayerView, contentUrl);
    }

    @Override
    public void onAdError(DACMASDKAdErrorEvent adErrorEvent) {
        // hide ad View
        videoPlayerPlayback.setVisibility(View.GONE);
        resumeContent();
    }

    void play() {
        requestAds(adTagUrl);
    }

    void resume() {
        videoPlayerPlayback.restorePosition();
        if (adsManager != null &&
                !videoPlayerPlayback.isAdCompleted() &&
                videoPlayerPlayback.inScroll()) {
            adsManager.resume();
        }
        videoPlayerPlayback.resumeContent();
    }

    void pause() {
        videoPlayerPlayback.savePosition();
        if (adsManager != null &&
                !videoPlayerPlayback.isAdCompleted()) {
            adsManager.pause();
        }
        videoPlayerPlayback.pauseContent();
    }

    void destroy() {
        videoPlayerPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
        }
        videoPlayerPlayback.pauseContent();
    }
}
