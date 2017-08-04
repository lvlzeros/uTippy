package com.levelzeros.utippy;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by Poon on 5/6/2017.
 */

public class uTippy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileDownloader.init(getApplicationContext());
    }
}
