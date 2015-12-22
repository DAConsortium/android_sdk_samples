package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import jp.co.dac.ma.sdk.api.DACMASDKAd;
import jp.co.dac.ma.sdk.api.DACMASDKAdsManager;
import jp.co.dac.ma.sdk.api.player.DACMASDKContentProgressProvider;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;
import jp.co.dac.ma.sdk.api.player.VideoAdPlayer;
import jp.co.dac.ma.sdk.widget.FullscreenButton;
import jp.co.dac.ma.sdk.widget.MuteButton;
import jp.co.dac.ma.sdk.widget.ScrollDetector;
import jp.co.dac.ma.sdk.widget.VideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.DACVideoPlayer;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

public class VideoPlayerContentWithAdPlayback extends RelativeLayout {

    private static final String TAG = VideoPlayerContentWithAdPlayback.class.getSimpleName();

    /** Interface for alerting caller of video completion. */
    public interface OnContentCompleteListener {
        void onContentComplete();
    }

    private FullscreenButton.Builder builder;

    private ScrollDetector detector;

    // The wrapped video player view.
    private VideoPlayerView mVideoPlayerView;
    private VideoPlayer mVideoPlayer;

    // Used to track if the current video is an ad (as opposed to a content video).
    private boolean mIsAdDisplayed;

    // Used to track the current content video URL to resume content playback.
    private String mContentVideoUrl;

    // The saved position in the content to resume to after ad playback.
    private int mSavedContentVideoPosition;

    // Called when the content is completed.
    private OnContentCompleteListener mOnContentCompleteListener;

    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    private VideoAdPlayer mVideoAdPlayer;

    // DACMASDKContentProgressProvider interface implementation for the SDK to check content progress.
    private DACMASDKContentProgressProvider mContentProgressProvider;

    private FullscreenButton mFullscreenButton;

    private MuteButton mMuteButton;

    private final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks =
            new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(1);

    // Check if the content video is complete
    private boolean mIsContentComplete;

    private boolean mIsInScreenLeastOnce = false;
    private boolean mIsAdCompleted = false;

    public VideoPlayerContentWithAdPlayback(Context context) {
        super(context);
    }

    public VideoPlayerContentWithAdPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        internalInit();
    }

    private void internalInit() {
        mIsAdDisplayed = false;
        mIsContentComplete = false;
        mSavedContentVideoPosition = 0;

        mVideoPlayer = new DACVideoPlayer();
        mVideoPlayerView = (VideoPlayerView) findViewById(R.id.videoplayer);
        mVideoPlayerView.init(mVideoPlayer);

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
                    mVideoPlayer.play();
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

        mContentProgressProvider = new DACMASDKContentProgressProvider() {
            @Override
            public DACMASDKVideoProgressUpdate getContentProgress() {
                if (mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
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
                    case STATE_RESUME:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPlay();
                            }
                        }

                        setVisibility(View.VISIBLE);
                        builder.isCompleted(false);
                        mIsAdCompleted = false;
                        break;
                    case STATE_PAUSED:
                        if (mIsAdDisplayed) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onPause();
                            }
                        }
                        break;
                    case STATE_ERROR:
                        if (mIsAdDisplayed) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onError();
                            }
                            setVisibility(View.GONE);
                        }
                        break;
                    case STATE_PLAYBACK_COMPLETED:
                        if (mIsAdDisplayed && !mIsAdCompleted) {
                            for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
                                callback.onEnded();
                            }
                            builder.isCompleted(true);
                            mIsAdCompleted = true;
                        } else {
                            // Alert an external listener that our content video is complete.
                            if (mOnContentCompleteListener != null) {
                                mOnContentCompleteListener.onContentComplete();
                            }
                            mIsContentComplete = true;
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

        post(new Runnable() {
            @Override
            public void run() {
                detector = new ScrollDetector(VideoPlayerContentWithAdPlayback.this);
                detector.prepare(new ScrollDetector.ScrollDetectorListener() {
                    @Override
                    public void inScreen() {
                        if (!mIsInScreenLeastOnce) {
                            mIsInScreenLeastOnce = !mIsInScreenLeastOnce;
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
        setVisibility(View.GONE);
    }

    /**
     * Set a listener to be triggered when the content (non-ad) video completes.
     */
    public void setOnContentCompleteListener(OnContentCompleteListener listener) {
        mOnContentCompleteListener = listener;
    }

    /**
     * Set the path of the video to be played as content.
     */
    public void setContentVideoPath(String contentVideoUrl) {
        mContentVideoUrl = contentVideoUrl;
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

    public DACMASDKContentProgressProvider getContentProgressProvider() {
        return mContentProgressProvider;
    }

    /**
     * Pause the currently playing content video in preparation for an ad to play, and disables
     * the media controller.
     */
    public void pauseContentForAdPlayback() {
        savePosition();
        mVideoPlayer.stop();
    }

    /**
     * Resume the content video from its previous playback progress position after
     * an ad finishes playing. Re-enables the media controller.
     */
    public void resumeContentAfterAdPlayback() {
        if (mContentVideoUrl == null || mContentVideoUrl.isEmpty()) {
            mVideoPlayer.pause();
            return;
        }

        mIsAdDisplayed = false;
        mVideoPlayer.setVideoPath(mContentVideoUrl);
        restorePosition();
        mFullscreenButton.setVisibility(View.GONE);
        if (!mIsContentComplete) {
            mVideoPlayer.play();
        } else {
            mVideoPlayer.stop();
        }
    }

    void setAdsManager(DACMASDKAdsManager adsManager) {
        if (builder != null) {
            builder.adsManager(adsManager);
        }
    }

    boolean isAdCompleted() {
        return mIsAdCompleted;
    }
}
