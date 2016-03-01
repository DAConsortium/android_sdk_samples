package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;

public class ContentVideoPlayerFragment extends Fragment {

    private final static String CONTENT_URL = "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4";

    static ContentVideoPlayerFragment newInstance() {
        return new ContentVideoPlayerFragment();
    }

    private VideoPlayerContentWithAdPlayback videoPlayerPlayback;
    private ContentVideoPlayerController videoPlayerController;

    public ContentVideoPlayerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_video_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoPlayerPlayback = (VideoPlayerContentWithAdPlayback) view.findViewById(R.id.video_player_with_ad_playback);
        videoPlayerController = new ContentVideoPlayerController(getActivity(), videoPlayerPlayback);
        videoPlayerController.play();

        videoPlayerController.setContentVideoPlayer(
                (DACVideoPlayerView) view.findViewById(R.id.content_video_player),
                CONTENT_URL);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoPlayerController != null) {
            videoPlayerController.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoPlayerController != null) {
            videoPlayerController.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayerController != null) {
            videoPlayerController.destroy();
        }
    }
}
