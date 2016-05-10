package jp.co.dac.sdk.brightcove.sample;

import android.util.Log;
import android.view.ViewGroup;

import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final VideoPlayerWithAdPlayback videoPlayerWithAdPlayback;

    private final DACMASDKAdsLoader adsLoader;
    private final DACMASDKFactory sdkFactory;
    private final String adTagUrl;
    private final boolean hasPreroll;

    private boolean isPresentingAd = false;
    private boolean isPlayingContentVideo = false;

    private DACMASDKAdsManager adsManager;
    private DACMASDKAdDisplayContainer adDisplayContainer;

    private Event originalEvent;

    ViewGroup adCompanionBanner;

    private boolean isAllAdsCompleted = false;

    // SDK側の設定とコンテンツ終了のリスナーのセット、VASTのURLのセット
    public VideoPlayerController(ViewGroup parentView,
                                 EventEmitter eventEmitter,
                                 VideoPlayerWithAdPlayback videoPlayerWithAdPlayback,
                                 String adTagUrl,
                                 boolean hasPreroll) {
        this.parentView = parentView;
        this.eventEmitter = eventEmitter;
        this.videoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
        this.adTagUrl = adTagUrl;
        this.hasPreroll = hasPreroll;

        sdkFactory = DACMASDKFactory.getInstance();
        adsLoader = sdkFactory.createAdsLoader(parentView.getContext());
        adsLoader.addAdsLoadedListener(this);
        adsLoader.addAdErrorListener(this);

        if (!hasPreroll) {
            hide();
        }
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
                    videoPlayerWithAdPlayback.setAdsManager(adsManager);
                    adsManager.start();
                }
                isAllAdsCompleted = false;
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
                isAllAdsCompleted = true;
                break;
            default:
                break;
        }

        videoPlayerWithAdPlayback.setAllAdsCompleted(isAllAdsCompleted);
    }

    @Override
    public void onAdError(DACMASDKAdErrorEvent adErrorEvent) {
        hide();

        eventEmitter.emit(MAAdPlayerEvent.DID_FAIL_TO_PLAY_AD);
        videoPlayerWithAdPlayback.postDelayed(new Runnable() {
            @Override
            public void run() {
                willResumeContent();
            }
        }, 200);
    }

    public void onContentResumeRequested() {
        if (isPresentingAd && !videoPlayerWithAdPlayback.isFullscreen()) {
            willResumeContent();
        }
    }

    private void willResumeContent() {
        Log.d(TAG, "willResumeContent");

        isPresentingAd = false;
        hide();

        Map<String, Object> properties = new HashMap<>();
        if (originalEvent == null) {
            originalEvent = new Event("play");
            originalEvent.properties.put("skipCuePoints", true);
        }
        properties.put("original", originalEvent);

        eventEmitter.emit(EventType.WILL_RESUME_CONTENT, properties);
        originalEvent = null;
    }

    public void onContentPauseRequested() {
        Log.d(TAG, "onContentPauseRequested");

        if (isPlayingContentVideo) {
            eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);
            isPlayingContentVideo = false;
        }

        isPresentingAd = true;

        // TODO: dirty hack
        // detach/attachをしないと表示されない端末がある
        hide();
        show();
    }

    void init() {
        eventEmitter.on(EventType.CUE_POINT, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("init-cue-point", "init-cue-point");
                int startTime = event.getIntegerProperty("startTime");
                int endTime = event.getIntegerProperty("endTime");
                Log.d("cue-point", "startTime:" + startTime + ",endTime:" + endTime);

                if (startTime <= endTime) {
                    internalInitializeAdsRequests(event);
                }
            }
        });

        eventEmitter.on(EventType.DID_PLAY, new EventListener() {
            @Override
            public void processEvent(Event event) {
                isPlayingContentVideo = true;
                if (isPresentingAd) {
                    adsManager.destroy();
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
                videoPlayerWithAdPlayback.closeAd();
            }
        });
    }

    private void internalInitializeAdsRequests(Event event) {
        if (isPresentingAd) return;

        originalEvent = (Event) event.properties.get("original");
        eventEmitter.emit(MAAdPlayerEvent.ADS_REQUEST_FOR_VIDEO);
    }

    void play() {
        requestAds(adTagUrl);
    }

    // AdsLoaderにVideoPlayer,VASTのURL,コンテンツの進行状況の取得設定を送信
    private void requestAds(final String adTagUrl) {
        Log.d(TAG, "requestAds");

        if (isPlayingContentVideo) {
            eventEmitter.emit(EventType.WILL_INTERRUPT_CONTENT);
            show();
            isPlayingContentVideo = false;
        }

        if (adsManager != null) {
            adsManager.destroy();
            adsManager = null;
        }

        videoPlayerWithAdPlayback.init();

        adDisplayContainer = sdkFactory.createAdDisplayContainer();
        adDisplayContainer.setPlayer(videoPlayerWithAdPlayback.getVideoAdPlayer());

        if (adCompanionBanner != null) {
            List<DACMASDKCompanionAdSlot> companionAdSlots = new ArrayList<>();
            DACMASDKCompanionAdSlot companionAdSlot = DACMASDKFactory.getInstance().createCompanionAdSlot();
            companionAdSlot.setContainer(adCompanionBanner);
            companionAdSlots.add(companionAdSlot);
            adDisplayContainer.setCompanionSlots(companionAdSlots);
        }

        DACMASDKAdsRequest request = sdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setAdVideoView(videoPlayerWithAdPlayback.getVideoPlayer());

        adsLoader.requestAds(request);
        isPresentingAd = false;
        isAllAdsCompleted = false;
    }

    void resume() {
        videoPlayerWithAdPlayback.restorePosition();
        if (adsManager != null && isPresentingAd) {
            adsManager.resume();
        }
    }

    void pause() {
        videoPlayerWithAdPlayback.savePosition();
        if (adsManager != null && isPresentingAd) {
            adsManager.pause();
        }
    }

    void destroy() {
        videoPlayerWithAdPlayback.restorePosition();
        if (adsManager != null) {
            adsManager.destroy();
            adsManager = null;
        }
    }

    private void show() {
        if (videoPlayerWithAdPlayback.getParent() == null) {
            parentView.addView(videoPlayerWithAdPlayback);
        }
    }

    private void hide() {
        if (videoPlayerWithAdPlayback.getParent() != null) {
            parentView.removeView(videoPlayerWithAdPlayback);
        }
    }
}
