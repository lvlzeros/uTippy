package com.levelzeros.utippy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ClassEditorActivity extends AppCompatActivity
        implements ClassEditorActivityFragment.OnClassSaveListener{
    private static final String TAG = "ClassEditorActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveClicked() {

        finish();
    }

    @Override
    public void onDeleteClicked() {

        finish();
    }
}
