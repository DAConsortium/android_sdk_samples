package jp.co.dac.sdk.ma.sample;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdFragment extends Fragment {

    private static final String INTENT_TAG_URL_KEY = "INTENT_TAG_URL_KEY";

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
        return inflater.inflate(R.layout.fragment_ad_vertical, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoPlayerPlayback = (VideoPlayerWithAdPlayback) view.findViewById(R.id.video_player_with_ad_playback);
        videoPlayerController = new VideoPlayerController(getActivity(), videoPlayerPlayback, getArguments().getString(INTENT_TAG_URL_KEY));
        videoPlayerController.play();
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
