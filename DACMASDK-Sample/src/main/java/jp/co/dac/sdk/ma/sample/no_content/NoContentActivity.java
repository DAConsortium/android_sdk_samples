package jp.co.dac.sdk.ma.sample.no_content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import jp.co.dac.sdk.ma.sample.R;

public class NoContentActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, NoContentActivity.class);
    }

    public static Intent getCallingIntent(Context context, String targetUrl) {
        Intent intent = new Intent(context, NoContentActivity.class);
        intent.putExtra("target_url", targetUrl);
        return intent;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_adpod);

        String targetUrl = getIntent().getStringExtra("target_url");
        if (TextUtils.isEmpty(targetUrl)) {
            targetUrl = getString(R.string.ad_tag_url);
        }

        populateAdFragment(targetUrl);
    }

    void populateAdFragment(String adTagUrl) {
        Fragment fragment = AdFragment.newInstance(adTagUrl);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
