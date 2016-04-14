package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;
import jp.co.dac.ma.sdk.api.player.VideoAdPlayer;
import jp.co.dac.ma.sdk.widget.FullscreenButton;
import jp.co.dac.ma.sdk.widget.MuteButton;
import jp.co.dac.ma.sdk.widget.ReplayButton;
import jp.co.dac.ma.sdk.widget.ScrollDetector;
import jp.co.dac.ma.sdk.widget.SkipButton;
import jp.co.dac.ma.sdk.widget.VideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.DACVideoPlayer;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

import static jp.co.dac.ma.sdk.api.player.VideoAdPlayer.VideoAdExtensionPlayer;
import static jp.co.dac.ma.sdk.api.player.VideoAdPlayer.VideoAdPlayerCallback;

public class VideoPlayerWithAdPlayback extends FrameLayout {

    private static final String TAG = VideoPlayerWithAdPlayback.class.getSimpleName();

    protected final List<VideoAdPlayer.VideoAdPlayerCallback> adCallbacks = new ArrayList<>(1);

    protected FullscreenButton.Builder builder;

    protected ScrollDetector detector;

    // The saved position in the content to resume to after ad playback.
    private int savedVideoPosition;

    // The wrapped video player view.
    protected VideoPlayerView videoPlayerView;
    protected VideoPlayer videoPlayer;

    // Used to track if the current video is an ad (as opposed to a content video).
    protected boolean isAdDisplayed;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    protected VideoAdPlayer videoAdPlayer;
    protected VideoAdExtensionPlayer videoAdExtensionPlayer;
    protected DACMASDKAdsManager adsManager;

    protected FullscreenButton fullscreenButton;
    protected MuteButton muteButton;
    protected SkipButton skipButton;
    protected ReplayButton replayButton;

    protected boolean isAdCompleted = false;

    public VideoPlayerWithAdPlayback(Context context) {
        this(context, null);
    }

    public VideoPlayerWithAdPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        internalInit();
    }

    private void internalInit() {
        isAdDisplayed = false;

        videoPlayer = new DACVideoPlayer();
        videoPlayerView = (VideoPlayerView) findViewById(R.id.ad_video_player);

        // Define VideoAdPlayer connector.
        videoAdPlayer = new VideoAdPlayer() {
            @Override
            public void playAd() {
                isAdDisplayed = true;
                if (!videoPlayer.isPlaying() && inScroll()) {
                    videoPlayer.play();
                }
            }

            @Override
            public void loadAd(String url) {
                isAdDisplayed = true;
                videoPlayer.setVideoPath(url);
                videoPlayerView.init(videoPlayer);
            }

            @Override
            public void stopAd() {
                isAdDisplayed = false;
                videoPlayer.stop();
            }

            @Override
            public void pauseAd() {
                if (videoPlayer.isPlaying()) {
                    videoPlayer.pause();
                }
            }

            @Override
            public void resumeAd() {
                playAd();
            }

            @Override
            public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                adCallbacks.add(videoAdPlayerCallback);
            }

            @Override
            public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                adCallbacks.remove(videoAdPlayerCallback);
            }

            @Override
            public DACMASDKVideoProgressUpdate getAdProgress() {
                if (!isAdDisplayed || videoPlayer.getDuration() <= 0) {
                    return DACMASDKVideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new DACMASDKVideoProgressUpdate(videoPlayer.getCurrentPosition(),
                        videoPlayer.getDuration());
            }
        };

        // Set player callbacks for delegating major video events.
        videoPlayer.registerEventListener(new VideoPlayer.EventListener() {
            @Override
            public void changeState(VideoPlayer.VideoPlayerState state) {
                Log.d(TAG, "changeState:" + state.toString());
                switch (state) {
                    case STATE_PREPARED:
                        videoPlayerView.requestLayout();
                    case STATE_PLAYING:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onPlay();
                            }
                        }
                        setVisibility(View.VISIBLE);
                        updateIsAdCompleted(false);
                        showVideoPlayer();
                        break;
                    case STATE_RESUME:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onResume();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        updateIsAdCompleted(false);
                        showVideoPlayer();
                        break;
                    case STATE_PAUSED:
                        if (isAdDisplayed) {
                            for (VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onPause();
                            }
                        }
                        break;
                    case STATE_ERROR:
                        for (VideoAdPlayerCallback callback : adCallbacks) {
                            callback.onError();
                        }
                        setVisibility(View.GONE);
                        break;
                    case STATE_PLAYBACK_COMPLETED:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onEnded();
                            }
                        }

                        if (haveVideoImage()) {
                            hideVideoPlayer();
                        }

                        break;
                }
            }

            @Override
            public void changeVolumeState(boolean isMute) {
                int volume = isMute ? 0 : 100;
                for (VideoAdPlayerCallback callback : adCallbacks) {
                    callback.onVolumeChanged(volume);
                }

                if (muteButton != null) {
                    muteButton.emitCallback(isMute);
                }
            }
        });

        videoAdExtensionPlayer = new VideoAdExtensionPlayer() {
            @Override
            public void onSkippable() {
                skipButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSkippdisable() {
                skipButton.setVisibility(View.GONE);
            }

            @Override
            public void onSkippableOffset(long offsetMillis) {
                Log.d("onSkippableOffset", String.valueOf(offsetMillis));
            }
        };

        setupFullScreenButton();
        setupMuteButton();
        setupReplayButton();
        setupSkipButton();

        post(new Runnable() {
            @Override
            public void run() {
                detector = new ScrollDetector(VideoPlayerWithAdPlayback.this);
                detector.prepare(new ScrollDetector.ScrollDetectorListener() {
                    @Override
                    public void inScreen() {
                        if (!isAdCompleted) {
                            videoPlayer.play();
                        }
                    }

                    @Override
                    public void outScreen() {
                        videoPlayer.pause();
                    }
                });
            }
        });

        updateIsAdCompleted(false);
        setVisibility(View.GONE);
    }

    private void setupFullScreenButton() {
        builder = new FullscreenButton.Builder(videoPlayerView);
        builder.callback(new FullscreenButton.Builder.Callback() {
            @Override
            public void onShow() {
                for (VideoAdPlayerCallback callback : adCallbacks) {
                    callback.onFullScreen();
                }
            }

            @Override
            public void onDismiss() {
                // synchronized mute state
                muteButton.emitCallback();

                if (isAdCompleted) {
                    // show last frame
                    videoPlayer.setFrame(videoPlayer.getDuration());
                }
            }
        });

        fullscreenButton = (FullscreenButton) findViewById(R.id.fullscreen_button);
        fullscreenButton.init(builder);
    }

    private void setupMuteButton() {
        muteButton = (MuteButton) findViewById(R.id.mute_button);
        muteButton.setMutePlayer(new MuteButton.CanMutePlayer() {
            @Override
            public void mute() {
                videoPlayer.mute();
                builder.isMute(true);
            }

            @Override
            public void unMute() {
                videoPlayer.unMute(MuteButton.deviceVolume(getContext()));
                builder.isMute(false);
            }
        }, true);
    }

    private void setupReplayButton() {
        replayButton = (ReplayButton) findViewById(R.id.replay_button);
        replayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoPlayer != null) {
                    updateIsAdCompleted(false);
                    videoPlayer.seekTo(0);
                    videoPlayer.play();
                }
            }
        });
    }

    private void setupSkipButton() {
        skipButton = (SkipButton) findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adsManager != null) {
                    adsManager.skip();
                }
            }
        });
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return videoAdPlayer;
    }

    public VideoAdExtensionPlayer getVideoAdExtensionPlayer() {
        return videoAdExtensionPlayer;
    }

    public View getVideoPlayerContainer() {
        return this;
    }

    /**
     * Pause the currently playing content video in preparation for an ad to play, and disables
     * the media controller.
     */
    public void pauseContentForAdPlayback() {
        // do nothing
    }

    void setAdsManager(DACMASDKAdsManager adsManager) {
        if (builder != null) {
            builder.adsManager(adsManager);
        }
        this.adsManager = adsManager;
    }

    boolean inScroll() {
        return detector == null
                ? true
                : detector.currentInScreen();
    }

    boolean isAdCompleted() {
        return isAdCompleted;
    }

    private void updateIsAdCompleted(boolean isAdCompleted) {
        this.isAdCompleted = isAdCompleted;

        if (builder != null) {
            builder.isCompleted(isAdCompleted);
        }

        if (replayButton != null) {
            if (isAdCompleted) {
                replayButton.setVisibility(View.VISIBLE);
            } else {
                replayButton.setVisibility(View.GONE);
            }
        }
    }

    private void showVideoPlayer() {
        videoPlayerView.setVisibility(View.VISIBLE);
        getVideoPlayerImage().setVisibility(View.GONE);
    }

    private void hideVideoPlayer() {
        videoPlayerView.setVisibility(View.GONE);
        getVideoPlayerImage().setVisibility(View.VISIBLE);
    }

    /**
     * Save the playback progress state of the currently playing video.
     */
    public void savePosition() {
        savedVideoPosition = videoPlayer.getCurrentPosition();
    }

    /**
     * Restore the currently loaded video to its previously saved playback progress state.
     */
    public void restorePosition() {
        videoPlayer.seekTo(savedVideoPosition);
    }

    public void setAllAdsCompleted(boolean allAdCompleted) {
        updateIsAdCompleted(allAdCompleted);
    }

    public ViewGroup getCompanionView() {
        return (ViewGroup) ((View) getParent()).findViewById(R.id.companion_ad_banner);
    }

    public ViewGroup getVideoPlayerImage() {
        return (ViewGroup) findViewById(R.id.video_player_image);
    }

    private boolean haveVideoImage() {
        ViewGroup videoPlayerImage = getVideoPlayerImage();
        if (videoPlayerImage == null) {
            return false;
        }
        return videoPlayerImage.getChildCount() != 0;
    }
}
