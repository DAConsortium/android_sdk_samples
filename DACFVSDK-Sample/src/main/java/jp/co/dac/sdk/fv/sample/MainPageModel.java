package jp.co.dac.sdk.fv.sample;


import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class MainPageModel {

    @NotNull private final Context context;

    @NotNull public final ObservableInt adType;
    @NotNull public final ObservableField<String> headerImgSrc;
    @NotNull public final ObservableField<String> autoClose;
    @NotNull public final ObservableField<String> inViewPercent;
    @NotNull public final ObservableBoolean isInterstitial;
    @NotNull public final ObservableBoolean isCloseButton;
    @NotNull public final ObservableBoolean isCompanionBanner;
    @NotNull public final ObservableBoolean isScrollStop;
    @NotNull public final ObservableBoolean isFirstView;

    MainPageModel(@NonNull Context context) {
        this.context = context;
        this.adType = new ObservableInt(R.id.vertical_ad_type);
        this.headerImgSrc = new ObservableField<>("http://54.178.193.21:10080/tategumi_header.png");
        this.autoClose = new ObservableField<>("0");
        this.inViewPercent = new ObservableField<>("50");
        this.isInterstitial = new ObservableBoolean();
        this.isCloseButton = new ObservableBoolean();
        this.isCompanionBanner = new ObservableBoolean();
        this.isScrollStop = new ObservableBoolean();
        this.isFirstView = new ObservableBoolean(true);
    }

    public void submit(View view) {
        Intent intent = VideoActivity.getCallingIntent(context, getAdTagUrl(),
                headerImgSrc.get(), Integer.parseInt(autoClose.get()),
                Integer.parseInt(inViewPercent.get()),
                isInterstitial.get(), isCloseButton.get(), isCompanionBanner.get(),
                isScrollStop.get(), isFirstView.get());
        context.startActivity(intent);
    }

    private String getAdTagUrl() {
        switch (adType.get()) {
            case R.id.vertical_ad_type:
                return context.getString(R.string.ad_vertical_tag_url);
            case R.id.horizontal_ad_type:
                return context.getString(R.string.ad_horizontal_tag_url);
        }
        // unknown params
        throw new IllegalArgumentException();
    }
}
