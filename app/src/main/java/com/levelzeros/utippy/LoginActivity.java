package com.levelzeros.utippy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.levelzeros.utippy.utility.NetworkUtils;
import com.levelzeros.utippy.utility.PreferenceUtils;

import java.net.URL;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements UserDetailTask.UserDetailCallBack {

    private Context mContext = this;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserDetailTask mUserDetailTask = null;

    // UI references.
    private LinearLayout mLoginWelcomeContainer;
    private LinearLayout mLoginFormContainer;
    private Button mGetStartedButton;
    private EditText mStudentIdEditText;
    private EditText mPasswordEditText;
    private TextView mCancelOption;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setElevation(0f);

        mLoginWelcomeContainer = (LinearLayout) findViewById(R.id.login_welcome_screen);
        mLoginFormContainer = (LinearLayout) findViewById(R.id.login_form_screen);
        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mCancelOption = (TextView) findViewById(R.id.login_cancel);

        //Set up the welcome screen
        mLoginWelcomeContainer.setVisibility(View.VISIBLE);
        mLoginFormContainer.setVisibility(View.GONE);
        mGetStartedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginForm();
            }
        });

        mCancelOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLoginForm();
            }
        });


        // Set up the login form.
        mStudentIdEditText = (EditText) findViewById(R.id.student_id);

        mPasswordEditText = (EditText) findViewById(R.id.password);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

    }

    /**
     * Hide welcome screen and show login form
     */
    private void showLoginForm(){
        mLoginWelcomeContainer.setVisibility(View.GONE);
        mLoginFormContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Hide login form and some welcome screen
     */
    private void hideLoginForm(){
        mLoginWelcomeContainer.setVisibility(View.VISIBLE);
        mLoginFormContainer.setVisibility(View.GONE);

        //Boilerplate code to hide soft keyboard
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mStudentIdEditText.setError(null);
        mPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String studentId = mStudentIdEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(studentId)) {
            mStudentIdEditText.setError(getString(R.string.error_field_required));
            focusView = mStudentIdEditText;
            cancel = true;
        } else if (!isStudentIdValid(studentId)) {
            mStudentIdEditText.setError(getString(R.string.error_invalid_email));
            focusView = mStudentIdEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                Snackbar.make(mLoginFormView, getString(R.string.network_error), 10000)
                        .setAction(getString(R.string.prompt_retry),new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                attemptLogin();
                            }
                        })
                        .show();
            } else {
                showProgress(true);
                mCancelOption.setVisibility(View.GONE);
                mAuthTask = new UserLoginTask(this, this);
                mAuthTask.execute(studentId, password);
            }
        }
    }

    /**
     * Check student ID input validity
     *
     * @param studentId
     * @return
     */
    private boolean isStudentIdValid(String studentId) {
        return studentId.length() >= 3;
    }

    /**
     * Check password input validity
     *
     * @param password
     * @return
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Update user's details upon success login, then proceed to MainActivity
     * else prompt to retry login
     *
     * @param context
     * @param initalizationStatus
     */
    @Override
    public void onUserDetailsObtained(Context context, boolean initalizationStatus) {
        showProgress(false);
        if (initalizationStatus) {
            PreferenceUtils.updateInitializationStatus(context, initalizationStatus);

            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);

            finish();
        } else {
            mAuthTask = null;
            mUserDetailTask = null;
            mCancelOption.setVisibility(View.VISIBLE);

            mPasswordEditText.setError(getString(R.string.error_initalization));
            mPasswordEditText.requestFocus();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "UserLoginTask";

        private final Context mContext;
        private final UserDetailTask.UserDetailCallBack mCallBack;

        UserLoginTask(Context context, UserDetailTask.UserDetailCallBack callBack) {
            mContext = context;
            mCallBack = callBack;
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String mStudentID = params[0];
            String mPassword = params[1];

            URL webTokenRequestUrl = NetworkUtils.buildWebTokenQueryUrl(mStudentID, mPassword);

            try {
                String jsonWebTokenResponse = NetworkUtils.getResponseFromHttpsUrl(webTokenRequestUrl, mContext);

                String webToken = NetworkUtils.getWebToken(jsonWebTokenResponse);
                return webToken;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String webToken) {
            mAuthTask = null;


            if (null != webToken && !webToken.isEmpty()) {

                PreferenceUtils.updateWebToken(mContext, webToken);

                mUserDetailTask = new UserDetailTask(mContext, mCallBack);
                mUserDetailTask.execute();

            } else {
                showProgress(false);
                mCancelOption.setVisibility(View.VISIBLE);

                mPasswordEditText.setError(getString(R.string.error_incorrect_password));
                mPasswordEditText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


/**
 * Asynchronous task to obtain user's details
 */
class UserDetailTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "UserDetailTask";

    public interface UserDetailCallBack {
        void onUserDetailsObtained(Context context, boolean initalizationStatus);
    }

    private Context mContext;
    private UserDetailCallBack mCallback;

    public UserDetailTask(Context context, UserDetailCallBack callBack) {
        mContext = context;
        mCallback = callBack;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        URL userDetailRequestUrl = NetworkUtils.buildUserDetailQueryUrl(mContext);

        try {
            String jsonWebTokenResponse = NetworkUtils.getResponseFromHttpsUrl(userDetailRequestUrl, mContext);

            return NetworkUtils.getUserDetail(jsonWebTokenResponse, mContext);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean initalizationStatus) {
        mCallback.onUserDetailsObtained(mContext, initalizationStatus);
    }
}