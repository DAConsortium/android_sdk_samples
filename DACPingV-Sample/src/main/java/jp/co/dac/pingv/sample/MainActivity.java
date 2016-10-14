package jp.co.dac.pingv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jp.co.dac.pingv.sample.MainApplication mainApplication = (jp.co.dac.pingv.sample.MainApplication) getApplication();

        mainApplication.getDACPingVSDK().setOid("yone.sample");
    }
}
