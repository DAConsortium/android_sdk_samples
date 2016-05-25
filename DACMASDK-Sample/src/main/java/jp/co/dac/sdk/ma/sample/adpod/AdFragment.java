package jp.co.dac.sdk.ma.sample.adpod;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.dac.ma.sdk.widget.DACVideoPlayerView;
import jp.co.dac.sdk.ma.sample.R;
import jp.co.dac.sdk.ma.sample.VideoPlayerController;
import jp.co.dac.sdk.ma.sample.VideoPlayerWithAdPlayback;

public class AdFragment extends Fragment {

    private final static String INTENT_TAG_URL_KEY = "INTENT_TAG_URL_KEY";
    private final static String CONTENT_URL = "http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4";

    static AdFragment newInstance(String adTagUrl) {
        AdFragment fragment = new AdFragment();
        Bundle args = new Bundle();
        args.putString(INTENT_TAG_URL_KEY, adTagUrl);
        fragment.setArguments(args);

        return fragment;
    }

    private VideoPlayerWithAdPlayback videoPlayerPlayback;
    private VideoPlayerController videoPlayerController;

    public AdFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_ad, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoPlayerPlayback = (VideoPlayerWithAdPlayback) view.findViewById(R.id.video_player_with_ad_playback);
        videoPlayerController = new VideoPlayerController(getActivity(), videoPlayerPlayback, getArguments().getString(INTENT_TAG_URL_KEY));
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
