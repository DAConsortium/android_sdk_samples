package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        String adTagUrl = getIntent().getStringExtra("ad_tag_url");
        if (adTagUrl == null) {
            adTagUrl = getString(R.string.ad_tag_url);
        }

        populateAdFragment(adTagUrl);
    }

    void populateAdFragment(String adTagUrl) {
        Fragment fragment = AdFragment.newInstance(adTagUrl);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
