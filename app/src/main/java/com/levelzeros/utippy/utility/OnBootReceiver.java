package com.levelzeros.utippy.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.levelzeros.utippy.data.ClassObject;
import com.levelzeros.utippy.data.DataContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poon on 11/3/2017.
 */

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String sortOrder = DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM + "," + DataContract.ClassEntry.COLUMN_CLASS_START_TIME;
        Cursor cursor = context.getContentResolver().query(DataContract.ClassEntry.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);


        if (null != cursor && cursor.getCount() > 0) {
            List<ClassObject> mClassList = obtainClass(cursor);

            ReminderReceiver.setupNextAlarm(context, mClassList);
        }
    }

    private List<ClassObject> obtainClass(final Cursor mCursor) {

        if (null != mCursor && mCursor.getCount() > 0) {

            List<ClassObject> classList = new ArrayList<>();

            mCursor.moveToFirst();
            for (int i = 0; i < mCursor.getCount(); i++) {
                if (!mCursor.moveToPosition(i)) {
                    throw new IllegalStateException("Can't move cursor to position " + i);
                }

                int classId = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry._ID));
                String className = mCursor.getString(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_NAME));
                String classDescription = mCursor.getString(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_DESCRIPTION));
                String classDay = mCursor.getString(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_DAY));
                int classDayNum = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM));
                long startTime = mCursor.getLong(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_START_TIME));
                int classStartHour = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_START_HOUR));
                int classStartMin = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_START_MINUTE));
                long endTime = mCursor.getLong(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_END_TIME));
                int classEndHour = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_END_HOUR));
                int classEndMin = mCursor.getInt(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_END_MINUTE));
                String classVenue = mCursor.getString(mCursor.getColumnIndex(DataContract.ClassEntry.COLUMN_CLASS_VENUE));


                classList.add(
                        new ClassObject(classId, className, classDescription,
                                classDay, classDayNum, startTime,
                                classStartHour, classStartMin, endTime,
                                classEndHour, classEndMin, classVenue));
            }

            return classList;
        }

        return null;
    }
}
