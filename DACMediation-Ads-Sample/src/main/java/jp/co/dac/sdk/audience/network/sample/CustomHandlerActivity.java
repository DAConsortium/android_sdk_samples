package jp.co.dac.sdk.audience.network.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import jp.co.dac.dacadssdk.MediationView;
import jp.co.dac.dacadssdk.models.AdvertisementServer;
import jp.co.dac.dacadssdk.models.response.Tag;
import jp.co.dac.sdk.audience.network.sample.databinding.ActivityMainBinding;

public class CustomHandlerActivity extends AppCompatActivity {

    private static final int DAC_PLACEMENT_ID = 0; // input your placement id
    private static final String FACEBOOK_ID = "your facebook id";

    private ActivityMainBinding binding;

    private AdView adView;
    private MediationView mediationView;
    private MediationView.RotateHandler rotateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        populateMediation();
    }

    private void populateMediation() {
        mediationView = new MediationView(this);
        mediationView.setPlacementId(DAC_PLACEMENT_ID, 50, 320);
        rotateHandler = new MediationView.RotateHandler() {
            @Override
            public boolean willPrepareView(int placementId, @NonNull Tag tag, @NonNull AdvertisementServer server) {
                return false;
            }

            @Override
            public boolean showView(int placementId, @NonNull Tag tag, @NonNull AdvertisementServer server) {
                return false;
            }
        };
        mediationView.addRotateHandler(rotateHandler);
        binding.adViewContainer.addView(mediationView);
    }

    private void prepareFacebookAd() {
        AdSettings.addTestDevice("9c363dc44d64e258e12ba60bd4a98432");
        AdSettings.addTestDevice("f74a6f2d086d15562af1770b4fd62dbf");

        if (binding.adViewContainer.indexOfChild(adView) != -1) {
            binding.adViewContainer.removeView(adView);
        }

        adView = new AdView(this, FACEBOOK_ID, AdSize.BANNER_320_50);
        adView.setVisibility(View.GONE);
        binding.adViewContainer.addView(adView);
        adView.loadAd();
    }

    private void removeFacebookAd() {
        if (binding.adViewContainer.indexOfChild(adView) != -1) {
            adView.destroy();
            binding.adViewContainer.removeView(adView);
            adView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null) {
            adView.destroy();
            adView = null;
        }

        mediationView.removeRotateHandler(rotateHandler);
    }
}
