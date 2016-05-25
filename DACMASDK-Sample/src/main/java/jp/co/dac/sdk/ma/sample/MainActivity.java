package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.co.dac.sdk.ma.sample.adpod.AdpodActivity;
import jp.co.dac.sdk.ma.sample.content.ContentActivity;
import jp.co.dac.sdk.ma.sample.no_content.NoContentActivity;
import jp.co.dac.sdk.ma.sample.vertical.VerticalActivity;
import jp.co.dac.sdk.ma.sample.vmap.VmapActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        findViewById(R.id.adpod).setOnClickListener(view -> startActivity(AdpodActivity.getCallingIntent(this)));
        findViewById(R.id.vertical).setOnClickListener(view -> startActivity(VerticalActivity.getCallingIntent(this)));
        findViewById(R.id.content).setOnClickListener(view -> startActivity(ContentActivity.getCallingIntent(this)));
        findViewById(R.id.no_content).setOnClickListener(view -> startActivity(NoContentActivity.getCallingIntent(this)));
        findViewById(R.id.vmap).setOnClickListener(view -> startActivity(VmapActivity.getCallingIntent(this)));
    }
}
