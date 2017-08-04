package com.levelzeros.utippy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.levelzeros.utippy.utility.PreferenceUtils;

/**
 * Created by Poon on 25/2/2017.
 */

/**
 * Activity to display splash screen
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Check user's initialization status and open appropriate activity for each scenario
         */
        if(!PreferenceUtils.checkInitializatonStatus(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
