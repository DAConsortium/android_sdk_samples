# DAC First View (Android)

- - -
## 準備するもの
* Android Studio : 1.0 or newer
- - -


## 保証環境

### 対応OSバージョン

- OS  4.0.3 ~ 6.0
- API level 15 ~ 23


### 保証端末

- Xperia Z3
- Xperia A
- Xperia Z3 Compact
- ARROWS NX
- GALAXY S5
- AQUOS PHONE ZETA
- GALAXY S4 AQUOS ZETA
- Xperia Z1
- Xperia UL
- Xperia Z
- Xperia Z2 Disney Mobile on docomo
- AQUOS PHONE Xx mini
- GALAXY S III
- Xperia ZL2
- AQUOS SERIE
- Xperia VL


## 導入手順

以下はサンプルコードをそのまま使った場合の例になります。
カスタマイズが必要な場合はサンプルコードを変更して下さい。

- res/layout/fragment_main_basic.xml: 広告を表示するためのView(以下、広告View)を実装します
- jp/co/dac/sdk/fv/sample/MainFragment.java: 広告Viewに対してActivity(Fragment)のライフサイクルに合わせて、SDKの適切なメソッドを呼び出します

以下、サンプルコードをベースにして具体的な組み込み手順を説明していきます。


### 1. Android Studioにaarライブラリを追加します

必要なaarファイルをダウンロードするため、app/build.gradleに以下を追加します。

```gradle
repositories {
    maven {
        url 'https://raw.githubusercontent.com/DAConsortium/android-sdk/master/'
    }
}

android {
    ...

    dependencies {
        ･･･
        compile 'com.google.android.gms:play-services-ads:8.4.0'

        compile 'jp.co.dac:dac-ma-sdk:0.6.0'
        compile 'jp.co.dac:dac-fv-sdk:0.5.1'
        compile 'jp.co.dac:dac-ad-manager:0.5.1'
    }
}
```


### 2. 広告を再生したい場所に、広告Viewを配置します

広告を再生したい場所に、FirstViewInlinePlayerContainerを追加します。

```xml
...
<!-- Ad: start -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center"
    android:weightSum="1.0"
    android:background="@android:color/black" >

    <jp.co.dac.sdk.fv.widget.DACSDKMAAdVideoPlayer
        android:id="@+id/video_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <jp.co.dac.sdk.fv.widget.DACSDKMAAdTextureVideoPlayerView
            android:id="@+id/video_player"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center" />
    </jp.co.dac.sdk.fv.widget.DACSDKMAAdVideoPlayer>
</LinearLayout>

<FrameLayout
    android:id="@+id/companion_ad_banner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:visibility="gone" />
<!-- Ad: end -->
...
```

詳しいコードは、サンプルのfragment_main_basic.xmlにあります。


### 3. 広告Viewの初期化メソッド、その他必要なメソッドを呼び出します

広告Viewを管理しているActivity(Fragment)で、以下のように初期化メソッド, Activityのライフサイクル関連のメソッドを呼び出して下さい。

```java
public class MainFragment extends Fragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoPlayerContainer = (DACSDKMAAdVideoPlayer) view.findViewById(R.id.video_container);
        dacVideoPlayerView = (DACVideoPlayerView) view.findViewById(R.id.video_player);

        new DACSDKMAAdVideoPlayer.SettingsBuilder()
                .fixedMaxHeight(getContentHeight())
                .companionBanner((ViewGroup) view.findViewById(R.id.companion_ad_banner))
                .middleOutScreenPercent(adScrollOutPercent)
                .videoOrientationListener(new DACSDKMAAdVideoPlayer.VideoOrientationListener() {
                    @Override
                    public void onVideoSize(View containerView, int width, int height) {
                        Log.d("videoSize", width + "-" + height);
                    }
                })
                .closeButton(true)
                .apply(videoPlayerContainer);
    }

    @Override
    public void onResume() {
        super.onResume();
        videoPlayerContainer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        videoPlayerContainer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoPlayerContainer.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoPlayerContainer.onChangeOrientation();
    }

```

変数(adTagUrl)には、適切な値を設定して下さい。

詳しいコードは, サンプルのMainFragment.javaにあります。


### 4. 動作確認をします

正しく広告が表示されることを確認して下さい。
