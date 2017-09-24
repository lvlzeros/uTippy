package com.levelzeros.utippy.utility;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.levelzeros.utippy.R;
import com.levelzeros.utippy.data.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static android.content.ContentValues.TAG;

/**
 * Created by Poon on 14/2/2017.
 */

public class NetworkUtils {

    /**
     * General convention to build URL
     */
    private static final String WEB_SERVICE_FUNCTION = "wsfunction";
    private static final String WEB_SERVICE_TOKEN = "wstoken";
    private static final String WEB_SERVICE_REST_FORMAT = "moodlewsrestformat";
    private static final String WEB_SERVICE_REST_JSON_FORMAT = "json";
    private static final String USER_ID = "userid";
    private static final String COURSE_ID = "courseid";
    private static final String FORUM_ID = "forumid";
    private static final String DISCUSSION_ID = "discussionid";
    private static final String GENERAL_QUERY_URL = "https://ulearn.utp.edu.my/webservice/rest/server.php"; //TODO

    /**
     * Variables to obtain user's web token
     */
    private static final String WEB_QUERY_URL = "https://ulearn.utp.edu.my/login/token.php"; //TODO

    private static final String WEB_TOKEN_QUERY_USERNAME = "username";
    private static final String WEB_TOKEN_QUERY_PASSWORD = "password";
    private static final String WEB_TOKEN_QUERY_SERVICE = "service";
    private static final String WEB_TOKEN_SERVICE_CODE = "moodle_mobile_app";

    /**
     * File download token
     */
    private static final String FILE_DOWNLOAD_TOKEN = "token";

    /**
     * Web service function to obtain desired data
     */
    private static final String USER_DETAIL_WEB_SERVICE_FUNCTION = "core_webservice_get_site_info";
    private static final String USER_COURSE_WEB_SERVICE_FUNCTION = "core_enrol_get_users_courses";
    private static final String COURSE_CONTENT_WEB_SERVICE_FUNCTION = "core_course_get_contents";
    private static final String FORUM_WEB_SERVICE_FUNCTION = "mod_forum_get_forum_discussions_paginated";


    /**
     * Build URL to retrieve user's web token
     *
     * @param studentId student ID
     * @param password  password
     * @return URL to request user's web token
     */
    public static URL buildWebTokenQueryUrl(String studentId, String password) {
        Uri webTokenQueryUri = Uri.parse(WEB_QUERY_URL).buildUpon()
                .appendQueryParameter(WEB_TOKEN_QUERY_USERNAME, studentId)
                .appendQueryParameter(WEB_TOKEN_QUERY_PASSWORD, password)
                .appendQueryParameter(WEB_TOKEN_QUERY_SERVICE, WEB_TOKEN_SERVICE_CODE)
                .build();

        try {
            URL webTokenQueryUrl = new URL(webTokenQueryUri.toString());
            return webTokenQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse downloaded Json string to obtain user's web token
     *
     * @param jsonData Downloaded Json string
     * @return User's web token
     * @throws JSONException
     */
    public static String getWebToken(String jsonData) throws JSONException {
        final String WEB_TOKEN_TAG = "token";

        JSONObject webTokenJson = new JSONObject(jsonData);
        String webToken = webTokenJson.getString(WEB_TOKEN_TAG);

        return webToken;
    }


    /**
     * Build URL to retrieve user's details, such as user ID, name etc
     *
     * @param context Pass in context to obtain user's web token from SharedPreferences
     * @return URL to retrieve user's details
     */
    public static URL buildUserDetailQueryUrl(Context context) {
        String webToken = PreferenceUtils.getWebToken(context);

        if (null == webToken) {
            return null;
        }

        Uri userDetailQueryUri = Uri.parse(GENERAL_QUERY_URL).buildUpon()
                .appendQueryParameter(WEB_SERVICE_FUNCTION, USER_DETAIL_WEB_SERVICE_FUNCTION)
                .appendQueryParameter(WEB_SERVICE_REST_FORMAT, WEB_SERVICE_REST_JSON_FORMAT)
                .appendQueryParameter(WEB_SERVICE_TOKEN, webToken).build();

        try {
            URL userDetailQueryUrl = new URL(userDetailQueryUri.toString());
            return userDetailQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse downloaded Json string to obtain user's details, such as user ID, name etc
     *
     * @param jsonData Downloaded Json string
     * @param context  Used to save user details into SharedPreferences
     * @return true if user details has successfully saved
     * @throws JSONException
     */
    public static boolean getUserDetail(String jsonData, Context context) throws JSONException {
        final String SITENAME_TAG = "sitename";
        final String STUDENT_ID_TAG = "username";
        final String USER_NAME_TAG = "firstname";
        final String USER_ID_TAG = "userid";

        JSONObject userDetailJson = new JSONObject(jsonData);

        String siteName = userDetailJson.getString(SITENAME_TAG);
        String studentId = userDetailJson.getString(STUDENT_ID_TAG);
        String userName = userDetailJson.getString(USER_NAME_TAG);
        String userId = userDetailJson.getString(USER_ID_TAG);

        Log.d(TAG, "getUserDetail: userId = " + userId);
        return PreferenceUtils.updateUserDetails(context, siteName, studentId, userName, userId);
    }


    /**
     * Build URL to retrieve user's enrolled course
     *
     * @param context Pass in context to obtain user's web token & user's id from SharedPreferences
     * @return URL to retrieve user's enrolled course
     */
    public static URL buildUserCourseQueryUrl(Context context) {
        String webToken = PreferenceUtils.getWebToken(context);
        String userId = PreferenceUtils.getUserId(context);

        Uri userCourseQueryUri = Uri.parse(GENERAL_QUERY_URL).buildUpon()
                .appendQueryParameter(WEB_SERVICE_FUNCTION, USER_COURSE_WEB_SERVICE_FUNCTION)
                .appendQueryParameter(WEB_SERVICE_TOKEN, webToken)
                .appendQueryParameter(USER_ID, userId)
                .appendQueryParameter(WEB_SERVICE_REST_FORMAT, WEB_SERVICE_REST_JSON_FORMAT)
                .build();

        try {
            URL userCourseQueryUrl = new URL(userCourseQueryUri.toString());
            return userCourseQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean getUserCourse(String jsonData, Context context) throws JSONException {
        final String COURSE_ID_TAG = "id";
        final String COURSE_NAME_TAG = "fullname";
//        final String COURSE_ENROLLED_USER_COUNT_TAH = "enrolledusercount";

        JSONArray userCourseJsonArray = new JSONArray(jsonData);
        Vector<ContentValues> userCourseContentVector = new Vector<>(userCourseJsonArray.length());

        for (int i = 0; i < userCourseJsonArray.length(); i++) {
            JSONObject userCourse = userCourseJsonArray.getJSONObject(i);
            int courseId = userCourse.getInt(COURSE_ID_TAG);
            String courseName = userCourse.getString(COURSE_NAME_TAG);

            ContentValues values = new ContentValues();
            values.put(DataContract.UserCourseEntry.COLUMN_COURSE_ID, courseId);
            values.put(DataContract.UserCourseEntry.COLUMN_COURSE_NAME, courseName);
            values.put(DataContract.UserCourseEntry.COLUMN_COURSE_UPDATE, 0);

            userCourseContentVector.add(values);
        }

        context.getContentResolver().delete(DataContract.UserCourseEntry.CONTENT_URI, null, null);
        int rowInserted = 0;

        if (userCourseContentVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[userCourseContentVector.size()];
            userCourseContentVector.toArray(cvArray);
            rowInserted = context.getContentResolver().bulkInsert(DataContract.UserCourseEntry.CONTENT_URI, cvArray);
        }

        return rowInserted > 0;
    }

    /**
     * Build URL to retrieve desired course contents
     *
     * @param context  to obtain user web token from Shared Preferences
     * @param courseId to identify specific course content to retrieve
     * @return desired course content query url
     */
    public static URL buildCourseContentQueryUrl(Context context, int courseId) {
        String webToken = PreferenceUtils.getWebToken(context);

        Uri courseContentQueryUri = Uri.parse(GENERAL_QUERY_URL).buildUpon()
                .appendQueryParameter(WEB_SERVICE_FUNCTION, COURSE_CONTENT_WEB_SERVICE_FUNCTION)
                .appendQueryParameter(WEB_SERVICE_TOKEN, webToken)
                .appendQueryParameter(COURSE_ID, String.valueOf(courseId))
                .appendQueryParameter(WEB_SERVICE_REST_FORMAT, WEB_SERVICE_REST_JSON_FORMAT)
                .build();

        try {
            URL courseContentQueryUrl = new URL(courseContentQueryUri.toString());

            return courseContentQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean getCourseContent(String jsonData, int courseId, Context context) throws JSONException {
        //Tags to parse Json
        final String CONTENT_ID_TAG = "id";
        final String CONTENT_TITLE_TAG = "name";
        final String CONTENT_SUMMARY_TAG = "summary";

        final String CONTENT_MODULES_TAG = "modules";
        final String CONTENT_MODULES_ID_TAG = "id";
        final String CONTENT_MODULES_FORUM_ID_TAG = "instance";
        final String CONTENT_MODULES_NAME_TAG = "name";
        final String CONTENT_MODULES_DESCRIPTION_TAG = "description";
        final String CONTENT_MODULES_TYPE_TAG = "modname";

        final String CONTENT_MODULES_CONTENTS_TAG = "contents";
        final String CONTENT_FILE_TYPE_TAG = "type";
        final String CONTENT_FILE_NAME_TAG = "filename";
        final String CONTENT_FILE_SIZE_TAG = "filesize";
        final String CONTENT_FILE_URL_TAG = "fileurl";
        final String CONTENT_FILE_TIME_CREATED_TAG = "timecreated";
        final String CONTENT_FILE_TIME_MODIFIED_TAG = "timemodified";
        final String CONTENT_AUTHOR_TAG = "author";

        JSONArray courseContentArray = new JSONArray(jsonData);
        Vector<ContentValues> courseContentValuesVector = new Vector<>(courseContentArray.length(), courseContentArray.length());
        Vector<ContentValues> contentModuleValuesVector = new Vector<>(courseContentArray.length(), courseContentArray.length());
        Vector<ContentValues> moduleFileValuesVector = new Vector<>(courseContentArray.length(), courseContentArray.length());

        for (int i = 0; i < courseContentArray.length(); i++) {
            JSONObject courseContentObj = courseContentArray.getJSONObject(i);

            ContentValues cc = new ContentValues();

            String contentId = courseContentObj.getString(CONTENT_ID_TAG);
            String title = courseContentObj.getString(CONTENT_TITLE_TAG);
            String summary = courseContentObj.getString(CONTENT_SUMMARY_TAG);

            cc.put(DataContract.CourseContentEntry.COLUMN_COURSE_ID, courseId);
            cc.put(DataContract.CourseContentEntry.COLUMN_CONTENT_ID, contentId);
            cc.put(DataContract.CourseContentEntry.COLUMN_CONTENT_TITLE, title);
            cc.put(DataContract.CourseContentEntry.COLUMN_CONTENT_SUMMARY, summary);


            JSONArray courseModuleArray = courseContentObj.getJSONArray(CONTENT_MODULES_TAG);
            //check if has "modules" section has value
            if (courseModuleArray != null && courseModuleArray.length() > 0) {

                for (int j = 0; j < courseModuleArray.length(); j++) {
                    JSONObject courseModuleObj = courseModuleArray.getJSONObject(j);

                    ContentValues cm = new ContentValues();
                    int moduleId = courseModuleObj.getInt(CONTENT_MODULES_ID_TAG);
                    String moduleName = courseModuleObj.getString(CONTENT_MODULES_NAME_TAG);
                    String moduleType = courseModuleObj.getString(CONTENT_MODULES_TYPE_TAG);

                    if ("forum".equals(moduleType)) {
                        int forumId = courseModuleObj.getInt(CONTENT_MODULES_FORUM_ID_TAG);
                        cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID, forumId);
                    }

                    if (courseModuleObj.has(CONTENT_MODULES_DESCRIPTION_TAG)) {
                        String moduleDescription = courseModuleObj.getString(CONTENT_MODULES_DESCRIPTION_TAG);
                        cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_DESCRIPTION, moduleDescription);
                    } else {
                        cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_DESCRIPTION, "");
                    }

                    cm.put(DataContract.ContentModuleEntry.COLUMN_COURSE_ID, courseId);
                    cm.put(DataContract.ContentModuleEntry.COLUMN_CONTENT_ID, contentId);
                    cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_ID, moduleId);
                    cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_NAME, moduleName);
                    cm.put(DataContract.ContentModuleEntry.COLUMN_MODULE_TYPE, moduleType);


                    //check if has "contents" section
                    if (courseModuleObj.has(CONTENT_MODULES_CONTENTS_TAG)) {
                        JSONArray courseModuleContentArray = courseModuleObj.getJSONArray(CONTENT_MODULES_CONTENTS_TAG);

                        for (int k = 0; k < courseModuleContentArray.length(); k++) {
                            ContentValues mf = new ContentValues();

                            JSONObject courseModuleContentObj = courseModuleContentArray.getJSONObject(k);
                            String contentType = courseModuleContentObj.getString(CONTENT_FILE_TYPE_TAG);
                            String contentFileName = courseModuleContentObj.getString(CONTENT_FILE_NAME_TAG);
                            String contentFileSize = courseModuleContentObj.getString(CONTENT_FILE_SIZE_TAG);
                            String contentFileUrl = courseModuleContentObj.getString(CONTENT_FILE_URL_TAG);
                            String contentFileTimeCreated = courseModuleContentObj.getString(CONTENT_FILE_TIME_CREATED_TAG);
                            String contentFileTimeModified = courseModuleContentObj.getString(CONTENT_FILE_TIME_MODIFIED_TAG);
                            String contentAuthor = courseModuleContentObj.getString(CONTENT_AUTHOR_TAG);

                            mf.put(DataContract.ContentModuleEntry.COLUMN_COURSE_ID, courseId);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_MODULE_ID, moduleId);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_TYPE, contentType);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_NAME, contentFileName);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_SIZE, contentFileSize);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_URL, contentFileUrl);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_TIME_CREATED, contentFileTimeCreated);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_FILE_TIME_MODIFIED, contentFileTimeModified);
                            mf.put(DataContract.ModuleFileEntry.COLUMN_AUTHOR, contentAuthor);

                            moduleFileValuesVector.add(mf);
                        }
                    }

                    contentModuleValuesVector.add(cm);
                }

                //if the content has Module
                courseContentValuesVector.add(cc);
            } else {

                //if the content has no Module
                courseContentValuesVector.add(cc);
            }

        }

        context.getContentResolver().delete(DataContract.CourseContentEntry.CONTENT_URI, " course_id =?", new String[]{String.valueOf(courseId)});
        context.getContentResolver().delete(DataContract.ContentModuleEntry.CONTENT_URI, " course_id =?", new String[]{String.valueOf(courseId)});
        context.getContentResolver().delete(DataContract.ModuleFileEntry.CONTENT_URI, " course_id =?", new String[]{String.valueOf(courseId)});

        int rowInserted = 0;

        if (courseContentValuesVector.size() > 0) {
            ContentValues[] ccArray = new ContentValues[courseContentValuesVector.size()];
            courseContentValuesVector.toArray(ccArray);
            rowInserted += context.getContentResolver().bulkInsert(DataContract.CourseContentEntry.CONTENT_URI, ccArray);
        }

        if (contentModuleValuesVector.size() > 0) {
            ContentValues[] cmArray = new ContentValues[contentModuleValuesVector.size()];
            contentModuleValuesVector.toArray(cmArray);
            rowInserted += context.getContentResolver().bulkInsert(DataContract.ContentModuleEntry.CONTENT_URI, cmArray);
        }

        if (moduleFileValuesVector.size() > 0) {
            ContentValues[] mfArray = new ContentValues[moduleFileValuesVector.size()];
            moduleFileValuesVector.toArray(mfArray);
            rowInserted += context.getContentResolver().bulkInsert(DataContract.ModuleFileEntry.CONTENT_URI, mfArray);
        }

        return rowInserted > 0;
    }

    /**
     * Build file download link by appending user web token
     *
     * @param context to obtain web token from shared preference
     * @param fileUrl fileUrl obtained
     * @return URL to download file
     */
    public static URL buildFileDownloadUrl(Context context, String fileUrl) {
        String webToken = PreferenceUtils.getWebToken(context);

        Uri fileDownloadUri = Uri.parse(fileUrl).buildUpon()
                .appendQueryParameter(FILE_DOWNLOAD_TOKEN, webToken)
                .build();

        try {
            URL fileDownloadUrl = new URL(fileDownloadUri.toString());

            return fileDownloadUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Function to build URL to get forum data
     *
     * @param context To access user's web token
     * @param forumId To identify ID of forum to be retrieved
     * @return URL to query for desired forum data
     */
    public static URL buildForumQueryUri(Context context, int forumId) {
        String webToken = PreferenceUtils.getWebToken(context);

        Uri forumQueryUri = Uri.parse(GENERAL_QUERY_URL).buildUpon()
                .appendQueryParameter(WEB_SERVICE_FUNCTION, FORUM_WEB_SERVICE_FUNCTION)
                .appendQueryParameter(WEB_SERVICE_TOKEN, webToken)
                .appendQueryParameter(FORUM_ID, String.valueOf(forumId))
                .appendQueryParameter(WEB_SERVICE_REST_FORMAT, WEB_SERVICE_REST_JSON_FORMAT)
                .build();

        try {
            URL forumQueryUrl = new URL(forumQueryUri.toString());

            return forumQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse course forum data
     *
     * @param jsonData json string from web services
     * @param forumId  forum ID as key
     * @param context  to access context resolver
     * @return if rows inserted > 0, return true
     * @throws JSONException
     */
    public static boolean getForumData(String jsonData, int forumId, Context context) throws JSONException {
        final String DISCUSSIONS_TAG = "discussions";

        final String POST_ID_TAG = "id";
        final String POST_TITLE_TAG = "name";
        final String TIME_MODIFIED_TAG = "timemodified";
        final String DISCUSSION_ID_TAG = "discussion";
        final String SUBJECT_TAG = "subject";
        final String MESSAGE_TAG = "message";
        final String AUTHOR_TAG = "userfullname";

        final String ATTACHMENTS_TAG = "attachments";
        final String FILE_NAME_TAG = "filename";
        final String FILE_TYPE_TAG = "mimetype";
        final String FILE_URL_TAG = "fileurl";

        JSONObject jsonForumObject = new JSONObject(jsonData);
        JSONArray jsonForumArray = jsonForumObject.getJSONArray(DISCUSSIONS_TAG);

        Vector<ContentValues> forumVector = new Vector<>(jsonForumArray.length());
        Vector<ContentValues> forumAttachmentVector = new Vector<>(jsonForumArray.length());

        for (int i = 0; i < jsonForumArray.length(); i++) {
            JSONObject jsonForumPostObj = jsonForumArray.getJSONObject(i);

            ContentValues forumPost = new ContentValues();

            int postId = jsonForumPostObj.getInt(POST_ID_TAG);
            String postTitle = jsonForumPostObj.getString(POST_TITLE_TAG);
            long timeModified = jsonForumPostObj.getLong(TIME_MODIFIED_TAG);
            int discussionId = jsonForumPostObj.getInt(DISCUSSION_ID_TAG);
            String postSubject = jsonForumPostObj.getString(SUBJECT_TAG);
            String postMessage = jsonForumPostObj.getString(MESSAGE_TAG);
            String postAuthor = jsonForumPostObj.getString(AUTHOR_TAG);

            forumPost.put(DataContract.ForumEntry.COLUMN_FORUM_ID, forumId);
            forumPost.put(DataContract.ForumEntry.COLUMN_POST_ID, postId);
            forumPost.put(DataContract.ForumEntry.COLUMN_POST_TITLE, postTitle);
            forumPost.put(DataContract.ForumEntry.COLUMN_TIME_MODIFIED, timeModified);
            forumPost.put(DataContract.ForumEntry.COLUMN_DISCUSSION_ID, discussionId);
            forumPost.put(DataContract.ForumEntry.COLUMN_DISCUSSION_SUBJECT, postSubject);
            forumPost.put(DataContract.ForumEntry.COLUMN_DISCUSSION_MESSAGE, postMessage);
            forumPost.put(DataContract.ForumEntry.COLUMN_AUTHOR, postAuthor);


            if (jsonForumPostObj.has(ATTACHMENTS_TAG)) {

                JSONArray jsonForumFileArray = jsonForumPostObj.getJSONArray(ATTACHMENTS_TAG);

                for (int j = 0; j < jsonForumFileArray.length(); j++) {
                    JSONObject jsonForumFileObj = jsonForumFileArray.getJSONObject(j);

                    ContentValues forumFile = new ContentValues();

                    String fileName = jsonForumFileObj.getString(FILE_NAME_TAG);
                    String fileType = jsonForumFileObj.getString(FILE_TYPE_TAG);
                    String fileUrl = jsonForumFileObj.getString(FILE_URL_TAG);

                    forumFile.put(DataContract.ForumAttachmentEntry.COLUMN_FORUM_ID, forumId);
                    forumFile.put(DataContract.ForumAttachmentEntry.COLUMN_DISCUSSION_ID, discussionId);
                    forumFile.put(DataContract.ForumAttachmentEntry.COLUMN_FILE_NAME, fileName);
                    forumFile.put(DataContract.ForumAttachmentEntry.COLUMN_FILE_TYPE, fileType);
                    forumFile.put(DataContract.ForumAttachmentEntry.COLUMN_FILE_URL, fileUrl);
                    forumAttachmentVector.add(forumFile);
                }
            }

            forumVector.add(forumPost);
        }

        context.getContentResolver().delete(DataContract.ForumEntry.CONTENT_URI, " forum_id = ?", new String[]{String.valueOf(forumId)});
        context.getContentResolver().delete(DataContract.ForumAttachmentEntry.CONTENT_URI, " forum_id = ?", new String[]{String.valueOf(forumId)});

        int rowInserted = 0;

        if (forumVector.size() > 0) {
            ContentValues[] cv = new ContentValues[forumVector.size()];
            forumVector.toArray(cv);
            rowInserted += context.getContentResolver().bulkInsert(DataContract.ForumEntry.CONTENT_URI, cv);
        }

        if (forumVector.size() > 0) {
            ContentValues[] cv = new ContentValues[forumAttachmentVector.size()];
            forumAttachmentVector.toArray(cv);
            rowInserted += context.getContentResolver().bulkInsert(DataContract.ForumAttachmentEntry.CONTENT_URI, cv);
        }

        return true;
    }


    /**
     * General helper method to download Json string using HTTP
     *
     * @param url URL to obtain desired data
     * @return Json string
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(7000);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:

                    InputStream in = urlConnection.getInputStream();

                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    if (hasInput) {
                        return scanner.next();
                    } else {
                        return null;
                    }


                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * General helper method to download Json string using HTTPS/SSL
     *
     * @param url URL to obtain desired data
     * @return Json string
     * @throws IOException
     */
    public static String getResponseFromHttpsUrl(URL url, Context mContext) throws IOException {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(R.raw.utpedumy);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca = " + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(7000);
            urlConnection.setReadTimeout(7000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:

                    InputStream in = urlConnection.getInputStream();

                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    if (hasInput) {
                        return scanner.next();
                    } else {
                        return null;
                    }


                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Check user's network connection
     * @param context
     * @return
     */
    public static boolean checkNetworkStatus(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
