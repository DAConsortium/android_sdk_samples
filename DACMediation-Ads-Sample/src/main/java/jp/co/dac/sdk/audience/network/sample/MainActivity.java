package jp.co.dac.sdk.audience.network.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.ads.AdSize;

import jp.co.dac.dacadssdk.MediationView;
import jp.co.dac.dacadssdk.MediationViewListener;
import jp.co.dac.dacadssdk.ads.FacebookRotateHandler;
import jp.co.dac.sdk.audience.network.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String FACEBOOK_ID = "your facebook id";
    private static final int DAC_PLACEMENT_ID = 18859;

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
//        AdSettings.addTestDevice("your device id");
        fbRotateHandler = new FacebookRotateHandler.Builder(binding.adViewContainer,
                FACEBOOK_ID, AdSize.BANNER_320_50)
                .build();
    }

    private void populateMediationView() {
        mediationView = new MediationView(this);
        mediationView.setPlacementId(DAC_PLACEMENT_ID, 50, 320);

        mediationView.setListener(new MediationViewListener() {
            @Override
            public void onPrepareToShowMediationView() {
            }

            @Override
            public void onShowMediationView() {
            }

            @Override
            public void onPrepareToDismissMediationView() {
            }

            @Override
            public void onDismissMediationView() {
            }

            @Override
            public void onMediationViewLoadAd() {
            }

            @Override
            public void onClickedMediationView() {
            }

            @Override
            public void onLoadFailedMediation() {
            }
        });

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
