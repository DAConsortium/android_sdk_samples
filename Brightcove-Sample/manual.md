# Brightcove組み込み手順

サンプルコードは, src/ 以下にあります.

※ Brightcove PlayerとVideo Ad Playerは全くの別物です. 別々のインスタンスになります.
※ Brightcove上に, DACMASDKで配信されている広告を表示するためのサンプルになります.


## 実装手順

### DACMASDKのインストール

build.gradleに以下を追記して下さい.

```gradle
dependencies {
    compile(name: 'DACMASDK', ext: 'aar')

    compile "com.brightcove.player:android-sdk:4.6.+"
    compile 'com.google.android.gms:play-services-ads:8.4.+'
    ...
}
```

※ 4.6以外のバージョンの場合, 要検証


### 組み込み手順

#### 広告URLの設定

src/main/res/values/strings.xml

```xml
<string name="ad_tag_url"><![CDATA[https://m.one.impact-ad.jp/preview-bsw?creative_id=486056]]></string>
```

適切なURLをセットして下さい.


#### レイアウトの設定

src/main/res/layout/content_main.xml

```xml
    ...
    <jp.co.dac.sdk.brightcove.sample.VideoPlayerWithAdPlayback
        android:id="@+id/videoplayer_with_ad_playback"
        android:layout_width="match_parent"
        android:layout_height="280dp"
        android:background="@android:color/black"
        android:visibility="gone" >

        <jp.co.dac.ma.sdk.widget.DACVideoPlayerView
            android:id="@+id/videoplayer"
            android:layout_width="match_parent"
            android:layout_height="280dp"
            android:layout_centerVertical="true"
            android:background="@android:color/black" />

        <RelativeLayout
            android:id="@+id/video_controller"
            android:layout_width="match_parent"
            android:layout_height="280dp"
            android:padding="6dp" >

            <jp.co.dac.ma.sdk.widget.MuteButton
                android:id="@+id/mute_button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:scaleType="fitCenter"
                android:background="@android:color/transparent" />

            <jp.co.dac.ma.sdk.widget.PRButton
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:scaleType="fitCenter"
                android:layout_alignParentRight="true"
                android:background="@android:color/transparent" />

            <jp.co.dac.ma.sdk.widget.FullscreenButton
                android:id="@+id/fullscreen_button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentRight="true"
                android:layout_alignParentBottom="true"
                android:scaleType="fitCenter"
                android:background="@android:color/transparent" />

            <jp.co.dac.ma.sdk.widget.ReplayButton
                android:id="@+id/replay_button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:scaleType="fitCenter"
                android:visibility="gone"
                android:background="@android:color/transparent" />
        </RelativeLayout>

    </jp.co.dac.sdk.brightcove.sample.VideoPlayerWithAdPlayback>
    ...
```

上記のようにして, `Brightcove Player`に被せる形で広告をセットして下さい.


#### 必要なファイル

- src/main/java/jp/co/dac/sdk/brightcove/sample/VideoPlayerWithAdPlayback.java
- src/main/java/jp/co/dac/sdk/brightcove/sample/VideoPlayerController.java
- src/main/java/jp/co/dac/sdk/brightcove/sample/MAAdPlayerEvent.java

をアプリにコピーして下さい. これらのファイルは`Activity`で使用します.

※ SDKを作ると, ここの手順がいらなくなります.


#### Activityへの記述

src/main/java/jp/co/dac/sdk/brightcove/sample/MainActivity.java


##### DACMASDKの初期化, 広告の再生開始イベントのハンドリング

広告を表示したいBrightCove Player(Activity)で, 下記のように実装して下さい.

```java
private void setupMA() {
    adVideoPlayerPlayback = (VideoPlayerWithAdPlayback) findViewById(R.id.videoplayer_with_ad_playback);
    adVideoPlayerPlayback.setEventEmitter(eventEmitter);

    videoPlayerController = new VideoPlayerController(eventEmitter, MainActivity.this, adVideoPlayerPlayback);
    videoPlayerController.init();

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
}
```

##### 広告が終了した時の処理

```java
// 広告が終了した時にemitされます
// コンテンツを再開して下さい
eventEmitter.on(MAAdPlayerEvent.DID_COMPLETE_AD, new EventListener() {
    @Override
    public void processEvent(Event event) {
        Log.d(TAG, event.getType());

        // auto playing
        brightcoveVideoView.start();
    }
});
```

今回のサンプルの場合, 広告が終了すると, 自動再生をするようになっています. アプリに合わせてカスタマイズして下さい.


##### 広告を挿入したいタイミング(`CuePoint`)を指定

```java
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
```

- 最初に挿入したい場合は`preroll`
- 途中に挿入したい場合は秒数指定(`midroll`)
- 最後に挿入したい場合は`postroll`

を使用して下さい.


##### Activityのライフサイクルに合わせて, メソッドを呼び出す

```java
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
```


さらに詳細なコードはsrc/main/java/jp/co/dac/sdk/brightcove/sample/MainActivity.java を参照して下さい.


#### 動作確認

広告が最初に再生されるか, 広告が終了したらコンテンツが自動再生されるか, 動画の下にバナー広告が正しく表示されていることを確認して下さい.


### メモ

広告の 再生/停止/終了などのタイミングに合わせて, 下記のイベントが発行されます. イベント一覧は`MAAdPlayerEvent.java`に定義されています.

- 広告読み込み準備: MAAdPlayerEvent.ADS_REQUEST_FOR_VIDEO
- 再生開始: MAAdPlayerEvent.DID_START_AD
- 再生停止: MAAdPlayerEvent.DID_PAUSE_AD
- 再生完了: MAAdPlayerEvent.DID_COMPLETE_AD
- 再生失敗: MAAdPlayerEvent.DID_FAIL_TO_PLAY_AD
- 再生再開: MAAdPlayerEvent.DID_RESUME_AD


## 参考

- [Brightcove Plugin Guide for Android](https://github.com/BrightcoveOS/android-plugin-guide)
