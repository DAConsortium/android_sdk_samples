package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        populateOnlyAdFragment(getString(R.string.ad_tag_url));
    }

    void populateOnlyAdFragment(String adTagUrl) {
        Fragment fragment = ScrollableVideoPlayerFragment.newInstance(adTagUrl);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    void populateWithContentFragment(String adTagUrl) {
        Fragment fragment = ContentVideoPlayerFragment.newInstance(adTagUrl);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
