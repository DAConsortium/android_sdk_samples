# DAC Multimedia Ads SDK(Android)

DACMASDKは、Android Studioにて組み込まれることが想定されています。

DACMASDKで保証されるAndroidのバージョンは、Android4.0.3～5.1.1 ApiLevel15～22になります。

また, サンプルコードを内包しているので, それを参考にして下さい.


## 必要なツール, DACライブラリ

+ AndroidStudio
+ DACMultimediaAdsSDK(Android)
  - DACMASDK.aar
  - DACMASDK-Widget.aar


## ソースコード組み込み手順

以下はサンプルコードをそのまま使った場合の例になります.
カスタマイズが必要な場合はサンプルコードを参考にして, 変更して下さい.


### 1. aarファイルのセッティング

必要なaarファイル(DACMASDK.aar, DACMASDK-Widget.aar)をapp/libs/以下に格納し, app/build.gradleに以下のように記述します。

app/build.gradle

```
android {
    ...

    dependencies {
        compile 'com.android.support:appcompat-v7:23.0.1'
        compile 'com.google.android.gms:play-services-ads:8.1.0'

        compile(name: 'DACMASDK', ext: 'aar')
        compile(name: 'DACMASDK-Widget', ext: 'aar')
    }
}
```


### 2. Adを再生するViewの作成

広告を再生するViewを作成します. 以下のようにソースコードを配置して下さい.

-- jp.co.dac.videoad.ma
-    - ScrollableVideoPlayerFragment  # サンプルフラグメント
-    - VideoPlayerController          # Playerを再生するタイミング等を管理
-    - VideoPlayerWithAdPlayback      # Playerを配置するコンテナ


### 3. 再生する広告のURLをString Resourcesに指定

res/values/strings.xml

```
<resources>
    <string name="ad_tag_url"><![CDATA[http://xp1.zedo.com/jsc/xp2/fns.vast?n=2696&c=25/11&d=17&s=2&v=vast3&pu=__page-url__&ru=__referrer__&pw=__player-width__&ph=__player-height__&z=__random-number__]]></string>
</resources>
```


### 4. 広告を再生するViewをレイアウトに記述

※ scrollable_video_player_fragment.xml内の各layout_widthやlayout_height等を変更することで、レイアウトを変更することができます。

以下のタグをlayoutに記述して下さい.

res/layout/scrollable_video_player.xml

```
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <!-- 広告: 開始 -->
        <jp.co.dac.sdk.ma.sample.VideoPlayerWithAdPlayback
            android:id="@+id/videoplayer_with_ad_playback"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <jp.co.dac.ma.sdk.widget.DACVideoPlayerView
                android:id="@+id/videoplayer"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_centerHorizontal="true" />

            <jp.co.dac.ma.sdk.widget.PRButton
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignRight="@+id/videoplayer"
                android:background="@android:color/transparent" />

            <jp.co.dac.ma.sdk.widget.MuteButton
                android:id="@+id/mute_button"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:layout_marginLeft="4dp"
                android:paddingBottom="4dp"
                android:layout_alignParentBottom="true"
                android:scaleType="fitCenter"
                android:background="@android:color/transparent" />

            <jp.co.dac.ma.sdk.widget.FullscreenButton
                android:id="@+id/fullscreen_button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignRight="@+id/videoplayer"
                android:layout_marginRight="8dp"
                android:paddingBottom="8dp"
                android:layout_alignParentBottom="true"
                android:background="@android:color/transparent" />
        </jp.co.dac.sdk.ma.sample.VideoPlayerWithAdPlayback>
        <!-- 広告: 終了 -->

    </LinearLayout>

</ScrollView>
```


### 5. AndroidManigest.xmlに`INTERNET`, `ACCESS_NETWORK_STATE` permissionを追加します

manigests/AndroidManigest.xml

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="jp.co.dac.videoad.ma">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    ...

</manifest>
```

これで広告を再生するプレイヤーを実装することができました！
