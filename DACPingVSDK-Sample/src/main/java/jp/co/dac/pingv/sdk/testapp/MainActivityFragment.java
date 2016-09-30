package jp.co.dac.pingv.sdk.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.dac.dacadssdk.MediationView;
import jp.co.dac.dacadssdk.MediationViewListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MediationView mediationView;
    private ViewGroup mediationViewParent;

    private MainApplication mainApplication;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mainApplication = (MainApplication) getActivity().getApplication();

        mediationViewParent = (ViewGroup) view.findViewById(R.id.mediation_view_parent);
        mediationView = (MediationView) view.findViewById(R.id.mediation_view);

        createMediationView();
    }

    private void createMediationView() {
        mediationView.setListener(new MediationViewListener() {

            @Override
            public void onShowMediationView() {
            }

            @Override
            public void onPrepareToShowMediationView() {
            }

            @Override
            public void onPrepareToDismissMediationView() {
            }

            @Override
            public void onMediationViewLoadAd() {
            }

            @Override
            public void onLoadFailedMediation() {
            }

            @Override
            public void onDismissMediationView() {
            }

            @Override
            public void onClickedMediationView() {
            }
        });
        mediationView.setPlacementInfo(28253, 50, 320);
        mediationView.start();

        // PingV.
        mainApplication.getDACPingVSDK2().setPlacement(String.valueOf(28253));
        mainApplication.getDACPingVSDK2().setOid("yone.mediation");
        mainApplication.getDACPingVSDK2().setViewable(getContext(), mediationView, mediationViewParent);
    }
}
