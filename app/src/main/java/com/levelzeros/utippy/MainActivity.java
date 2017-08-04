package com.levelzeros.utippy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements StorageFragment.OnStorageItemClickListener {

    public static final String INTENT_FILE_ACTIVITY_KEY = "course-name";
    public static final String LOGOUT_REQUEST_ACTION = "logout_requested";
    public static final int REQUEST_CODE_WRITE_EXT_STORAGE = 100;

    public static int PREVIOUS_PAGE = 0;

    public Context mContext = this;
    BroadcastReceiver mBroadcastReceiver;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXT_STORAGE);
        }


        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tab);

        mViewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(PREVIOUS_PAGE, true);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(LOGOUT_REQUEST_ACTION)) {
                    Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                    logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    finish();
                }
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter(LOGOUT_REQUEST_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:

                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingIntent);

                return true;

            case R.id.action_about:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStorageItemClick(String courseName) {
        PREVIOUS_PAGE = 1;
        Intent ViewFileIntent = new Intent(MainActivity.this, FileActivity.class);
        ViewFileIntent.putExtra(INTENT_FILE_ACTIVITY_KEY, courseName);
        startActivity(ViewFileIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }
}


class MainViewPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 4;
    private String[] PAGE_TITLES = new String[]{"Courses", "Files", "Class", "Todo"};

    MainViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CourseFragment();
            case 1:
                return new StorageFragment();
            case 2:
                return new ClassFragment();
            case 3:
                return new TodoFragment();
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
            case 1:
            case 2:
            case 3:
                return PAGE_TITLES[position];

            default:
                return super.getPageTitle(position);
        }
    }

}