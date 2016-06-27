package jp.co.dac.sdk.audience.network.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;

import jp.co.dac.dacadssdk.MediationView;
import jp.co.dac.dacadssdk.ads.FacebookRotateHandler;
import jp.co.dac.sdk.audience.network.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int DAC_PLACEMENT_ID = 0; // input your placement id
    private static final String FACEBOOK_ID = "your facebook id";

    private ActivityMainBinding binding;

    private MediationView mediationView;
    private FacebookRotateHandler fbRotateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        prepareFacebookAd();
        populateMediationView();
    }

    private void prepareFacebookAd() {
        fbRotateHandler = new FacebookRotateHandler.Builder(binding.adViewContainer,
                FACEBOOK_ID, AdSize.BANNER_320_50)
                .build();
    }

    private void populateMediationView() {
        mediationView = new MediationView(this);
        mediationView.setPlacementId(DAC_PLACEMENT_ID, 50, 320);

        mediationView
                .addRotateHandler(fbRotateHandler);

        binding.adViewContainer.addView(mediationView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediationView != null) {
            mediationView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediationView != null) {
            mediationView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        fbRotateHandler.release();
        mediationView
                .removeRotateHandler(fbRotateHandler);
    }
}
