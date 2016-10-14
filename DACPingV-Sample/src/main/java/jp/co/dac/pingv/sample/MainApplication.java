package jp.co.dac.pingv.sample;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import jp.co.dac.pingv.sdk.DACPingVSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {

    private DACPingVSDK dacPingVSDK = new DACPingVSDK();

    public DACPingVSDK getDACPingVSDK() {
        return dacPingVSDK;
    }

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
        getDACPingVSDK().onResume(getApplicationContext());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        getDACPingVSDK().onPause(getApplicationContext());
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
