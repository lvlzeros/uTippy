package com.levelzeros.utippy.ui;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.levelzeros.utippy.LoginActivity;
import com.levelzeros.utippy.R;
import com.levelzeros.utippy.utility.PreferenceUtils;

/**
 * Created by Poon on 5/6/2017.
 */

public class DialogSessionExpired extends DialogFragment {
    public static final String DIALOG_SEESION_EXPIRED_TAG = "session-expired";

    private TextView mReloginTextView;
    private TextView mCancelTextView;

    public DialogSessionExpired() {
    }

    public static DialogSessionExpired newInstance() {
        DialogSessionExpired frag = new DialogSessionExpired();

        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_relogin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCancelTextView = (TextView) view.findViewById(R.id.button_cancel);
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mReloginTextView = (TextView) view.findViewById(R.id.button_relogin);
        mReloginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloginTask();
            }
        });
    }

    private void reloginTask() {
        PreferenceUtils.logOutRequest(getActivity().getApplicationContext());

        Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoutIntent);

        dismiss();
    }
}
