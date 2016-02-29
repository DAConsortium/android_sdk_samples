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

- res/layout/fragment_mediation.xml: 広告を表示するためのView(以下、広告View)を実装します
- jp/co/dac/sdk/fv/sample/MediationFragment.java: 広告Viewに対してActivity(Fragment)のライフサイクルに合わせて、SDKの適切なメソッドを呼び出します
-


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

        compile 'jp.co.dac:dac-ma-sdk:0.5.1'
        compile 'jp.co.dac:dac-fv-sdk:0.5.1'
        compile 'jp.co.dac:dac-ad-manager:0.6.0'

        compile 'jp.co.dac:dac-mediation-sdk:1.1.4'
        compile 'jp.co.dac:dac-mediation-sdk-client:1.1.5'
    }
}
```


### 2. 広告を再生したい場所に、広告Viewを配置します

広告を再生したい場所に、DACAdContainerViewを追加します。

```xml
...
<!-- Ad: start -->
<jp.co.dac.ad.manager.DACAdContainerView
    android:id="@+id/ad_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center" />

<FrameLayout
    android:id="@+id/companion_ad_banner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:visibility="gone" />
<!-- Ad: end -->
...
```

詳しいコードは、サンプルのfragment_mediation.xmlにあります。


### 3. 広告Viewの初期化メソッド、その他必要なメソッドを呼び出します

広告Viewを管理しているActivity(Fragment)で、以下のように初期化メソッド, Activityのライフサイクル関連のメソッドを呼び出して下さい。

```java
public class MediationFragment extends Fragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        dacAdContainerView = (DACAdContainerView) view.findViewById(R.id.ad_container);

        DACSDKMAAdVideoPlayerClient dacSDKMAAdVideoPlayerClient = new DACSDKMAAdVideoPlayerClient.Builder(getContext(), adTagUrl)
                .fixedMaxHeight(getContentHeight()) // 最大の高さを設定
                .companionBanner((ViewGroup) view.findViewById(R.id.companion_ad_banner)) // 動画の下にバナー広告を表示する
                // Videoのサイズを受け取る
                .videoOrientationListener(new DACSDKMAAdVideoPlayer.VideoOrientationListener() {
                    @Override
                    public void onVideoSize(View containerView, int width, int height) {
                        Log.d("videoSize", width + "-" + height);
                    }
                })
                .middleOutScreenPercent(50) // 動画を再生/停止する範囲(0~100%)を指定
                .closeButton(true)          // 閉じるボタンを表示/非表示にする
                .build();

        MediationAdClient mediationAdClient = new MediationAdClient.Builder(
                    getContext(), mediationPlacementId, mediationWidth, mediationHeight)
                .build();

        final DACAdRequestManager manager = new DACAdRequestManager.Builder()
                .addClient(dacSDKMAAdVideoPlayerClient) // first try: video ad
                .addClient(mediationAdClient)           // second try: mediation(banner) ad
                .clientCallback(new DACAdRequestManager.ClientCallback() {
                    @Override
                    public void success(View adView, int adIndex) {
                        Log.d(TAG, "ad-onSuccess:" + adIndex);
                    }

                    @Override
                    public void failure() {
                        Log.d(TAG, "all-ad-failure");
                    }
                })
                .build();
        dacAdContainerView.loadAd(manager);
    }

    @Override
    public void onResume() {
        super.onResume();
        dacAdContainerView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        dacAdContainerView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dacAdContainerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dacAdContainerView.onChangeOrientation();
    }

```

変数(adTagUrl, mediationPlacementId, mediationWidth, mediationHeight)には、適切な値を設定して下さい。

詳しいコードは, サンプルのMediationFragment.javaにあります。
