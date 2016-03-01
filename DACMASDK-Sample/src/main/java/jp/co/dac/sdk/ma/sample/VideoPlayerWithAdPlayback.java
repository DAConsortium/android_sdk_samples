package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

import static jp.co.dac.ma.sdk.api.player.VideoAdPlayer.*;

public class VideoPlayerWithAdPlayback extends FrameLayout {

    private static final String TAG = VideoPlayerWithAdPlayback.class.getSimpleName();

    protected FullscreenButton.Builder builder;

    protected ScrollDetector detector;

    // The saved position in the content to resume to after ad playback.
    private int mSavedVideoPosition;

    // The wrapped video player view.
    protected VideoPlayerView mVideoPlayerView;
    protected VideoPlayer mVideoPlayer;

    // Used to track if the current video is an ad (as opposed to a content video).
    protected boolean mIsAdDisplayed;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    protected VideoAdPlayer mVideoAdPlayer;
    protected VideoAdExtensionPlayer mVideoAdExtensionPlayer;

    protected FullscreenButton mFullscreenButton;
    protected MuteButton mMuteButton;
    protected SkipButton mSkipButton;
    protected ReplayButton mReplayButton;

    protected final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks =
            new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(1);

    protected boolean mIsAdCompleted = false;

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
        mIsAdDisplayed = false;

        mVideoPlayer = new DACVideoPlayer();
        mVideoPlayerView = (VideoPlayerView) findViewById(R.id.video_player);

        // Define VideoAdPlayer connector.
        mVideoAdPlayer = new VideoAdPlayer() {
            @Override
            public void playAd() {
                mIsAdDisplayed = true;
                if (!mIsAdCompleted) {
                    mVideoPlayer.play();
                }
            }

            @Override
            public void loadAd(String url) {
                mIsAdDisplayed = true;
                mVideoPlayerView.init(mVideoPlayer);
                mVideoPlayer.setVideoPath(url);
            }

            @Override
            public void stopAd() {
                mVideoPlayer.stop();
            }

            @Override
            public void pauseAd() {
                if (mVideoPlayer.isPlaying()) {
                    mVideoPlayer.pause();
                }
            }

            @Override
            public void resumeAd() {
                playAd();
            }

            @Override
            public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                mAdCallbacks.add(videoAdPlayerCallback);
            }

            @Override
            public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                mAdCallbacks.remove(videoAdPlayerCallback);
            }

            @Override
            public DACMASDKVideoProgressUpdate getAdProgress() {
                if (!mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
                    return DACMASDKVideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new DACMASDKVideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
                        mVideoPlayer.getDuration());
            }
        };

        // Set player callbacks for delegating major video events.
        mVideoPlayer.registerEventListener(new VideoPlayer.EventListener() {
            @Override
            public void changeState(VideoPlayer.VideoPlayerState state) {
                Log.d(TAG, "changeState:" + state.toString());
                switch (state) {
                    case STATE_PREPARED:
                        mVideoPlayerView.requestLayout();
                    case STATE_PLAYING:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPlay();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_RESUME:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onResume();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_PAUSED:
                        if (mIsAdDisplayed) {
                            for (VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPause();
                            }
                        }
                        break;
                    case STATE_ERROR:
                        for (VideoAdPlayerCallback callback : mAdCallbacks) {
                            callback.onError();
                        }
                        mAdCallbacks.clear();
                        setVisibility(View.GONE);
                        break;
                    case STATE_PLAYBACK_COMPLETED:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayerCallback callback : mAdCallbacks) {
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
                for (VideoAdPlayerCallback callback : mAdCallbacks) {
                    callback.onVolumeChanged(volume);
                }

                if (mMuteButton != null) {
                    mMuteButton.emitCallback(isMute);
                }
            }
        });

        mVideoAdExtensionPlayer = new VideoAdExtensionPlayer() {
            @Override
            public void onSkippable() {
                mSkipButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSkippdisable() {
                mSkipButton.setVisibility(View.GONE);
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
                        if (!mIsAdCompleted) {
                            mVideoPlayer.play();
                        }
                    }

                    @Override
                    public void outScreen() {
                        mVideoPlayer.pause();
                    }
                });
            }
        });

        setIsAdCompleted(false);
        setVisibility(View.GONE);
    }

    private void setupFullScreenButton() {
        builder = new FullscreenButton.Builder(mVideoPlayerView);
        builder.callback(new FullscreenButton.Builder.Callback() {
            @Override
            public void onShow() {
                for (VideoAdPlayerCallback callback : mAdCallbacks) {
                    callback.onFullScreen();
                }
            }

            @Override
            public void onDismiss() {
                // synchronized mute state
                mMuteButton.emitCallback();

                if (mIsAdCompleted) {
                    // show last frame
                    mVideoPlayer.setFrame(mVideoPlayer.getDuration());
                }
            }
        });

        mFullscreenButton = (FullscreenButton) findViewById(R.id.fullscreen_button);
        mFullscreenButton.init(builder);
    }

    private void setupMuteButton() {
        mMuteButton = (MuteButton) findViewById(R.id.mute_button);
        mMuteButton.setMutePlayer(new MuteButton.CanMutePlayer() {
            @Override
            public void mute() {
                mVideoPlayer.mute();
                builder.isMute(true);
            }

            @Override
            public void unMute() {
                mVideoPlayer.unMute(MuteButton.deviceVolume(getContext()));
                builder.isMute(false);
            }
        }, true);
    }

    private void setupReplayButton() {
        mReplayButton = (ReplayButton) findViewById(R.id.replay_button);
        mReplayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer != null) {
                    setIsAdCompleted(false);
                    mVideoPlayer.seekTo(0);
                    mVideoPlayer.play();
                }
            }
        });
    }

    private void setupSkipButton() {
        mSkipButton = (SkipButton) findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (VideoAdPlayerCallback callback : mAdCallbacks) {
                    callback.onSkip();
                }
            }
        });
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return mVideoAdPlayer;
    }

    public VideoAdExtensionPlayer getVideoAdExtensionPlayer() {
        return mVideoAdExtensionPlayer;
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
    }

    boolean inScroll() {
        if (detector == null) {
            return false;
        }
        return detector.currentInScreen();
    }

    boolean isAdCompleted() {
        return mIsAdCompleted;
    }

    private void setIsAdCompleted(boolean isAdCompleted) {
        mIsAdCompleted = isAdCompleted;

        if (builder != null) {
            builder.isCompleted(isAdCompleted);
        }

        if (mReplayButton != null) {
            if (isAdCompleted) {
                mReplayButton.setVisibility(View.VISIBLE);
            } else {
                mReplayButton.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Save the playback progress state of the currently playing video.
     */
    public void savePosition() {
        mSavedVideoPosition = mVideoPlayer.getCurrentPosition();
    }

    /**
     * Restore the currently loaded video to its previously saved playback progress state.
     */
    public void restorePosition() {
        mVideoPlayer.seekTo(mSavedVideoPosition);
    }
}
