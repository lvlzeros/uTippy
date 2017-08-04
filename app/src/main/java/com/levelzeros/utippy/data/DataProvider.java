package com.levelzeros.utippy.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Poon on 16/2/2017.
 */

public class DataProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DataDbHelper mOpenHelper;

    static final int COURSE = 100;
    static final int COURSE_CONTENT = 200;
    static final int CONTENT_MODULE = 201;
    static final int MODULE_FILE = 202;
    static final int FORUM = 300;
    static final int FORUM_FILE = 301;
    static final int CLASS = 400;
    static final int CLASS_ID = 401;
    static final int TODO = 500;
    static final int TODO_ID = 501;


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DataContract.PATH_USER_COURSE, COURSE);
        matcher.addURI(authority, DataContract.PATH_COURSE_CONTENT, COURSE_CONTENT);
        matcher.addURI(authority, DataContract.PATH_COURSE_MODULE, CONTENT_MODULE);
        matcher.addURI(authority, DataContract.PATH_COURSE_FILE, MODULE_FILE);
        matcher.addURI(authority, DataContract.PATH_FORUM, FORUM);
        matcher.addURI(authority, DataContract.PATH_FORUM_ATTACHMENT, FORUM_FILE);
        matcher.addURI(authority, DataContract.PATH_CLASS, CLASS);
        matcher.addURI(authority, DataContract.PATH_CLASS + "/#", CLASS_ID);
        matcher.addURI(authority, DataContract.PATH_TODO, TODO);
        matcher.addURI(authority, DataContract.PATH_TODO + "/#", TODO_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = DataDbHelper.getInstance(getContext().getApplicationContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case COURSE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.UserCourseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case COURSE_CONTENT:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.CourseContentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case CONTENT_MODULE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ContentModuleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case MODULE_FILE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ModuleFileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case FORUM:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ForumEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case FORUM_FILE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ForumAttachmentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case CLASS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ClassEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case CLASS_ID:
                long classId = DataContract.ClassEntry.getClassId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ClassEntry.TABLE_NAME,
                        projection,
                        DataContract.ClassEntry._ID + " =?",
                        new String[]{String.valueOf(classId)},
                        null,
                        null,
                        sortOrder
                );
                break;

            case TODO:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.TodoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case TODO_ID:
                long todoId = DataContract.TodoEntry.getTodoId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.TodoEntry.TABLE_NAME,
                        projection,
                        DataContract.TodoEntry._ID + " =?",
                        new String[]{String.valueOf(todoId)},
                        null,
                        null,
                        sortOrder
                );
                break;


            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case COURSE:
                return DataContract.UserCourseEntry.CONTENT_TYPE;

            case COURSE_CONTENT:
                return DataContract.CourseContentEntry.CONTENT_TYPE;

            case CONTENT_MODULE:
                return DataContract.ContentModuleEntry.CONTENT_TYPE;

            case MODULE_FILE:
                return DataContract.ModuleFileEntry.CONTENT_TYPE;

            case FORUM:
                return DataContract.ForumEntry.CONTENT_TYPE;

            case FORUM_FILE:
                return DataContract.ForumAttachmentEntry.CONTENT_TYPE;

            case CLASS:
                return DataContract.ClassEntry.CONTENT_TYPE;

            case CLASS_ID:
                return DataContract.ClassEntry.CONTENT_ITEM_TYPE;

            case TODO:
                return DataContract.TodoEntry.CONTENT_TYPE;

            case TODO_ID:
                return DataContract.TodoEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long recordId;
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case COURSE:
                recordId = db.insert(DataContract.UserCourseEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.UserCourseEntry.buildCourseUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case COURSE_CONTENT:
                recordId = db.insert(DataContract.CourseContentEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.CourseContentEntry.buildCourseUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case CONTENT_MODULE:
                recordId = db.insert(DataContract.ContentModuleEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.ContentModuleEntry.buildCourseUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case MODULE_FILE:
                recordId = db.insert(DataContract.ModuleFileEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.ModuleFileEntry.buildCourseUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case FORUM:
                recordId = db.insert(DataContract.ForumEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.ForumEntry.buildForumUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case FORUM_FILE:
                recordId = db.insert(DataContract.ForumAttachmentEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.ForumAttachmentEntry.buildForumAttachmentUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case CLASS:
                recordId = db.insert(DataContract.ClassEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.ClassEntry.buildTodoUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            case TODO:
                recordId = db.insert(DataContract.TodoEntry.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = DataContract.TodoEntry.buildTodoUri(recordId);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case COURSE:
                rowsDeleted = db.delete(
                        DataContract.UserCourseEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case COURSE_CONTENT:
                rowsDeleted = db.delete(
                        DataContract.CourseContentEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CONTENT_MODULE:
                rowsDeleted = db.delete(
                        DataContract.ContentModuleEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MODULE_FILE:
                rowsDeleted = db.delete(
                        DataContract.ModuleFileEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FORUM:
                rowsDeleted = db.delete(
                        DataContract.ForumEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FORUM_FILE:
                rowsDeleted = db.delete(
                        DataContract.ForumAttachmentEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CLASS:
                rowsDeleted = db.delete(
                        DataContract.ClassEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CLASS_ID:
                long classId = DataContract.ClassEntry.getClassId(uri);
                rowsDeleted = db.delete(
                        DataContract.ClassEntry.TABLE_NAME,
                        DataContract.ClassEntry._ID + " =?",
                        new String[]{String.valueOf(classId)});
                break;

            case TODO:
                rowsDeleted = db.delete(
                        DataContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TODO_ID:
                long todoId = DataContract.TodoEntry.getTodoId(uri);
                rowsDeleted = db.delete(
                        DataContract.TodoEntry.TABLE_NAME,
                        DataContract.TodoEntry._ID + " =?",
                        new String[]{String.valueOf(todoId)});
                break;


            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case COURSE:
                rowsUpdated = db.update(DataContract.UserCourseEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case COURSE_CONTENT:
                rowsUpdated = db.update(DataContract.CourseContentEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case CONTENT_MODULE:
                rowsUpdated = db.update(DataContract.ContentModuleEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case MODULE_FILE:
                rowsUpdated = db.update(DataContract.ModuleFileEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case FORUM:
                rowsUpdated = db.update(DataContract.ForumEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case FORUM_FILE:
                rowsUpdated = db.update(DataContract.ForumAttachmentEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case CLASS:
                rowsUpdated = db.update(DataContract.ClassEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case CLASS_ID:
                long classId = DataContract.ClassEntry.getClassId(uri);
                rowsUpdated = db.update(DataContract.ClassEntry.TABLE_NAME,
                        values,
                        DataContract.ClassEntry._ID + " =?",
                        new String[]{String.valueOf(classId)});

            case TODO:
                rowsUpdated = db.update(DataContract.TodoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case TODO_ID:
                long todoId = DataContract.TodoEntry.getTodoId(uri);
                rowsUpdated = db.update(DataContract.TodoEntry.TABLE_NAME,
                        values,
                        DataContract.TodoEntry._ID + " =?",
                        new String[]{String.valueOf(todoId)});
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Insert all in one shot!
     *
     * @param uri    Table URI
     * @param values Array of ContentValues
     * @return number of rows inserted
     */
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        switch (sUriMatcher.match(uri)) {
            case COURSE:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long c_id = db.insert(DataContract.UserCourseEntry.TABLE_NAME, null, cv);
                        if (c_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case COURSE_CONTENT:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long cc_id = db.insert(DataContract.CourseContentEntry.TABLE_NAME, null, cv);
                        if (cc_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case CONTENT_MODULE:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long cm_id = db.insert(DataContract.ContentModuleEntry.TABLE_NAME, null, cv);
                        if (cm_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MODULE_FILE:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long mf_id = db.insert(DataContract.ModuleFileEntry.TABLE_NAME, null, cv);
                        if (mf_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case FORUM:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long f_id = db.insert(DataContract.ForumEntry.TABLE_NAME, null, cv);
                        if (f_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case FORUM_FILE:
                db.beginTransaction();

                try {
                    for (ContentValues cv : values) {
                        long fa_id = db.insert(DataContract.ForumAttachmentEntry.TABLE_NAME, null, cv);
                        if (fa_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
