# DAC Viewable SDK(Android)
DAC Viewable SDKは、Android Studioにて組み込まれることが想定されています。

## 対応環境
+ AndroidOS ：4.0.3～6.0
+ ApiLevel ：15～23

## 必要なツール, DACライブラリ
+ AndroidStudio
+ DACViewableSDK(Android)
  - DACViewableSDK.jar

## SDK の基本動作概要
指定した広告枠の可視状態を監視し、可視となった場合は実績を送信します。

## SDK が提供するAPI
パッケージ名:jp.co.dac.viewable.sdk
### クラス名:DACViewableSDK

|API|Description|
|:--|:--|
|void DACViewableSDK()|コンストラクタ<br>広告枠１個につきインスタンスを１個割り当ててください。<br>監視したい広告枠が複数ある場合はその数だけnewする必要があります|
|void void setPlacement(@NonNull String placement) |プレイスメントを設定|
|void setOid(@NonNull String oid)|データオーナーIDを設定|
|void setViewable(@NonNull Context context, @NonNull View targetView, @NonNull View parentView, int validRange, int validTime) |広告枠の可視監視設定<br>View targetView 広告枠のView<br>View parentView 広告枠の親View|
|void onResume() |onResumeのタイミングでコール|
|void onPause()|onPauseのタイミングでコール|

## SDK の組み込み手順
### 1. jarファイルのセッティング
DACViewableSDK.jarファイルをapp/libs/配下に格納します。

### 2. AndroidManifest.xml の編集
#### 2.1. permissionの追加
以下のpermissionを追加します。
```
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/AndroidManifest.xml

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
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainApplication.java

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

+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainApplication.java

+ 追加イメージ

```
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

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
+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/AndroidManifest.xml

+ 追加イメージ

```
<?xml version="1.0" encoding="utf-8"?>
<manifest package="jp.co.dac.viewable.sample"
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
#### 3.5. DACViewableSDKのインスタンス生成(MainApplication.java)
DACViewableSDKのインスタンスを生成する処理を追加します。

+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainApplication.java

+ 追加イメージ

```
import jp.co.dac.viewable.sdk.DACViewableSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {

    private DACViewableSDK dacViewableSDK = new DACViewableSDK();

    public DACViewableSDK getDACViewableSDK() {
        return dacViewableSDK;
    }

    @Override
    public void onCreate() {
    ...
```
#### 3.6. onResume時の処理の追加(MainApplication.java)
onActivityResumed()メソッド内に以下の処理を追加します。
```
dacViewableSDK.onResume();
```
+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainApplication.java

+ 追加イメージ

```
    ...
    @Override
    public void onActivityResumed(Activity activity) {
        dacViewableSDK.onResume();
    }
    ...
```
#### 3.7. onPause時の処理の追加(MainApplication.java)
onActivityPaused()メソッド内に以下の処理を追加します。
```
dacViewableSDK.onPause();
```
+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainApplication.java

+ 追加イメージ

```
    ...
    @Override
    public void onActivityPaused(Activity activity) {
        dacViewableSDK.onPause();
    }
    ...
```
### 4. 広告枠の設定
#### 4.1. 広告枠の親Viewを追加
広告枠の親Viewを追加します。
例としてLinearLayoutを追加しますが、ScrollViewやListView内に広告枠を設置する場合は任意のViewに置き換えてください。
```
    <LinearLayout
        android:id="@+id/parent_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="bottom">
    </LinearLayout>
```
+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/res/layout/activity_main.xml

+ 追加イメージ
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="jp.co.dac.viewable.sample.MainActivity"
    android:orientation="vertical">

    <LinearLayout
        android:id="@+id/parent_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="bottom">
    </LinearLayout>
</LinearLayout>
```

#### 4.2. 広告枠の追加
4.1.で追加した親View内に広告枠を追加します。
例としてTextViewを追加します。

+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/res/layout/activity_main.xml

+ 追加イメージ
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="jp.co.dac.viewable.sample.MainActivity"
    android:orientation="vertical">

    <LinearLayout
        android:id="@+id/parent_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="bottom">

        <TextView
            android:id="@+id/ad_view"
            android:layout_width="@dimen/adview_width"
            android:layout_height="@dimen/adview_height"
            android:layout_gravity="center"
            android:gravity="center"
            android:background="#cccccc"
            android:text="AD"/>

    </LinearLayout>
</LinearLayout>
```

### 5. 広告枠の可視監視設定
#### 5.1. MainActivity.javaの編集
setPlacement()、setOid()、setViewable()を追加します。

setPlacement()にプレイスメントを指定してください。
例として"12345"を設定。

setOid()にはデータオーナーIDを指定してください。
例として"yone.sample"を設定。

setViewable()の引数に手順4.で追加した親Viewと広告枠を使用します。
```
        mainApplication = (MainApplication) getApplication();

        TextView targetView =  (TextView) findViewById(R.id.target_view);
        ViewGroup parentView =  (ViewGroup) findViewById(R.id.parent_view);

        if (targetView == null || parentView == null) {
            return;
        }

        mainApplication.getDACViewableSDK().setPlacement("12345");
        mainApplication.getDACViewableSDK().setOid("yone.sample");
        mainApplication.getDACViewableSDK().setViewable(this, targetView, parentView);
```

+ サンプルファイル
SDK組み込みサンプル/ViewableSDK-Sample/app/src/main/java/jp/co/dac/viewable/sample/MainActivity.java

+ 追加イメージ
```
    MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainApplication = (MainApplication) getApplication();

        TextView targetView =  (TextView) findViewById(R.id.target_view);
        ViewGroup parentView =  (ViewGroup) findViewById(R.id.parent_view);

        if (targetView == null || parentView == null) {
            return;
        }

        mainApplication.getDACViewableSDK().setPlacement("12345");
        mainApplication.getDACViewableSDK().setOid("yone.sample");
        mainApplication.getDACViewableSDK().setViewable(this, targetView, parentView);
     }
```
