package jp.co.dac.ad.nativead.sample;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import jp.co.dac.ad.nativead.DACNativeAdLoader;
import jp.co.dac.ad.nativead.DACNativeContentAd;
import jp.co.dac.ad.nativead.sample.databinding.ActivityMainBinding;
import jp.co.dac.ad.nativead.sample.databinding.DacAdContentBinding;

import static jp.co.dac.ad.nativead.DACNativeAdLoader.Builder;
import static jp.co.dac.ad.nativead.DACNativeAdLoader.DACAdException;
import static jp.co.dac.ad.nativead.DACNativeAdLoader.OnContentAdLoadedListener;
import static jp.co.dac.ad.nativead.DACNativeAdLoader.OnErrorAdListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String AD_TAG_URL = "http://webdemo.dac.co.jp/sdfactory/stsn/nativead/index.html";

    @VisibleForTesting
    ActivityMainBinding binding;
    @VisibleForTesting
    DacAdContentBinding contentBinding;
    @VisibleForTesting
    DACNativeContentAd contentAd;
    @VisibleForTesting
    DACAdException adException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.perform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareWebView(AD_TAG_URL);
            }
        });
    }

    @VisibleForTesting
    void prepareWebView(String adTagUrl) {
        DACNativeAdLoader adLoader = new Builder(this, adTagUrl)
                .contentAdListener(new OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(@NonNull DACNativeContentAd contentAd) {
                        Log.d(TAG, "onContentAdLoaded");
                        createContentAdView(contentAd);
                    }
                })
                .errorAdListener(new OnErrorAdListener() {
                    @Override
                    public void onErrorAd(@NonNull DACAdException error) {
                        Log.e(TAG, String.valueOf(error));
                        performContentAdError(error);
                    }
                })
                .build();
        adLoader.loadAd();
    }

    private void createContentAdView(DACNativeContentAd contentAd) {
        this.contentAd = contentAd;
        contentBinding = DataBindingUtil.inflate(
                getLayoutInflater(), R.layout.dac_ad_content,
                binding.adPlaceholder, false);

        binding.adPlaceholder.removeAllViews();
        binding.adPlaceholder.addView(contentBinding.getRoot());

        contentBinding.title.setText(contentAd.getTitle());
        contentBinding.description.setText(contentAd.getDescription());
        contentBinding.advertiser.setText(contentAd.getAdvertiser());
        loadImage(this, contentBinding.image, contentAd.getImageUrl());

        contentBinding.contentAd.setNativeAd(contentAd);
    }

    private void performContentAdError(DACAdException error) {
        adException = error;

        // TODO: should handle error
    }

    private static void loadImage(Context context, ImageView targetView, String url) {
        Picasso.with(context)
                .load(url)
                .into(targetView);
    }
}
