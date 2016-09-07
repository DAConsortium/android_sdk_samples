package jp.co.dac.viewable.sample;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import jp.co.dac.viewable.sdk.DACViewableSDK;

public class MainApplication extends Application implements ActivityLifecycleCallbacks {

    private DACViewableSDK dacViewableSDK = new DACViewableSDK();

    public DACViewableSDK getDACViewableSDK() {
        return dacViewableSDK;
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
        dacViewableSDK.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        dacViewableSDK.onPause();
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
