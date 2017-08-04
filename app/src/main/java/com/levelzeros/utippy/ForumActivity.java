package com.levelzeros.utippy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity to display course's forum
 */
public class ForumActivity extends AppCompatActivity {
    private static final String TAG = "ForumActivity";

    static int courseId;
    static String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Locate crucial data before instantiating fragment
        courseId = getIntent().getIntExtra(CourseContentActivity.FORUM_COURSE_ID_KEY, 0);
        courseName = getIntent().getStringExtra(CourseContentActivity.FORUM_COURSE_NAME_KEY);

        setContentView(R.layout.activity_forum);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(courseName);

    }

    /**
     * Getter for current course ID
     * @return course ID
     */
    public static int getCourseId() {
        return courseId;
    }

    /**
     * Getter for current course name
     * @return course name
     */
    public static String getCourseName() {
        return courseName;
    }
}
