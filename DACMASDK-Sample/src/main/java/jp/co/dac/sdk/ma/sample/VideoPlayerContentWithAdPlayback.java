package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import jp.co.dac.ma.sdk.api.player.DACMASDKContentProgressProvider;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;

public class VideoPlayerContentWithAdPlayback extends VideoPlayerWithAdPlayback {

    private static final String TAG = VideoPlayerContentWithAdPlayback.class.getSimpleName();

    /** Interface for alerting caller of video completion. */
    public interface OnContentCompleteListener {
        void onContentComplete();
    }

    // Used to track the current content video URL to resume content playback.
    private String mContentVideoUrl;

    // Called when the content is completed.
    private OnContentCompleteListener mOnContentCompleteListener;

    // DACMASDKContentProgressProvider interface implementation for the SDK to check content progress.
    private DACMASDKContentProgressProvider mContentProgressProvider;

    // Check if the content video is complete
    private boolean mIsContentComplete;

    private boolean mIsInScreenLeastOnce = false;

    public VideoPlayerContentWithAdPlayback(Context context) {
        this(context, null);
    }

    public VideoPlayerContentWithAdPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        internalInit();
        super.onFinishInflate();
    }

    private void internalInit() {
        mIsAdDisplayed = false;
        mIsContentComplete = false;

        setupContent();
    }

    private void setupContent() {
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
}
