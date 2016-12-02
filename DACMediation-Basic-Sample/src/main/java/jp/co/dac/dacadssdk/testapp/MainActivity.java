package jp.co.dac.dacadssdk.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.co.dac.dacadssdk.MediationView;
import jp.co.dac.dacadssdk.MediationViewListener;

public class MainActivity extends AppCompatActivity {

    private MediationView mvBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mvBottom = (MediationView) findViewById(R.id.mediation_view);
        mvBottom.setPlacementInfo(getResources().getInteger(R.integer.placement_id_sample), 50, 320);
        mvBottom.setListener(new MediationViewListener() {
            @Override
            public void onShowMediationView() {
                //メディエーションビューが表示される直前のコールバック
            }

            @Override
            public void onPrepareToShowMediationView() {
                //メディエーションビューが表示される際のコールバック
            }

            @Override
            public void onPrepareToDismissMediationView() {
                //メディエーションビューが非表示となる際のコールバック
            }

            @Override
            public void onMediationViewLoadAd() {
                //メディエーションビューが非表示となる際のコールバック
            }

            @Override
            public void onLoadFailedMediation() {
                //メディエーションビュー内に広告がロードされたタイミングのコールバック
            }

            @Override
            public void onDismissMediationView() {
                //メディエーション広告がクリックされた際のコールバック
            }

            @Override
            public void onClickedMediationView() {
                //メディエーション情報の取得が失敗した際のコールバック
            }
        });
        mvBottom.start();
    }

    @Override
    protected void onPause() {
        mvBottom.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mvBottom.onResume();
    }
}
