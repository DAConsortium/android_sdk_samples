package jp.co.dac.sdk.brightcove.sample;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.brightcove.player.event.EventEmitter;

import java.util.ArrayList;
import java.util.List;

import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;
import jp.co.dac.ma.sdk.api.player.VideoAdPlayer;
import jp.co.dac.ma.sdk.widget.FullscreenButton;
import jp.co.dac.ma.sdk.widget.MuteButton;
import jp.co.dac.ma.sdk.widget.ReplayButton;
import jp.co.dac.ma.sdk.widget.VideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.DACVideoPlayer;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

public class VideoPlayerWithAdPlayback extends RelativeLayout {

    private static final String TAG = VideoPlayerWithAdPlayback.class.getSimpleName();

    // Retains a video information for Fullscreen.
    private FullscreenButton.Builder fullscreenBuilder;

    private VideoPlayerView videoPlayerView;
    private VideoPlayer videoPlayer;

    // The saved position in the content to resume to after ad playback.
    private int savedContentVideoPosition;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    private VideoAdPlayer videoAdPlayer;

    private FullscreenButton fullscreenButton;
    private MuteButton muteButton;
    private ReplayButton replayButton;

    @NonNull
    private final List<VideoAdPlayer.VideoAdPlayerCallback> adCallbacks = new ArrayList<>(1);

    private boolean isAdDisplayed = false;
    private boolean isAdCompleted = false;

    private EventEmitter eventEmitter;

    public VideoPlayerWithAdPlayback(Context context) {
        super(context);
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
        savedContentVideoPosition = 0;

        videoPlayer = new DACVideoPlayer();
        videoPlayerView = (VideoPlayerView) findViewById(R.id.videoplayer);

        fullscreenBuilder = new FullscreenButton.Builder(videoPlayerView);
        fullscreenBuilder.callback(new FullscreenButton.Builder.Callback() {
            @Override
            public void onShow() {
                for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                    callback.onFullScreen();
                }

                eventEmitter.emit(MAAdPlayerEvent.DID_OPEN_FULLSCREEN);
            }

            @Override
            public void onDismiss() {
                // synchronized mute state
                muteButton.emitCallback();

                if (isAdCompleted) {
                    // show last frame
                    videoPlayer.setFrame(videoPlayer.getDuration());
                }

                eventEmitter.emit(MAAdPlayerEvent.DID_CLOSE_FULLSCREEN);
            }
        });

        fullscreenButton = (FullscreenButton) findViewById(R.id.fullscreen_button);
        fullscreenButton.init(fullscreenBuilder);

        muteButton = (MuteButton) findViewById(R.id.mute_button);
        muteButton.setMutePlayer(new MuteButton.CanMutePlayer() {
            @Override
            public void mute() {
                videoPlayer.mute();
                fullscreenBuilder.isMute(true);
            }

            @Override
            public void unMute() {
                videoPlayer.unMute(MuteButton.deviceVolume(getContext()));
                fullscreenBuilder.isMute(false);
            }
        }, true);

        // Define VideoAdPlayer connector.
        videoAdPlayer = new VideoAdPlayer() {
            @Override
            public void playAd() {
                if (!isAdCompleted) {
                    isAdDisplayed = true;
                    resumeAdPlayer();
                }
            }

            @Override
            public void loadAd(String url) {
                videoPlayer.setVideoPath(url);
                videoPlayerView.init(videoPlayer);
            }

            @Override
            public void stopAd() {
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
                Log.d(TAG, "changeState:" + state);
                switch (state) {
                    case STATE_PREPARED:
                        videoPlayerView.requestLayout();
                    case STATE_PLAYING:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onPlay();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_RESUME:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onResume();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_PAUSED:
                        if (isAdDisplayed) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onPause();
                            }
                        }
                        break;
                    case STATE_ERROR:
                        for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                            callback.onError();
                        }
                        adCallbacks.clear();
                        setVisibility(View.GONE);
                        break;
                    case STATE_PLAYBACK_COMPLETED:
                        if (isAdDisplayed && !isAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                                callback.onEnded();
                            }
                            setIsAdCompleted(true);
                        }

                        break;
                }
            }

            @Override
            public void changeVolumeState(boolean isMute) {
                int volume = isMute ? 0 : 100;
                for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
                    callback.onVolumeChanged(volume);
                }

                if (muteButton != null) {
                    muteButton.emitCallback(isMute);
                }
            }
        });

        replayButton = (ReplayButton) findViewById(R.id.replay_button);
        replayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoPlayer != null) {
                    setIsAdCompleted(false);
                    videoPlayer.seekTo(0);
                    videoPlayer.play();
                }
            }
        });

        setIsAdCompleted(false);
        setVisibility(View.GONE);
    }

    /**
     * Save the playback progress state of the currently playing video.
     */
    public void savePosition() {
        savedContentVideoPosition = videoPlayer.getCurrentPosition();
    }

    /**
     * Restore the currently loaded video to its previously saved playback progress state.
     */
    public void restorePosition() {
        videoPlayer.seekTo(savedContentVideoPosition);
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return videoAdPlayer;
    }

    public View getVideoPlayer() {
        return this;
    }

    void setEventEmitter(@NonNull EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    void setAdsManager(@NonNull DACMASDKAdsManager adsManager) {
        if (fullscreenBuilder != null) {
            fullscreenBuilder.adsManager(adsManager);
        }
    }

    void closeAd() {
        videoPlayer.pause();
        for (VideoAdPlayer.VideoAdPlayerCallback callback : adCallbacks) {
            callback.onClose();
        }

        setIsAdCompleted(true);
        isAdDisplayed = false;
    }

    void init() {
        isAdDisplayed = isAdCompleted = false;
        savedContentVideoPosition = 0;
        adCallbacks.clear();
    }

    private void resumeAdPlayer() {
        if (eventEmitter == null) return;

        videoPlayer.play();
        setVisibility(View.VISIBLE);
    }

    private void setIsAdCompleted(boolean isAdCompleted) {
        this.isAdCompleted = isAdCompleted;

        if (fullscreenBuilder != null) {
            fullscreenBuilder.isCompleted(isAdCompleted);
        }

        if (replayButton != null) {
            if (isAdCompleted) {
                replayButton.setVisibility(View.VISIBLE);
            } else {
                replayButton.setVisibility(View.GONE);
            }
        }
    }
}
