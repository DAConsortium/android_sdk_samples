package jp.co.dac.sdk.ma.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import jp.co.dac.ma.sdk.api.player.DACMASDKContentProgressProvider;
import jp.co.dac.ma.sdk.api.player.DACMASDKVideoProgressUpdate;
import jp.co.dac.ma.sdk.widget.VideoPlayerView;
import jp.co.dac.ma.sdk.widget.player.DACVideoPlayer;
import jp.co.dac.ma.sdk.widget.player.VideoPlayer;

public class VideoPlayerContentWithAdPlayback extends VideoPlayerWithAdPlayback {

    private static final String TAG = VideoPlayerContentWithAdPlayback.class.getSimpleName();

    /** Interface for alerting caller of video completion. */
    public interface OnContentCompleteListener {
        void onContentComplete();
    }

    // Used to track the current content video URL to resume content playback.
    private VideoPlayerView contentVideoPlayerView;
    private VideoPlayer contentVideoPlayer;
    private String contentUrl;

    // Called when the content is completed.
    private OnContentCompleteListener onContentCompleteListener;

    // DACMASDKContentProgressProvider interface implementation for the SDK to check content progress.
    private DACMASDKContentProgressProvider contentProgressProvider;

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
        isAdDisplayed = false;

        setupContent();
    }

    private void setupContent() {
        contentProgressProvider = new DACMASDKContentProgressProvider() {
            @Override
            public DACMASDKVideoProgressUpdate getContentProgress() {
                if (isAdDisplayed || videoPlayer.getDuration() <= 0) {
                    return DACMASDKVideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new DACMASDKVideoProgressUpdate(videoPlayer.getCurrentPosition(),
                        videoPlayer.getDuration());
            }
        };
    }

    /**
     * Set a listener to be triggered when the content (non-ad) video completes.
     */
    public void setOnContentCompleteListener(OnContentCompleteListener listener) {
        onContentCompleteListener = listener;
    }

    public DACMASDKContentProgressProvider getContentProgressProvider() {
        return contentProgressProvider;
    }

    /**
     * Pause the currently playing content video in preparation for an ad to play, and disables
     * the media controller.
     */
    public void pauseContentForAdPlayback() {
        if (contentVideoPlayer != null
                && contentVideoPlayerView != null) {
            contentVideoPlayerView.setVisibility(View.GONE);
            contentVideoPlayer.pause();
        }

        isAdDisplayed = true;
        setVisibility(View.VISIBLE);
    }

    /**
     * Resume the content video from its previous playback progress position after
     * an ad finishes playing. Re-enables the media controller.
     */
    public void resumeContentAfterAdPlayback() {
        if (contentVideoPlayer != null
                && contentVideoPlayerView != null) {
            contentVideoPlayerView.setVisibility(View.VISIBLE);
            contentVideoPlayer.play();

            setVisibility(View.GONE);
            videoPlayer.pause();

            isAdDisplayed = false;
        }
    }

    public void setContentVideoPlayer(VideoPlayerView contentVideoPlayerView, String contentUrl) {
        if (contentVideoPlayer == null) {
            contentVideoPlayer = new DACVideoPlayer();
        }

        if (!contentUrl.equals(this.contentUrl)) {
            this.contentUrl = contentUrl;
            contentVideoPlayer.setVideoPath(contentUrl);
            contentVideoPlayer.registerEventListener(new VideoPlayer.EventListener() {
                @Override
                public void changeState(VideoPlayer.VideoPlayerState state) {
                    switch (state) {
                        case STATE_PLAYBACK_COMPLETED:
                            if (onContentCompleteListener != null) {
                                onContentCompleteListener.onContentComplete();
                            }
                            break;
                    }
                }

                @Override
                public void changeVolumeState(boolean isMute) {
                    // TODO
                }
            });
        }

        if (this.contentVideoPlayerView != contentVideoPlayerView) {
            this.contentVideoPlayerView = contentVideoPlayerView;
            contentVideoPlayerView.init(contentVideoPlayer);
        }
    }

    public void resumeContent() {
        if (contentVideoPlayer != null
                && !isAdDisplayed
                && inScroll()) {
            contentVideoPlayer.play();
        }
    }

    public void pauseContent() {
        if (contentVideoPlayer != null) {
            contentVideoPlayer.pause();
        }
    }
}
