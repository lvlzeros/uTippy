package com.levelzeros.utippy.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.levelzeros.utippy.R;

/**
 * Created by Poon on 15/2/2017.
 */

/**
 * Utility to handle Preferences
 */
public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    /**
     * For first time login purpose
     */
    private static final String INTIALIZED_KEY = "initialized";
    private static final boolean INTIALIZED_DEFAULT = false;

    private static final String USER_WEB_TOKEN_KEY = "user_web_token";

    private static final String SITE_NAME_KEY = "site_name";
    private static final String USER_STUDENT_ID_KEY = "user_student_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_ID_KEY = "user_id";

    /**
     * To initialize user's courses for first time
     */
    private static final String USER_COURSE_INITIALIZED_KEY = "course_initialized";
    private static final boolean USER_COURSE_INITIALIZED_DEFAULT = false;


    /**
     * Check user initialization status upon starting the app
     *
     * @param context To access SharedPreferences
     * @return true if user has initialized in previous run
     */
    public static boolean checkInitializatonStatus(Context context) {
        SharedPreferences checkInitializatonStatus = PreferenceManager.getDefaultSharedPreferences(context);
        boolean initialized = checkInitializatonStatus.getBoolean(INTIALIZED_KEY, INTIALIZED_DEFAULT);
        if (!initialized) {
            return false;
        }
        String userWebToken = checkInitializatonStatus.getString(USER_WEB_TOKEN_KEY, "");
        if (userWebToken.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Update user's initialization status
     *
     * @param context              To access SharedPreferences
     * @param initializationStatus
     */
    public static void updateInitializationStatus(Context context, boolean initializationStatus) {
        SharedPreferences updateLogInStatus = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = updateLogInStatus.edit();
        editor.putBoolean(INTIALIZED_KEY, initializationStatus);
        editor.apply();
    }

    /**
     * Update user's web token
     *
     * @param context      To access SharedPreferences
     * @param userWebToken
     */
    public static void updateWebToken(Context context, String userWebToken) {
        SharedPreferences updateLogInStatus = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = updateLogInStatus.edit();
        editor.putString(USER_WEB_TOKEN_KEY, userWebToken);
        editor.apply();
    }

    /**
     * Obtain user's web token
     *
     * @param context To access SharedPreferences
     * @return
     */
    public static String getWebToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String webToken = sharedPreferences.getString(USER_WEB_TOKEN_KEY, null);

        return webToken;
    }

    /**
     * Update userDetails
     *
     * @param context   To access SharedPreferences
     * @param sitename
     * @param studentId
     * @param userName
     * @param userId
     * @return
     */
    public static boolean updateUserDetails(Context context, String sitename, String studentId, String userName, String userId) {
        SharedPreferences updateLogInStatus = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = updateLogInStatus.edit();

        if (null == userId || userId.isEmpty()) {
            return false;
        }

        editor.putString(SITE_NAME_KEY, sitename);
        editor.putString(USER_STUDENT_ID_KEY, studentId);
        editor.putString(USER_NAME_KEY, userName);
        editor.putString(USER_ID_KEY, userId);
        editor.apply();

        return true;
    }

    /**
     * Obtain user's ID
     *
     * @param context To access SharedPreferences
     * @return
     */
    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = sharedPreferences.getString(USER_ID_KEY, null);

        return userId;
    }

    /**
     * Check user's enrolled courses initialization status
     *
     * @param context To access SharedPreferences
     * @return
     */
    public static boolean checkCourseInitializationStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean courseInitialized = sharedPreferences.getBoolean(USER_COURSE_INITIALIZED_KEY, USER_COURSE_INITIALIZED_DEFAULT);
        if (courseInitialized) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update user's enrolled courses initialization status
     *
     * @param context To access SharedPreferences
     * @param initializationStatus
     */
    public static void updateCourseInitializationStatus(Context context, boolean initializationStatus) {
        SharedPreferences updateCourseInitStatus = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = updateCourseInitStatus.edit();
        editor.putBoolean(USER_COURSE_INITIALIZED_KEY, initializationStatus);
        editor.apply();
    }

    /**
     * Check if user enabled Class Reminder
     * @param context To access SharedPreferences
     * @return
     */
    public static boolean checkClassReminderEnabled(Context context) {
        SharedPreferences checkClassReminder = PreferenceManager.getDefaultSharedPreferences(context);

        return checkClassReminder.getBoolean(context.getString(R.string.pref_enable_reminder_key), true);
    }

    /**
     * Check if user's preferred Class Reminder time
     * @param context To access SharedPreferences
     * @return
     */
    public static int getUserPreferredReminderTime(Context context) {
        SharedPreferences checkPrefReminderTime = PreferenceManager.getDefaultSharedPreferences(context);

        String timeStr = checkPrefReminderTime.getString(context.getString(R.string.pref_reminder_time_key), context.getString(R.string.reminder_thirty_min));
        if(timeStr.equals(context.getString(R.string.reminder_five_min))){
            return 5;
        } else if(timeStr.equals(context.getString(R.string.reminder_ten_min))){
            return 10;
        } else if(timeStr.equals(context.getString(R.string.reminder_fifteen_min))){
            return 15;
        } else if(timeStr.equals(context.getString(R.string.reminder_twenty_min))){
            return 20;
        } else if(timeStr.equals(context.getString(R.string.reminder_twenty_five_min))){
            return 25;
        } else if(timeStr.equals(context.getString(R.string.reminder_thirty_min))){
            return 30;
        } else if(timeStr.equals(context.getString(R.string.reminder_one_hour))){
            return 60;
        } else if(timeStr.equals(context.getString(R.string.reminder_one_day))){
            return 1440;
        } else {
            return 30;
        }
    }

    /**
     * Handle user's request to logout
     * Reset all SharedPreferences
     *
     * @param context
     */
    public static void logOutRequest(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(INTIALIZED_KEY, INTIALIZED_DEFAULT);
        editor.putBoolean(USER_COURSE_INITIALIZED_KEY, USER_COURSE_INITIALIZED_DEFAULT);
        editor.putString(SITE_NAME_KEY, null);
        editor.putString(USER_STUDENT_ID_KEY, null);
        editor.putString(USER_NAME_KEY, null);
        editor.putString(USER_ID_KEY, null);
        editor.putString(USER_WEB_TOKEN_KEY, null);

        editor.apply();
    }
}
