package jp.co.dac.pingv.sdk.testapp;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import jp.co.dac.pingv.sdk.DACPingVSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {

    private static final String TAG = MainApplication.class.getSimpleName();

    private DACPingVSDK dacPingVSdk1 = new DACPingVSDK();
    private DACPingVSDK dacPingVSdk2 = new DACPingVSDK();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        registerActivityLifecycleCallbacks(this);
    }

    public DACPingVSDK getDACPingVSDK1() {
        return dacPingVSdk1;
    }

    public DACPingVSDK getDACPingVSDK2() {
        return dacPingVSdk2;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed");
        //PingV.
        getDACPingVSDK1().onResume(getApplicationContext());
        getDACPingVSDK2().onResume(getApplicationContext());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused");
        //PingV.
        getDACPingVSDK1().onPause(getApplicationContext());
        getDACPingVSDK2().onPause(getApplicationContext());
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
}
