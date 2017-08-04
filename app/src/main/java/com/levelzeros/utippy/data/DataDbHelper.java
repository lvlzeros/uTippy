package com.levelzeros.utippy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Poon on 16/2/2017.
 */

public class DataDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "data.db";

    private static DataDbHelper mInstance = null;

    private DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataDbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating course table");
        final String SQL_CREATE_COURSE_TABLE = "CREATE TABLE " + DataContract.UserCourseEntry.TABLE_NAME + "("
                + DataContract.UserCourseEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.UserCourseEntry.COLUMN_COURSE_ID + " INTEGER NOT NULL, "
                + DataContract.UserCourseEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL, "
                + DataContract.UserCourseEntry.COLUMN_COURSE_UPDATE + " INTEGER NOT NULL"
                + ");";

        final String SQL_CREATE_COURSE_CONTENT_TABLE = "CREATE TABLE " + DataContract.CourseContentEntry.TABLE_NAME + "("
                + DataContract.CourseContentEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.CourseContentEntry.COLUMN_COURSE_ID + " INTEGER NOT NULL,"
                + DataContract.CourseContentEntry.COLUMN_CONTENT_ID + " INTEGER NOT NULL,"
                + DataContract.CourseContentEntry.COLUMN_CONTENT_TITLE + " TEXT NOT NULL,"
                + DataContract.CourseContentEntry.COLUMN_CONTENT_SUMMARY + " TEXT"
                + ");";

        final String SQL_CREATE_CONTENT_MODULE_TABLE = "CREATE TABLE " + DataContract.ContentModuleEntry.TABLE_NAME + "("
                + DataContract.ContentModuleEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.ContentModuleEntry.COLUMN_COURSE_ID + " INTEGER NOT NULL,"
                + DataContract.ContentModuleEntry.COLUMN_CONTENT_ID + " INTEGER NOT NULL,"
                + DataContract.ContentModuleEntry.COLUMN_MODULE_ID + " INTEGER,"
                + DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID + " INTEGER,"
                + DataContract.ContentModuleEntry.COLUMN_MODULE_NAME + " TEXT,"
                + DataContract.ContentModuleEntry.COLUMN_MODULE_DESCRIPTION + " TEXT,"
                + DataContract.ContentModuleEntry.COLUMN_MODULE_TYPE + " TEXT"
                + ");";

        final String SQL_CREATE_MODULE_FILE_TABLE = "CREATE TABLE " + DataContract.ModuleFileEntry.TABLE_NAME + "("
                + DataContract.ModuleFileEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.ModuleFileEntry.COLUMN_COURSE_ID + " INTEGER NOT NULL,"
                + DataContract.ModuleFileEntry.COLUMN_MODULE_ID + " INTEGER NOT NULL,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_TYPE + " TEXT,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_NAME + " TEXT,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_SIZE + " INTEGER,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_URL + " TEXT,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_TIME_CREATED + " TEXT,"
                + DataContract.ModuleFileEntry.COLUMN_FILE_TIME_MODIFIED + " TEXT,"
                + DataContract.ModuleFileEntry.COLUMN_AUTHOR + " TEXT"
                + ");";

        final String SQL_CREATE_FORUM_TABLE = "CREATE TABLE " + DataContract.ForumEntry.TABLE_NAME + "("
                + DataContract.ForumEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.ForumEntry.COLUMN_FORUM_ID + " INTEGER NOT NULL,"
                + DataContract.ForumEntry.COLUMN_POST_ID + " INTEGER NOT NULL,"
                + DataContract.ForumEntry.COLUMN_POST_TITLE + " TEXT,"
                + DataContract.ForumEntry.COLUMN_TIME_MODIFIED + " TEXT,"
                + DataContract.ForumEntry.COLUMN_DISCUSSION_ID + " INTEGER NOT NULL,"
                + DataContract.ForumEntry.COLUMN_DISCUSSION_SUBJECT + " TEXT,"
                + DataContract.ForumEntry.COLUMN_DISCUSSION_MESSAGE + " TEXT,"
                + DataContract.ForumEntry.COLUMN_AUTHOR + " TEXT"
                + ");";

        final String SQL_CREATE_FORUM_FILE_TABLE = "CREATE TABLE " + DataContract.ForumAttachmentEntry.TABLE_NAME + "("
                + DataContract.ForumAttachmentEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.ForumAttachmentEntry.COLUMN_FORUM_ID + " INTEGER NOT NULL,"
                + DataContract.ForumAttachmentEntry.COLUMN_DISCUSSION_ID + " INTEGER NOT NULL,"
                + DataContract.ForumAttachmentEntry.COLUMN_FILE_NAME + " TEXT,"
                + DataContract.ForumAttachmentEntry.COLUMN_FILE_TYPE + " TEXT,"
                + DataContract.ForumAttachmentEntry.COLUMN_FILE_URL + " TEXT NOT NULL"
                + ");";

        final String SQL_CREATE_TODO_TABLE =  "CREATE TABLE " + DataContract.TodoEntry.TABLE_NAME + "("
                + DataContract.TodoEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.TodoEntry.COLUMN_TODO_NAME + " TEXT NOT NULL,"
                + DataContract.TodoEntry.COLUMN_TODO_DESCRIPTION + " TEXT,"
                + DataContract.TodoEntry.COLUMN_PRIORITY + " INTEGER NOT NULL,"
                + DataContract.TodoEntry.COLUMN_DISPLAY + " INTEGER NOT NULL"
                + ");";

        final String SQL_CREATE_CLASS_TABLE =  "CREATE TABLE " + DataContract.ClassEntry.TABLE_NAME + "("
                + DataContract.ClassEntry._ID + " INTEGER PRIMARY KEY,"
                + DataContract.ClassEntry.COLUMN_CLASS_NAME + " TEXT NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_DESCRIPTION + " TEXT,"
                + DataContract.ClassEntry.COLUMN_CLASS_DAY + " TEXT NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_START_TIME + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_START_HOUR + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_START_MINUTE + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_END_TIME + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_END_HOUR + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_END_MINUTE + " INTEGER NOT NULL,"
                + DataContract.ClassEntry.COLUMN_CLASS_VENUE + " TEXT,"
                + DataContract.ClassEntry.COLUMN_CLASS_REMINDER + " INTEGER"
                + ");";

        db.execSQL(SQL_CREATE_COURSE_TABLE);
        db.execSQL(SQL_CREATE_COURSE_CONTENT_TABLE);
        db.execSQL(SQL_CREATE_CONTENT_MODULE_TABLE);
        db.execSQL(SQL_CREATE_MODULE_FILE_TABLE);
        db.execSQL(SQL_CREATE_FORUM_TABLE);
        db.execSQL(SQL_CREATE_FORUM_FILE_TABLE);
        db.execSQL(SQL_CREATE_TODO_TABLE);
        db.execSQL(SQL_CREATE_CLASS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.UserCourseEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.CourseContentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.ContentModuleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.ModuleFileEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.ForumEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.ForumAttachmentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.TodoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.ClassEntry.TABLE_NAME);
        onCreate(db);
    }
}
