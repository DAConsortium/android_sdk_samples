# DAC PingV SDK(Android)
## 準備するもの
+ Android Studio : 1.0 or newer - - -

## 対応環境
+ AndroidOS ：4.0.3～6.0
+ ApiLevel ：15～23

## 必要なツール, DACライブラリ
+ AndroidStudio
+ DACPingVSDK(Android)

## SDKの基本動作概要
アプリがフォアグラウンドからバックグラウンドに入る時に、収集したデータの送信を行う。

## 提供するAPI
パッケージ名:jp.co.dac.pingv.sdk

### クラス名:DACPingVSDK
|API|Description|
|:--|:--|
|void DACPingVSDK()|コンストラクタ|
|void onResume(@NonNull Context context) |onResumeのタイミングでコール|
|void onPause(@NonNull Context context)|onPauseのタイミングでコール|
|void setOid(@NonNull String oid)|データオーナーIDを設定|
|void setEventIds(@NonNull String eventIds)|EventIdを送信データに格納（複数回コール時はカンマ区切りで連結）|
|void setPageId(@NonNull String pageId)|PageIdを送信データに格納|
|void setLocation(@NonNullString key, @NonNull double value)|Locationデータを送信データに格納|
|void setExtras(@NonNull String extras)|その他収集データ(JSON形式)を送信データに格納|

## 導入手順
### 1.  Android Studioにaarライブラリを追加します
必要なaarファイルをダウンロードするため、app/build.gradleに以下を追加します。
```
  repositories {
    maven {
        url 'https://raw.githubusercontent.com/DAConsortium/android-sdk/master/'
    }
}
...
android {
    ...

    dependencies {
        ･･･
        compile 'jp.co.dac:dac-pingv-sdk:1.0.1'
    }
}
```

### 2. AndroidManifest.xmlの編集
#### 2.1. permissionの追加
以下のpermissionをAndroidManifest.xmlに追加します。
```
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
+ サンプルファイル
SDK組み込みサンプル/DACPingV-Sample/src/main/AndroidManifest.xml

+ 追加イメージ

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="jp.co.dac.fone.sample" >

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
    ...
```

### 3. SDKの初期設定
#### 3.1. MainApplication.java ファイルの新規作成
MainApplication.javaを新規作成します。

#### 3.2. MainApplication.java ファイルの編集
MainApplicationクラスでandroid.app.Applicationを継承します。
onCreate()メソッドを追加します。
+ サンプルファイル
SDK組み込みサンプル/DACPingV-Sample/src/main/java/jp/co/dac/pingv/sample/MainApplication.java

+ 追加イメージ

```
import android.app.Application;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }
```
#### 3.3. MainApplication.java ファイルの編集
MainApplicationクラスでandroid.app.Application.ActivityLifecycleCallbacksインターフェイスを継承し必須メソッドを実装します。
onCreate()内に以下の処理を追加します。
```
registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback);
```

+ 追加イメージ

```
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import jp.co.dac.pingv.sdk.DACPingVSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {
    ...

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
```
#### 3.4. AndroidManifest.xml の編集

application要素のname属性に手順3.1で作成したクラスを指定します。

```
android:name=".MainApplication"
```

+ 追加イメージ

```
<?xml version="1.0" encoding="utf-8"?>
<manifest package="jp.co.dac.pingv.sample"
          xmlns:android="http://schemas.android.com/apk/res/android">
    ...
    <application
        android:name=".MainApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
    ...
```
#### 3.5. DACPingVSDKのインスタンス生成(MainApplication.java)
DACPingVSDKのインスタンスを生成する処理を追加します。

+ 追加イメージ

```
import jp.co.dac.pingv.sdk.DACPingVSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {

    private DACPingVSDK dacPingVSDK = new DACPingVSDK();

    public DACPingVSDK getDACPingVSDK() {
        return dacPingVSDK;
    }

    @Override
    public void onCreate() {
    ...
```
#### 3.6. onResume時の処理の追加(MainApplication.java)
onActivityResumed()メソッド内に以下の処理を追加します。
```
getDACPingVSDK().onResume();
```

+ 追加イメージ
```
    ...
    @Override
    public void onActivityResumed(Activity activity) {
        getDACPingVSDK().onResume();
    }
    ...
```
#### 3.7. onPause時の処理の追加(MainApplication.java)
onActivityPaused()メソッド内に以下の処理を追加します。
```
getDACPingVSDK().onPause();
```

+ 追加イメージ
```
    ...
    @Override
    public void onActivityPaused(Activity activity) {
        getDACPingVSDK().onPause();
    }
    ...
```
### 4. データオーナーIDを設定
データオーナーIDを設定します。

+ 記述例
```
    mmainApplication.getDACPingVSDK().setOid("hogehoge.oid");
```

### 5. 各種データID設定（任意）
必要に応じて各種データを追加します。

+ 記述例
```
    mainApplication.getDACPingVSDK().setEventIds("hogehoge");
```