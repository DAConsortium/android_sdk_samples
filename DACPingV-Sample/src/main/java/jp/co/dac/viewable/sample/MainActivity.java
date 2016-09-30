package jp.co.dac.viewable.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.co.dac.viewable.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MainApplication mainApplication = (MainApplication) getApplication();

        mainApplication.getDACViewableSDK().setPlacement("12345");
        mainApplication.getDACViewableSDK().setOid("yone.sample");
        mainApplication.getDACViewableSDK().setViewable(this, binding.targetView, binding.parentView);
    }
}
