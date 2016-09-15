package com.inger.android.ollintest;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        try {
            PackageInfo pInfo = null;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;

            TextView versionText = (TextView) findViewById(R.id.version);
            versionText.setText(getResources().getString(R.string.app_version) + " " + version + "." + verCode);

            final TextView button_privacy_terms = (TextView) findViewById(R.id.button_privacy_terms);
            button_privacy_terms.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent privacyScreen = new Intent(getApplicationContext(), PrivacyActivity.class);
                    startActivity(privacyScreen);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
