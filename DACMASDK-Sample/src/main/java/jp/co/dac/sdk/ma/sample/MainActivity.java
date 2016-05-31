package jp.co.dac.sdk.ma.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

        findViewById(R.id.adpod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AdpodActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.vertical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VerticalActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContentActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.no_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(NoContentActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.vmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VmapActivity.getCallingIntent(MainActivity.this));
            }
        });
    }
}
