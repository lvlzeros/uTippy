package com.levelzeros.utippy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    public static final String FACEBOOK_PAGE_LINK = "https://www.facebook.com/lvlzeros/";
    public static final String JOIN_US_FORM_LINK = "https://docs.google.com/forms/d/e/1FAIpQLSeU9GbM6oCJ25zCmKSRBl57PXrfAopHL81ovabyoTuSeBSduQ/viewform?usp=sf_link";
    public static final String FEEDBACK_FORM_LINK = "https://docs.google.com/forms/d/e/1FAIpQLScH5tsyZdluplclyEMr04Oy0mYKAYWtUZG2CbskOLI2DccTpQ/viewform?usp=sf_link";
    public static final String GITHUB_PAGE_LINK = "https://github.com/lvlzeros/uTippy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        Button mFollowButton = (Button) findViewById(R.id.button_follow);
        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(FACEBOOK_PAGE_LINK);
            }
        });

        Button mJoinUsButton = (Button) findViewById(R.id.button_join_us);
        mJoinUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(JOIN_US_FORM_LINK);
            }
        });

        Button mFeedbackButton = (Button) findViewById(R.id.button_feedback);
        mFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(FEEDBACK_FORM_LINK);
            }
        });

        Button mGithubButton = (Button) findViewById(R.id.button_github);
        mGithubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(GITHUB_PAGE_LINK);
            }
        });
    }

    void openUrl(String urlStr){
        Uri uri = Uri.parse(urlStr);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(intent, getString(R.string.prompt_open_with));

        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(chooser);
        }
    }

}
