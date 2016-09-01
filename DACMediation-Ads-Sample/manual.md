# Mediation + Facebook Audience Networkマニュアル

Android Studio + Gradleを使うことを想定しています.


## 事前準備

- Android Studio 1.5以上


## 組み込み

### ライブラリの組み込み

build.gradleにdependenciesを追加します.


```gradle
repositories {
    maven {
        url 'https://raw.githubusercontent.com/DAConsortium/android-sdk/master/'
    }
}

dependencies {
    compile 'jp.co.dac:dac-mediation-sdk:${latest sdk version}'
    compile ('jp.co.dac:dac-mediation-sdk-adapter:${latest sdk version}') {
        exclude module: 'dac-mediation-sdk'
    }
}
```


## 実装

まず, MediationViewを生成します. これは, メディエーションを開始するための基本クラスになります.

```java
MediationView mediationView = new MediationView(this);
mediationView.setPlacementInfo("your dac placement id", "ad height", "ad width");
```


次に, `FacebookRotateHandler`を生成します.
これは, Facebook Audience NetworkのAdViewを生成するために必要になります.

ここでのAdSizeは, メディエーションのサイズと合わせて下さい.
例えば, 幅320, 高さ50の広告の場合は, `AdSize.BANNER_320_50`を使って下さい.

```java
FacebookRotateHandler fbRotateHandler = new FacebookRotateHandler.Builder(
        binding.adViewContainer, "your facebook id", AdSize.BANNER_320_50)
        .build();

mediationView.addRotateHandler(fbRotateHandler);
```

次に, 生成したMediationViewを表示したい箇所に`ViewGroup.addView`して, 最後に`MediationView.start`を下さい.

```java
view.addView(mediationView);
mediationView.start();
```

これで, 広告が表示されます.

最後に, 生成した`MediationView`をActivityのライフサイクルと連動させるようにして下さい.

```
@Override
protected void onResume() {
    super.onResume();

    if (mediationView != null) {
        mediationView.onResume();
    }
}

@Override
protected void onPause() {
    super.onPause();

    if (mediationView != null) {
        mediationView.onPause();
    }
}

@Override
protected void onDestroy() {
    super.onDestroy();

    fbRotateHandler.release();
    mediationView
            .removeRotateHandler(fbRotateHandler);
}
```


より詳細な実装は, サンプルの`MainActivity.java`を参照して下さい.


## 確認

広告が表示されていることを確認して下さい.
