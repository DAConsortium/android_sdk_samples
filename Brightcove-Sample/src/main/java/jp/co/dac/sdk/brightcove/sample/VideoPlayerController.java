package jp.co.dac.sdk.brightcove.sample;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;

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

    private final ViewGroup parentView;
    private final EventEmitter eventEmitter;
    private final VideoPlayerWithAdPlayback videoPlayerContentPlayback;

    private final DACMASDKAdsLoader adsLoader;
    private final DACMASDKFactory dacMaSdkFactory;
    private final String defaultAdTagUrl;

    private boolean isPresentingAd = false;
    private boolean isPlayingContentVideo = false;

    private DACMASDKAdsManager adsManager;
    private DACMASDKAdDisplayContainer adDisplayContainer;

    ViewGroup adCompanionBanner;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public VideoPlayerController(ViewGroup parentView,
                                 EventEmitter eventEmitter,
                                 VideoPlayerWithAdPlayback videoPlayerWithAdPlayback) {
        this.parentView = parentView;
        this.eventEmitter = eventEmitter;

        Context context = parentView.getContext();

        videoPlayerContentPlayback = videoPlayerWithAdPlayback;
        defaultAdTagUrl = context.getString(R.string.ad_tag_url);

        dacMaSdkFactory = DACMASDKFactory.getInstance();
        adsLoader = dacMaSdkFactory.createAdsLoader(context);
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);
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
        Log.d(TAG, "onAdEvent:" + adEvent.getType());

        switch (adEvent.getType()) {
            case LOADED:
                if (adsManager != null) {
                    videoPlayerContentPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                break;
            case STARTED:
                isPresentingAd = true;
                eventEmitter.emit(MAAdPlayerEvent.DID_START_AD);
                break;
            case RESUMED:
                isPresentingAd = true;
                eventEmitter.emit(MAAdPlayerEvent.DID_RESUME_AD);
                break;
            case PAUSED:
                eventEmitter.emit(MAAdPlayerEvent.DID_PAUSE_AD);
                break;
            case CONTENT_RESUME_REQUESTED:
                onContentResumeRequested();
                break;
            case CONTENT_PAUSE_REQUESTED:
                onContentPauseRequested();
                break;
            case COMPLETED:
                eventEmitter.emit(MAAdPlayerEvent.DID_COMPLETE_AD);
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
        if (videoPlayerContentPlayback.getParent() != null) {
            parentView.removeView(videoPlayerContentPlayback);
        }
        eventEmitter.emit(MAAdPlayerEvent.DID_FAIL_TO_PLAY_AD);
    }

    public void onContentResumeRequested() {
        if (isPresentingAd) {
            willResumeContent();
        }
    }

    private void willResumeContent() {
        Log.d(TAG, "willResumeContent");

        isPresentingAd = false;
        if (videoPlayerContentPlayback.getParent() != null) {
            parentView.removeView(videoPlayerContentPlayback);
        }

        eventEmitter.emit(EventType.WILL_RESUME_CONTENT);
    }

    public void onContentPauseRequested() {
        Log.d(TAG, "onContentPauseRequested");

        if (videoPlayerContentPlayback.getParent() == null) {
            parentView.addView(videoPlayerContentPlayback);
        }

        isPresentingAd = true;
        if (isPlayingContentVideo) {
            eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);
            isPlayingContentVideo = false;
        }
    }

    void init() {
        eventEmitter.on(EventType.CUE_POINT, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("init-cue-point", "init-cue-point");
                internalInitializeAdsRequests();
            }
        });

        eventEmitter.on(EventType.DID_PLAY, new EventListener() {
            @Override
            public void processEvent(Event event) {
                isPlayingContentVideo = true;
                if (isPresentingAd) {
                    adsManager.pause();
                    isPresentingAd = false;
                }
            }
        });

        eventEmitter.on(EventType.DID_PAUSE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                isPlayingContentVideo = false;
            }
        });

        eventEmitter.on(MAAdPlayerEvent.FORCE_CLOSE_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                videoPlayerContentPlayback.closeAd();
            }
        });
    }

    private void internalInitializeAdsRequests() {
        if (isPresentingAd) return;

        eventEmitter.emit(MAAdPlayerEvent.ADS_REQUEST_FOR_VIDEO);
    }

    void play() {
        requestAds(defaultAdTagUrl);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(final String adTagUrl) {
        Log.d(TAG, "requestAds");
        if (adsManager != null) {
            adsManager.destroy();
            adsManager = null;
        }

        videoPlayerContentPlayback.init();

        adDisplayContainer = dacMaSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setPlayer(videoPlayerContentPlayback.getVideoAdPlayer());

        if (adCompanionBanner != null) {
            List<DACMASDKCompanionAdSlot> companionAdSlots = new ArrayList<>();
            DACMASDKCompanionAdSlot companionAdSlot = DACMASDKFactory.getInstance().createCompanionAdSlot();
            companionAdSlot.setContainer(adCompanionBanner);
            companionAdSlots.add(companionAdSlot);
            adDisplayContainer.setCompanionSlots(companionAdSlots);
        }

        DACMASDKAdsRequest request = dacMaSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setAdVideoView(videoPlayerContentPlayback.getVideoPlayer());

        adsLoader.requestAds(request);
        isPresentingAd = false;
    }

    void resume() {
        videoPlayerContentPlayback.restorePosition();
        if (adsManager != null && isPresentingAd) {
            adsManager.resume();
        }
    }

    void pause() {
        videoPlayerContentPlayback.savePosition();
        if (adsManager != null && isPresentingAd) {
            adsManager.pause();
        }
    }

    void destroy() {
        videoPlayerContentPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
            adsManager = null;
        }
    }
}
