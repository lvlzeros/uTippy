package com.levelzeros.utippy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import static com.levelzeros.utippy.CourseContentActivity.ARGS_COURSE_ID_KEY;
import static com.levelzeros.utippy.CourseContentActivity.ARGS_COURSE_NAME_KEY;
import static com.levelzeros.utippy.CourseContentActivity.COURSE_ID;
import static com.levelzeros.utippy.CourseContentActivity.COURSE_NAME;

public class CourseContentActivity extends AppCompatActivity implements
ContentFragment.OnViewForumListener{
    private static final String TAG = "CourseContentActivity";

    public static final String ARGS_COURSE_ID_KEY = "ARGS_COURSE_ID";
    public static final String ARGS_COURSE_NAME_KEY = "ARGS_COURSE_NAME";
    public static final String FORUM_COURSE_ID_KEY = "FORUM_COURSE_ID";
    public static final String FORUM_COURSE_NAME_KEY = "FORUM_COURSE_NAME";

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    static int COURSE_ID;
    static String COURSE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get crucial data and set it to the activity's variable
        COURSE_ID = getIntent().getIntExtra(CourseFragment.INTENT_COURSE_ID_KEY, 0);
        COURSE_NAME = getIntent().getStringExtra(CourseFragment.INTENT_COURSE_NAME_KEY);

        getSupportActionBar().setTitle(COURSE_NAME);

        //Ensure it's going back to the right page where this activity is initialized
        MainActivity.PREVIOUS_PAGE = 0;

        mViewPager = (ViewPager) findViewById(R.id.cc_viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.cc_sliding_tab);

        mViewPager.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Open the current course's forum as per user request
     */
    @Override
    public void viewForumRequest() {
        Intent forumIntent = new Intent(CourseContentActivity.this, ForumActivity.class);
        forumIntent.putExtra(FORUM_COURSE_ID_KEY, COURSE_ID);
        forumIntent.putExtra(FORUM_COURSE_NAME_KEY, COURSE_NAME);
        startActivity(forumIntent);
    }
}


class ContentViewPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "ContentViewPagerAdapter";

    final int PAGE_COUNT = 1;
    private String[] PAGE_TITLES = new String[]{"Contents"};
    private Bundle data;


    ContentViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ContentFragment contentFragment = new ContentFragment();

                //Set data before instantiating fragment
                data = new Bundle();
                data.putInt(ARGS_COURSE_ID_KEY, COURSE_ID);
                data.putString(ARGS_COURSE_NAME_KEY, COURSE_NAME);
                contentFragment.setArguments(data);
                return contentFragment;

        }
        return null;
    }


    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return PAGE_TITLES[position];

            default:
                return super.getPageTitle(position);
        }
    }

}

