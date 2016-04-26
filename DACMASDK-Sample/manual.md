# DAC Multimedia Ads SDK(Android)

DACMASDKは、Android Studioにて組み込まれることが想定されています。

DACMASDKで保証されるAndroidのバージョンは、Android4.0.3～5.1.1 APILevel 15～22になります。

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

app/build.gradle

```
buildscript {
    maven {
        url 'https://raw.githubusercontent.com/DAConsortium/android-sdk/master/'
    }
}

android {
    ...

    dependencies {
        compile 'com.android.support:appcompat-v7:23.0.1'
        compile 'com.google.android.gms:play-services-ads:8.1.0'

        compile ("jp.co.dac:dac-ma-sdk:0.7.0") {
            exclude module: 'support-annotations'
        }
    }
}
```


### 2. Adを再生するViewの作成

広告を再生するViewを作成します. 以下のソースコードを参考にして下さい.

-- jp.co.dac.sdk.ma.sample
-    - AdFragment                     # サンプルフラグメント(adpod, vmap, content, vertical)
-    - VideoPlayerController          # Playerを再生するタイミング等を管理
-    - VideoPlayerWithAdPlayback      # Playerを配置するコンテナ


### 3. 再生する広告のURLをString Resourcesに指定

res/values/strings.xml

```
<resources>
    <string name="ad_tag_url">${Your ad tag url}></string>
</resources>
```


### 4. 広告を再生するViewをレイアウトに記述

※ fragment_ad.xml内の各layout_widthやlayout_height等を変更することで、レイアウトを変更することができます。

以下のタグをlayoutに記述して下さい.

res/layout/fragment_ad.xml

```xml
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- 広告レイアウト -->
        <include
            android:id="@+id/dac_ad_layout"
            layout="@layout/dac_ad_layout" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="150dp"
            android:background="@android:color/black"
            android:text="test1"
            android:textColor="@android:color/white" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:background="@android:color/holo_blue_bright"
            android:text="test2" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="300dp"
            android:background="@android:color/holo_green_dark"
            android:text="test3" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="500dp"
            android:background="@android:color/holo_orange_dark"
            android:text="test4" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="500dp"
            android:background="@android:color/holo_green_dark"
            android:text="test5" />
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
