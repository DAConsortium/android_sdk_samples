package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScrollableVideoPlayerFragment extends Fragment {
    static ScrollableVideoPlayerFragment newInstance() {
        return new ScrollableVideoPlayerFragment();
    }

    private VideoPlayerWithAdPlayback mVideoPlayerNoContentPlayback;
    private VideoPlayerController mVideoPlayerController;

    public ScrollableVideoPlayerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scrollable_video_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mVideoPlayerNoContentPlayback = (VideoPlayerWithAdPlayback) view.findViewById(R.id.video_player_with_ad_playback);
        mVideoPlayerController = new VideoPlayerController(getActivity(), mVideoPlayerNoContentPlayback);
        mVideoPlayerController.play();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoPlayerController != null) {
            mVideoPlayerController.destroy();
        }
    }
}
