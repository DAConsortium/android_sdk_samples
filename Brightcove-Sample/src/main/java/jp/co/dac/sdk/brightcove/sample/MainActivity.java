package jp.co.dac.sdk.brightcove.sample;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.media.Catalog;
import com.brightcove.player.media.PlaylistListener;
import com.brightcove.player.media.VideoFields;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.CuePoint;
import com.brightcove.player.model.Playlist;
import com.brightcove.player.util.StringUtil;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveVideoView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BrightcovePlayer {

    private static final String brightcoveToken = "-wmqCJ8f1-Qqcp_ukrszRICDwAqsuYPOPmZoBEeISikt2rWWCRoufw..";
    private static final String playistId = "4666662278001";

    private BrightcoveMediaController mediaController;
    private EventEmitter eventEmitter;

    private VideoPlayerController videoPlayerController;
    private VideoPlayerWithAdPlayback adVideoPlayerPlayback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        setupBrightcove();

        super.onCreate(savedInstanceState);

        setupMA();
        setupContents();
    }

    private void setupBrightcove() {
        brightcoveVideoView = (BrightcoveVideoView) findViewById(R.id.brightcove_video_view);
        mediaController = new BrightcoveMediaController(brightcoveVideoView);
        brightcoveVideoView.setMediaController(mediaController);

        eventEmitter = brightcoveVideoView.getEventEmitter();
        eventEmitter.on(EventType.DID_SET_SOURCE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d(TAG, event.getType());
                setupCuePoints();

                // auto playing
                brightcoveVideoView.start();
            }
        });

        // 広告が再生された時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_START_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // 広告が停止された時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_PAUSE_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // 広告がフルスクリーンになった時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_OPEN_FULLSCREEN, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // 広告がフルスクリーンから戻った時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_CLOSE_FULLSCREEN, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());

                videoPlayerController.onContentResumeRequested();
            }
        });

        // 広告が再生された時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_START_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // 広告が終了した時にemitされます
        eventEmitter.on(MAAdPlayerEvent.DID_COMPLETE_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d(TAG, event.getType());
            }
        });
    }

    /** Ad VideoPlayer(DAC-MA-SDK) prepares */
    private void setupMA() {
        ViewGroup parentView = (ViewGroup) findViewById(R.id.root);

        adVideoPlayerPlayback = (VideoPlayerWithAdPlayback) findViewById(R.id.videoplayer_with_ad_playback);
        adVideoPlayerPlayback.setEventEmitter(eventEmitter);

        videoPlayerController = new VideoPlayerController(parentView, eventEmitter, adVideoPlayerPlayback);

        // 広告の再生タイミングになるとemitされます
        // 広告のロードを開始して下さい
        eventEmitter.on(MAAdPlayerEvent.ADS_REQUEST_FOR_VIDEO, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d(TAG, event.getType());

                // 動画の下にバナー広告を表示する
                videoPlayerController.adCompanionBanner = (ViewGroup) findViewById(R.id.companion_ad_banner);

                // Ad Request && 再生開始
                videoPlayerController.play();
            }
        });

        videoPlayerController.init();
    }


    /** TODO: **/
    private void setupCuePoints() {
        String cuePointType = "ad";
        Map<String, Object> properties = new HashMap<String, Object>();
        Map<String, Object> details = new HashMap<>();

        // preroll
        CuePoint cuePoint = new CuePoint(CuePoint.PositionType.BEFORE, cuePointType, properties);
        details.put(Event.CUE_POINT, cuePoint);
        eventEmitter.emit(EventType.SET_CUE_POINT, details);

        // midroll at 10 seconds.
        int cuepointTime = 10 * (int) DateUtils.SECOND_IN_MILLIS;
        cuePoint = new CuePoint(cuepointTime, cuePointType, properties);
        details.put(Event.CUE_POINT, cuePoint);
        eventEmitter.emit(EventType.SET_CUE_POINT, details);
        mediaController.getBrightcoveSeekBar().addMarker(cuepointTime);

        // postroll
        cuePoint = new CuePoint(CuePoint.PositionType.AFTER, cuePointType, properties);
        details.put(Event.CUE_POINT, cuePoint);
        eventEmitter.emit(EventType.SET_CUE_POINT, details);
    }

    private void setupContents() {
        // load Contents
        Map<String, String> options = new HashMap<>();
        List<String> values = new ArrayList<>(Arrays.asList(VideoFields.DEFAULT_FIELDS));
        values.remove(VideoFields.HLS_URL);
        options.put("video_fields", StringUtil.join(values, ","));

        final Catalog catalog = new Catalog(brightcoveToken);
        catalog.findPlaylistByID(playistId, options, new PlaylistListener() {
            public void onPlaylist(Playlist playlist) {
                brightcoveVideoView.addAll(playlist.getVideos());
            }

            public void onError(String error) {
                Log.e(TAG, error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoPlayerController != null) {
            videoPlayerController.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (videoPlayerController != null) {
            videoPlayerController.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (videoPlayerController != null) {
            videoPlayerController.destroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
