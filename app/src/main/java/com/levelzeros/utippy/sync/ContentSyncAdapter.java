package com.levelzeros.utippy.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.utility.PreferenceUtils;
import com.levelzeros.utippy.utility.SyncUtils;

import org.json.JSONException;

/**
 * Created by Poon on 11/3/2017.
 */

public class ContentSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "ContentSyncAdapter";

    public static final int SYNC_INTERVAL = 4 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 4;
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    public ContentSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (PreferenceUtils.checkInitializatonStatus(getContext())) {

            String[] courseQueryProjection = new String[]{DataContract.UserCourseEntry.COLUMN_COURSE_ID, DataContract.UserCourseEntry.COLUMN_COURSE_NAME};
            Cursor courseCursor = getContext().getContentResolver().query(
                    DataContract.UserCourseEntry.CONTENT_URI,
                    courseQueryProjection,
                    null,
                    null,
                    null);

            if (null != courseCursor && courseCursor.getCount() > 0) {
                Integer[] mCourseIdArray = getCourseIdArray(courseCursor);

                for (int i = 0; i < mCourseIdArray.length; i++) {

                    try {
                        if (SyncUtils.checkContentUpdate(getContext(), mCourseIdArray[i])){
                            //TODO add indicator for course
                            ContentValues cv = new ContentValues();
                            cv.put(DataContract.UserCourseEntry.COLUMN_COURSE_UPDATE, 1 );
                            getContext().getContentResolver().update(
                                    DataContract.UserCourseEntry.CONTENT_URI,
                                    cv,
                                    DataContract.UserCourseEntry.COLUMN_COURSE_ID + "=?",
                                    new String[]{"" + mCourseIdArray[i]});
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private Integer[] getCourseIdArray(Cursor cursor) {
        Integer[] courseIdArray = new Integer[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (!cursor.moveToPosition(i)) {
                throw new IllegalStateException("Cant't move cursor to position " + i);
            }

            courseIdArray[i] = cursor.getInt(cursor.getColumnIndex(DataContract.UserCourseEntry.COLUMN_COURSE_ID));
        }

        return courseIdArray;
    }
}
