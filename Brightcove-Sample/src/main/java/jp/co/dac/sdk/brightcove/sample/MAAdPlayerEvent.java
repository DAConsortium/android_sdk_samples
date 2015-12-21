package jp.co.dac.sdk.brightcove.sample;


public final class MAAdPlayerEvent {
    public static final String ADS_REQUEST_FOR_VIDEO = "dac_adsRequestForVideo";

    public static final String DID_START_AD = "dac_didStartAd";
    public static final String DID_PAUSE_AD = "dac_didPauseAd";
    public static final String DID_RESUME_AD = "dac_didResumeAd";
    public static final String DID_COMPLETE_AD = "dac_didCompleteAd";

    public static final String DID_FAIL_TO_PLAY_AD = "dac_didFailToPlayAd";

    public static final String DID_OPEN_FULLSCREEN = "dac_didOpenFullscreen";
    public static final String DID_CLOSE_FULLSCREEN = "dac_didCloseFullscreen";

    public static final String FORCE_CLOSE_AD = "dac_forceCloseAd";
}
