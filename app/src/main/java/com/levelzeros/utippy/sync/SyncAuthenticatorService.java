package com.levelzeros.utippy.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Poon on 11/3/2017.
 */

public class SyncAuthenticatorService extends Service {
    private SyncAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new SyncAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
