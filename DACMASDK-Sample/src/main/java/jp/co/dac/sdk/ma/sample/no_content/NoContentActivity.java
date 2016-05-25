package jp.co.dac.sdk.ma.sample.no_content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import jp.co.dac.sdk.ma.sample.R;

public class NoContentActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, NoContentActivity.class);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_adpod);

        populateAdFragment(getString(R.string.ad_tag_url));
    }

    void populateAdFragment(String adTagUrl) {
        Fragment fragment = AdFragment.newInstance(adTagUrl);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
