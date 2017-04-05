package com.cicese.android.matest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by netzahdzc on 7/18/16.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final TextView button_edit_account = (TextView) findViewById(R.id.button_edit_account);
        button_edit_account.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activationCodeScreen = new Intent(getApplicationContext(), EditAccountActivity.class);
                startActivity(activationCodeScreen);
            }
        });

        final TextView button_activation_code = (TextView) findViewById(R.id.button_activation_code);
        button_activation_code.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activationCodeScreen = new Intent(getApplicationContext(), ActivationAccountActivity.class);
                startActivity(activationCodeScreen);
            }
        });

        final TextView button_about = (TextView) findViewById(R.id.button_about);
        button_about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent aboutScreen = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutScreen);
            }
        });

        final TextView button_sensor_data = (TextView) findViewById(R.id.button_sensor_data);
        button_sensor_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent manualUploadScreen = new Intent(getApplicationContext(), ManualUploadActivity.class);
                startActivity(manualUploadScreen);
            }
        });
    }

}
