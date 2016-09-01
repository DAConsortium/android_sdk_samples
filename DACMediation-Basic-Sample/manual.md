# DAC Ads SDK(Android)

DAC Ads SDKは、Android Studioにて組み込まれることが想定されています。

## 対応環境
AndroidOS 2.3～6.0
ApiLevel 9～23

## 必要なツール, DACライブラリ
+ AndroidStudio
+ DACAdsSDK(Android)
  - DACAdsSDK.jar

## SDK の組み込み手順

### 1. jarファイルのセッティング

DACAdsSDK.jarファイルをapp/libs/配下に格納します。

### 2. app/build.gradleの編集

dependenciesに以下を追加します。

```gradle
  compile 'com.google.android.gms:play-services-ads:8.4.0'
  compile 'com.google.code.gson:gson:2.2.4'
```

追加イメージ

```gradle
android {
    ...
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:23.1.0'
    compile 'com.google.android.gms:play-services-ads:8.4.0'
    compile 'com.google.code.gson:gson:2.2.4'
}

```

### 2. AndroidManifest.xml の編集
#### 2.1. permissionの追加
以下のpermissionを追加します。

```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

追加イメージ

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="jp.co.dac.dacadssdk.sample" >

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
    ...
```

#### 2.2. meta-dataの追加

以下のmeta-dataを追加します。

```xml
    <meta-data android:name="com.google.android.gms.version"
                     android:value="@integer/google_play_services_version" />
```

追加イメージ

```xml
    ...
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme" >

        <meta-data android:name="com.google.android.gms.version"
                   android:value="@integer/google_play_services_version" />

        <activity android:name=".MainActivity" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

    ...
```

### 3. バナー広告フォーマットの設定
#### 3.1. レイアウトファイルで配置する場合

##### 3.1.1. レイアウトファイルの編集

以下の記述を追加します。

```xml
    <jp.co.dac.dacadssdk.MediationView
        android:id="@+id/mediation_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
```
サンプル
SDK組み込みサンプル/DACAdsSDK-Sample1/app/src/main/AndroidManifest.xml

##### 3.1.2. javaファイルの編集

以下の処理を追加します。

```java
    import jp.co.dac.dacadssdk.MediationView;
```

```java
    MediationView mvBottom;
    mvBottom = (MediationView) findViewById(R.id.mediation_view);
    mvBottom.setPlacementInfo(<プレースメントI D > ,<広告表示高さ> ,<広告表示幅> );
    mvBottom.start();
```

```java
    @Override
    protected void onPause() {
        mvBottom.onPause();
        super.onPause();
    }
```

```java
    @Override
    protected void onResume() {
        super.onResume();
        mvBottom.onResume();
    }
```

サンプル
SDK組み込みサンプル/DACAdsSDK-Sample1/app/src/main/java/jp/co/dac/dacadssdk/sample1/MainActivity.java

#### 3.2. レイアウトファイルを利用しない場合

javaファイルに以下の処理を追加します。

```java
import jp.co.dac.dacadssdk.MediationView;
```

```java
    MediationView mvBottom;
    mvBottom = (MediationView) findViewById(R.id.mediation_view);
    mvBottom.setPlacementInfo(<プレースメントI D > ,<広告表示高さ> ,<広告表示幅> );
    rootView.addView(mvTop)
    mvBottom.start();
```

```java
    @Override
    protected void onPause() {
        mvBottom.onPause();
        super.onPause();
    }
```

```java
    @Override
    protected void onResume() {
        super.onResume();
        mvBottom.onResume();
    }
```

サンプル
SDK組み込みサンプル/DACAdsSDK-Sample2/app/src/main/java/jp/co/dac/dacadssdk/sample2/MainActivity.java

### 4. SDKからのコールバック
SDKからMediationViewListenerを介して以下のコールバックが通知されます。
- void onPrepareToShowMediationView();

    メディエーションビューが表示される直前のコールバック

- void onShowMediationView();

    メディエーションビューが表示される際のコールバック

- public void onPrepareToDismissMediationView();

    メディエーションビューが非表示となる際のコールバック

- public void onDismissMediationView();

    メディエーションビューが非表示となる際のコールバック

- public void onMediationViewLoadAd();

    メディエーションビュー内に広告がロードされたタイミングのコールバック

- public void onClickedMediationView();

    メディエーション広告がクリックされた際のコールバック

- public void onLoadFailedMediation();

    メディエーション情報の取得が失敗した際のコールバック

#### 4.1. コールバックの設定
必要に応じて設定してください。

javaファイルに以下の処理を追加します。

```java
import jp.co.dac.dacadssdk.MediationViewListener;
```

```java
        mvBottom.setListener(new MediationViewListener() {
            @Override
            public void onShowMediationView() {
                //メディエーションビューが表示される直前のコールバック
            }

            @Override
            public void onPrepareToShowMediationView() {
                //メディエーションビューが表示される際のコールバック
            }

            @Override
            public void onPrepareToDismissMediationView() {
                //メディエーションビューが非表示となる際のコールバック
            }

            @Override
            public void onMediationViewLoadAd() {
                //メディエーションビューが非表示となる際のコールバック
            }

            @Override
            public void onLoadFailedMediation() {
                //メディエーションビュー内に広告がロードされたタイミングのコールバック
            }

            @Override
            public void onDismissMediationView() {
                //メディエーション広告がクリックされた際のコールバック
            }

            @Override
            public void onClickedMediationView() {
                //メディエーション情報の取得が失敗した際のコールバック
            }
        });
```
