package com.levelzeros.utippy.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Poon on 15/2/2017.
 */

public class DataContract {
    public static final String CONTENT_AUTHORITY = "com.levelzeros.utippy.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    /**
     * Table to store user's enrolled course
     */
    public static final String PATH_USER_COURSE = "course";

    public static final class UserCourseEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER_COURSE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_COURSE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_COURSE;

        public static final String TABLE_NAME = "course";

        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_NAME = "course_name";
        public static final String COLUMN_COURSE_UPDATE = "course_update";
//        public static final String COLUMN_COURSE_PARTICIPANTS = "course_participant_count";

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /**
     * Table to store course's content
     */
    static final String PATH_COURSE_CONTENT = "content";

    public static final class CourseContentEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE_CONTENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_CONTENT;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_CONTENT;

        public static final String TABLE_NAME = "course_content";

        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_CONTENT_ID = "content_id";
        public static final String COLUMN_CONTENT_TITLE = "title";
        public static final String COLUMN_CONTENT_SUMMARY = "summary";

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /**
     * Table to store course's module
     */
    static final String PATH_COURSE_MODULE = "module";

    public static final class ContentModuleEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE_MODULE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_MODULE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_MODULE;

        public static final String TABLE_NAME = "content_module";

        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_CONTENT_ID = "content_id";
        public static final String COLUMN_MODULE_ID = "module_id";
        public static final String COLUMN_MODULE_FORUM_ID = "forum_id";
        public static final String COLUMN_MODULE_NAME = "module_name";
        public static final String COLUMN_MODULE_DESCRIPTION = "module_description";
        public static final String COLUMN_MODULE_TYPE = "module_type";

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Table to store files attached to course's module
     */
    static final String PATH_COURSE_FILE = "file";

    public static final class ModuleFileEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COURSE_FILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_FILE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_FILE;

        public static final String TABLE_NAME = "module_file";

        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_MODULE_ID = "module_id";
        public static final String COLUMN_FILE_TYPE = "file_type";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String COLUMN_FILE_SIZE = "file_size";
        public static final String COLUMN_FILE_URL = "file_url";
        public static final String COLUMN_FILE_TIME_CREATED = "time_created";
        public static final String COLUMN_FILE_TIME_MODIFIED = "time_modified";
        public static final String COLUMN_AUTHOR = "author";

        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /**
     * Table to store course's forum data
     */
    static final String PATH_FORUM = "forum";

    public static final class ForumEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FORUM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_FILE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE_FILE;

        public static final String TABLE_NAME = "forum";

        public static final String COLUMN_FORUM_ID = "forum_id";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_POST_TITLE = "post_title";
        public static final String COLUMN_TIME_MODIFIED = "time_modified";
        public static final String COLUMN_DISCUSSION_ID = "discussion_id";
        public static final String COLUMN_DISCUSSION_SUBJECT = "subject";
        public static final String COLUMN_DISCUSSION_MESSAGE = "message";
        public static final String COLUMN_AUTHOR = "author";

        public static Uri buildForumUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /**
     * Table to store attachments bound to course's forum
     */
    static final String PATH_FORUM_ATTACHMENT = "forum_file";

    public static final class ForumAttachmentEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FORUM_ATTACHMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FORUM_ATTACHMENT;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FORUM_ATTACHMENT;

        public static final String TABLE_NAME = "forum_file";

        public static final String COLUMN_FORUM_ID = "forum_id";
        public static final String COLUMN_DISCUSSION_ID = "discussion_id";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String COLUMN_FILE_TYPE = "file_type";
        public static final String COLUMN_FILE_URL = "file_url";

        public static Uri buildForumAttachmentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Table to store to-do list entry
     */
    static final String PATH_TODO = "todo";

    public static final class TodoEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TODO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TODO;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TODO;

        public static final String TABLE_NAME = "todo";

        public static final String COLUMN_TODO_NAME = "todo_name";
        public static final String COLUMN_TODO_DESCRIPTION = "todo_description";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_DISPLAY = "display";

        public static Uri buildTodoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getTodoId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }


    /**
     * Table to store class entry
     */
    static final String PATH_CLASS = "class";

    public static final class ClassEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLASS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLASS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLASS;

        public static final String TABLE_NAME = "class";

        public static final String COLUMN_CLASS_NAME = "class_name";
        public static final String COLUMN_CLASS_DESCRIPTION = "class_description";
        public static final String COLUMN_CLASS_DAY = "day";
        public static final String COLUMN_CLASS_DAY_NUM = "day_num";
        public static final String COLUMN_CLASS_START_TIME = "start_time";
        public static final String COLUMN_CLASS_START_HOUR = "start_hour";
        public static final String COLUMN_CLASS_START_MINUTE = "start_minute";
        public static final String COLUMN_CLASS_END_TIME = "end_time";
        public static final String COLUMN_CLASS_END_HOUR = "end_hour";
        public static final String COLUMN_CLASS_END_MINUTE = "end_minute";
        public static final String COLUMN_CLASS_VENUE = "venue";
        public static final String COLUMN_CLASS_REMINDER = "reminder";

        public static Uri buildTodoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getClassId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }
}
