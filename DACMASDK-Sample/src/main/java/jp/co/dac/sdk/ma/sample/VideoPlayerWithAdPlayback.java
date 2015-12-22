package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import jp.co.dac.ma.sdk.api.DACMASDKAd;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;
import jp.co.dac.ma.sdk.api.player.VideoAdPlayer;
import jp.co.dac.ma.sdk.widget.FullscreenButton;
import jp.co.dac.ma.sdk.widget.MuteButton;
import jp.co.dac.ma.sdk.widget.ReplayButton;
import jp.co.dac.ma.sdk.widget.ScrollDetector;
import jp.co.dac.ma.sdk.widget.VideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.DACVideoPlayer;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

public class VideoPlayerWithAdPlayback extends FrameLayout {

    private static final String TAG = VideoPlayerWithAdPlayback.class.getSimpleName();

    private FullscreenButton.Builder builder;

    private ScrollDetector detector;

    // The wrapped video player view.
    private VideoPlayerView mVideoPlayerView;
    private VideoPlayer mVideoPlayer;

    // Used to track if the current video is an ad (as opposed to a content video).
    private boolean mIsAdDisplayed;

    // The saved position in the content to resume to after ad playback.
    private int mSavedContentVideoPosition;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    private VideoAdPlayer mVideoAdPlayer;

    private ViewGroup mVideoControllerView;
    private FullscreenButton mFullscreenButton;
    private MuteButton mMuteButton;
    private ReplayButton mReplayButton;

    private final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks =
            new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(1);

    private boolean mIsAdCompleted = false;

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
        mIsAdDisplayed = false;
        mSavedContentVideoPosition = 0;

        mVideoPlayer = new DACVideoPlayer();
        mVideoPlayerView = (VideoPlayerView) findViewById(R.id.videoplayer);

        builder = new FullscreenButton.Builder(mVideoPlayerView);
        builder.callback(new FullscreenButton.Builder.Callback() {
            @Override
            public void onShow() {
                for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
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
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPlay();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_RESUME:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onResume();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        setIsAdCompleted(false);
                        break;
                    case STATE_PAUSED:
                        if (mIsAdDisplayed) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPause();
                            }
                        }
                        break;
                    case STATE_ERROR:
                        for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                            callback.onError();
                        }
                        mAdCallbacks.clear();
                        setVisibility(View.GONE);
                        break;
                    case STATE_PLAYBACK_COMPLETED:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
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
                for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                    callback.onVolumeChanged(volume);
                }

                if (mMuteButton != null) {
                    mMuteButton.emitCallback(isMute);
                }
            }
        });

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

    /**
     * Save the playback progress state of the currently playing video.
     */
    public void savePosition() {
        mSavedContentVideoPosition = mVideoPlayer.getCurrentPosition();
    }

    /**
     * Restore the currently loaded video to its previously saved playback progress state.
     */
    public void restorePosition() {
        mVideoPlayer.seekTo(mSavedContentVideoPosition);
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return mVideoAdPlayer;
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

}
