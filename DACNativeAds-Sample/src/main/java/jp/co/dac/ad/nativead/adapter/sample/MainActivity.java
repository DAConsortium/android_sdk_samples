package jp.co.dac.ad.nativead.adapter.sample;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import jp.co.dac.ad.nativead.DACNativeAdLoader;
import jp.co.dac.ad.nativead.DACNativeContentAd;
import jp.co.dac.ad.nativead.adapter.DACNativeAdHandlerImpl;
import jp.co.dac.ad.nativead.adapter.DACNativeAdManager;
import jp.co.dac.ad.nativead.adapter.FacebookNativeAdHandler;
import jp.co.dac.ad.nativead.adapter.sample.databinding.ActivityMainBinding;
import jp.co.dac.ad.nativead.adapter.sample.databinding.DacAdContentBinding;
import jp.co.dac.ad.nativead.adapter.sample.databinding.FbAdContentBinding;

public class MainActivity extends AppCompatActivity {

    private final int dacPlacementId = -1;
    private final String fbPlacementId = "your facebook placement id";

    private ActivityMainBinding binding;

    private DACNativeAdManager manager;
    private DACNativeAdHandlerImpl dacNativeAdHandler;
    private FacebookNativeAdHandler facebookNativeAdHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        prepareDACNativeAdHandler();
        prepareFacebookNativeAdHandler();

        manager = new DACNativeAdManager.Builder()
                .rotationTime(60_000, TimeUnit.MILLISECONDS)
                .addNativeAdHandler(dacNativeAdHandler)
                .addNativeAdHandler(facebookNativeAdHandler)
                .build();

        manager.loadAd();
    }

    private void prepareDACNativeAdHandler() {
        dacNativeAdHandler = new DACNativeAdHandlerImpl.Builder(binding.adViewContainer, dacPlacementId)
                .contentAdLoadedListener(new DACNativeAdLoader.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(@NonNull DACNativeContentAd contentAd) {
                        Log.d("dac ad", contentAd.toString());
                        dacAdLoaded(contentAd);
                    }
                })
                .errorAdListener(new DACNativeAdLoader.OnErrorAdListener() {
                    @Override
                    public void onErrorAd(@NonNull DACNativeAdLoader.DACAdException error) {
                        Log.d("dac ad", error.toString());
                    }
                })
                .build();
    }

    private void dacAdLoaded(DACNativeContentAd contentAd) {
        DacAdContentBinding contentBinding = DataBindingUtil.inflate(
                getLayoutInflater(), R.layout.dac_ad_content,
                binding.adViewContainer, false);

        binding.adViewContainer.removeAllViews();
        binding.adViewContainer.addView(contentBinding.getRoot());

        contentBinding.title.setText(contentAd.getTitle());
        contentBinding.description.setText(contentAd.getDescription());
        contentBinding.advertiser.setText(contentAd.getAdvertiser());
        loadImage(this, contentBinding.image, contentAd.getImageUrl());

        contentBinding.contentAd.setNativeAd(contentAd);
    }

    private static void loadImage(Context context, ImageView targetView, String url) {
        Picasso.with(context)
                .load(url)
                .into(targetView);
    }

    private void prepareFacebookNativeAdHandler() {
        facebookNativeAdHandler = new FacebookNativeAdHandler.Builder(binding.adViewContainer, fbPlacementId)
                .adListener(new AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        Log.e("facebook ad", adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        Log.d("facebook ad", ad.toString());

                        NativeAd nativeAd = facebookNativeAdHandler.getNativeAd();
                        if (nativeAd == null) {
                            return;
                        }

                        fbAdLoaded(ad, nativeAd);
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                    }
                })
                .build();
    }

    private void fbAdLoaded(Ad ad, NativeAd nativeAd) {
        Log.d("facebook ad", "fbAdLoaded");

        binding.adViewContainer.removeAllViews();

        View adView = NativeAdView.render(this, nativeAd, NativeAdView.Type.HEIGHT_100);
        binding.adViewContainer.addView(adView);
    }

    private void createFBCustomAdView(Ad ad, NativeAd nativeAd) {
        Log.d("facebook ad", "fbAdLoaded");

        FbAdContentBinding contentBinding = DataBindingUtil.inflate(
                getLayoutInflater(), R.layout.fb_ad_content,
                binding.adViewContainer, false);
        binding.adViewContainer.addView(contentBinding.adUnit);

        // Setting the Text.
        contentBinding.nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        contentBinding.nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        contentBinding.nativeAdTitle.setText(nativeAd.getAdTitle());
        contentBinding.nativeAdBody.setText(nativeAd.getAdBody());

        // Downloading and setting the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, contentBinding.nativeAdIcon);

        // Download and setting the cover image.
        NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
        contentBinding.nativeAdMedia.setNativeAd(nativeAd);

        // Add adChoices icon
        AdChoicesView adChoicesView = new AdChoicesView(this, nativeAd, true);
        contentBinding.adUnit.addView(adChoicesView, 0);

        nativeAd.registerViewForInteraction(contentBinding.adUnit);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (manager != null) {
            manager.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (manager != null) {
            manager.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (manager != null) {
            manager.destroy();
        }
    }
}
