package com.levelzeros.utippy.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.levelzeros.utippy.data.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Vector;

/**
 * Created by Poon on 11/3/2017.
 */

public class SyncUtils {
    private static final String TAG = "SyncUtils";

    public static boolean checkContentUpdate(Context context, int courseId) throws JSONException {
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

        boolean NEW_CONTENT_FOUND = false;

        URL courseContentQueryUrl = NetworkUtils.buildCourseContentQueryUrl(context, courseId);

        String jsonData = "";
        try {
            jsonData = NetworkUtils.getResponseFromHttpsUrl(courseContentQueryUrl, context);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(jsonData)) {
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


            Cursor courseModuleCursor = context.getContentResolver().query(DataContract.ContentModuleEntry.CONTENT_URI,
                    new String[]{DataContract.ContentModuleEntry._ID},
                    DataContract.ContentModuleEntry.COLUMN_COURSE_ID + "=?",
                    new String[]{"" + courseId},
                    null);
            if (courseModuleCursor == null) {
                if (contentModuleValuesVector.size() > 0) {
                    NEW_CONTENT_FOUND = true;
                }
            } else {
                if (contentModuleValuesVector.size() > courseModuleCursor.getCount()) {
                    NEW_CONTENT_FOUND = true;
                }
            }


            Cursor moduleFileCursor = context.getContentResolver().query(DataContract.ModuleFileEntry.CONTENT_URI,
                    new String[]{DataContract.ModuleFileEntry._ID},
                    DataContract.ModuleFileEntry.COLUMN_COURSE_ID + "=?",
                    new String[]{"" + courseId},
                    null);
            if (moduleFileCursor == null) {
                if (contentModuleValuesVector.size() > 0) {
                    NEW_CONTENT_FOUND = true;
                }
            } else {
                if (moduleFileValuesVector.size() > moduleFileCursor.getCount()) {
                    NEW_CONTENT_FOUND = true;
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

            return NEW_CONTENT_FOUND;

        } else {

            return NEW_CONTENT_FOUND;
        }
    }
}
