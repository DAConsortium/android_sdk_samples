# Android Native Ad SDKマニュアル

- Javadoc(TODO)


## 事前準備

- Android Studio 1.5以上


## 組み込み手順


### ライブラリ組み込み

サンプルのbuild.gradleようにdependenciesを追加します.

```gradle
repositories {
    ...
    maven {
        url 'https://raw.githubusercontent.com/DAConsortium/android-sdk/master/'
    }
}

dependencies {
    compile 'jp.co.dac:dac-native-ad:${latest sdk version}'

    ...
}
```


### 実装

サンプルの`MainActivity.java`を参考にして下さい.

最初に`DACNativeAdLoader`を使い, 広告のロードを開始します.

```java
DACNativeAdLoader adLoader = new Builder(this, adTagUrl)
    .contentAdListener(new OnContentAdLoadedListener() {
        @Override
        public void onContentAdLoaded(@NonNull DACNativeContentAd contentAd) {
            createContentAdView(contentAd);
        }
    })
    .errorAdListener(new OnErrorAdListener() {
        @Override
        public void onErrorAd(@NonNull DACAdException error) {
            performContentAdError(error);
        }
    })
    .build();
adLoader.loadAd();
```

広告のロードが成功したら, `onContentAdLoaded`がコールされます. ここで適切にViewに対して`DACNativeContentAd`の値をマッピングして下さい.
`dac_ad_content.xml`にサンプルの広告Viewがあります. これをカスタマイズして使って下さい.

また, 広告のロードが失敗したら`onErrorAd`がコールされます. ここでアプリに合わせたエラーハンドリングをして下さい.


### 確認

実際に, 広告が表示がされていることを確認して下さい.
