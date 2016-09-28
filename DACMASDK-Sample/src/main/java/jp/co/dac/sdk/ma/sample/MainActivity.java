package jp.co.dac.sdk.ma.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import jp.co.dac.sdk.ma.sample.adpod.AdpodActivity;
import jp.co.dac.sdk.ma.sample.content.ContentActivity;
import jp.co.dac.sdk.ma.sample.no_content.NoContentActivity;
import jp.co.dac.sdk.ma.sample.vertical.VerticalActivity;
import jp.co.dac.sdk.ma.sample.vmap.VmapActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_main);

        findViewById(R.id.adpod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AdpodActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.vertical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VerticalActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContentActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.no_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(NoContentActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.vmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VmapActivity.getCallingIntent(MainActivity.this));
            }
        });

        findViewById(R.id.custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View view = inflater.inflate(R.layout.input_url_dialog, (ViewGroup) findViewById(R.id.root), false);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Input custom url")
                        .setView(view)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = (EditText) view.findViewById(R.id.url);
                                String url = editText.getText().toString();
                                startActivity(ContentActivity.getCallingIntent(MainActivity.this, url));
                            }
                        })
                        .show();
            }
        });
    }
}
