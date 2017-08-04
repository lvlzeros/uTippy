package com.levelzeros.utippy;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.data.DataDbHelper;
import com.levelzeros.utippy.ui.DialogSessionExpired;
import com.levelzeros.utippy.utility.DownloadHelper;
import com.levelzeros.utippy.utility.GeneralUtils;
import com.levelzeros.utippy.utility.NetworkUtils;

import java.net.URL;

/**
 * Created by Poon on 18/2/2017.
 */

/**
 * Fragment for handle course content activity
 */
public class ContentFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        CourseContentTask.OnUpdateCourseContent,
        ModuleFileAdapter.OnDownloadClickListener,
        CourseContentRecyclerViewAdapter.OnForumSelectedListener,
        DownloadHelper.OnTaskExecuted {

    private static final String TAG = "ContentFragment";

    //Codes and ID for methods
    public static final int COURSE_CONTENT_LOADER_ID = 200;
    private static final int REQUEST_CODE_WRITE_EXT_STORAGE = 300;

    //Variables
    private Context mContext;
    private Fragment mContentFragment;
    private int courseId;
    private String courseName;

    //Views
    private SwipeRefreshLayout mCourseContentSwipeRefreshLayout;
    private RecyclerView mCourseContentRecyclerView;
    private CourseContentRecyclerViewAdapter mCourseContentRVAdapter;

    //Callbacks
    private CourseContentTask.OnUpdateCourseContent mUpdateCourseCallBack;
    private ModuleFileAdapter.OnDownloadClickListener mDownloadCallback;
    private DownloadHelper.OnTaskExecuted mStatusCallback;
    private CourseContentRecyclerViewAdapter.OnForumSelectedListener mForumSelectListener;
    private OnViewForumListener mForumListener;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = this;

    //Interface to handle user's request to view forum
    interface OnViewForumListener {
        void viewForumRequest();
    }

    //Public constructor needed for instantiation
    public ContentFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mUpdateCourseCallBack = this;
        mDownloadCallback = this;
        mStatusCallback = this;
        mForumSelectListener = this;
        mContentFragment = this;

        //Get Bundle saved from CourseContentActivity
        Bundle args = getArguments();
        if (args != null) {
            this.courseId = args.getInt(CourseContentActivity.ARGS_COURSE_ID_KEY);
            this.courseName = args.getString(CourseContentActivity.ARGS_COURSE_NAME_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.course_content_main_fragment, container, false);
        mCourseContentSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.course_content_swipe_refresh_container);
        mCourseContentRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_course_content_container);
        mCourseContentRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mCourseContentSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateContent();
                    }
                });

        mCourseContentRVAdapter = new CourseContentRecyclerViewAdapter(null, mContext, mDownloadCallback, mForumSelectListener);
        mCourseContentRecyclerView.setAdapter(mCourseContentRVAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ConnectivityManager conMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            new CheckTableContentTask().execute(courseId);
        } else {
            updateContent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Check if there's any saved course content from previous run
         */
//        if (!checkTableContent()) {
//            updateContent();
//        }
        getLoaderManager().restartLoader(COURSE_CONTENT_LOADER_ID, null, this);
    }

    /**
     * Check if activity is an instance of OnViewForumListener, to handle user's request to view forum
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnViewForumListener) {
            mForumListener = (OnViewForumListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + "must be an instance of OnViewForumListener");
        }
    }

    /**
     * Reset OnViewForumListener instance
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mForumListener = null;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = DataContract.CourseContentEntry.COLUMN_CONTENT_ID;
        String selection = DataContract.CourseContentEntry.COLUMN_COURSE_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(courseId)};

        switch (id) {
            case COURSE_CONTENT_LOADER_ID:
                return new CursorLoader(mContext,
                        DataContract.CourseContentEntry.CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        sortOrder);

            default:
                throw new UnsupportedOperationException("Unknown Loader ID: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCourseContentRVAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCourseContentRVAdapter.swapCursor(null);
    }

    /**
     * Handle user's request to download file
     *
     * @param fileUrl URL to download desired file
     */
    @Override
    public void OnDownloadClick(String fileUrl) {

        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            URL fileDownloadLink = NetworkUtils.buildFileDownloadUrl(mContext, fileUrl);
            new DownloadHelper(mContext, courseName, mStatusCallback).execute(fileDownloadLink);

        } else {
            Snackbar.make(mCourseContentRecyclerView, getString(R.string.storage_permission_error), 10000)
                    .setAction(getString(R.string.prompt_grant_access), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXT_STORAGE);
                            } else {
                                //If user tapped "Dont ask again", permanently deny prompt to access Storage
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                                    intent.setData(uri);
                                    mContext.startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                    mContext.startActivity(intent);
                                }
                            }
                        }
                    }).show();
        }
    }

    /**
     * Handle user's request to retry download task in the case of getting error in previous run
     *
     * @param status  status of previous run
     * @param fileUrl URL to download desired file
     */
    @Override
    public void onRetry(boolean status, final String fileUrl) {
        if (!status) {
            //Rare occasion it would crash because of null pointer exception
            if (null != mCourseContentRecyclerView) {
                Snackbar.make(mCourseContentRecyclerView, getString(R.string.network_error), 10000)
                        .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                        .setAction(getString(R.string.prompt_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                OnDownloadClick(fileUrl);
                            }
                        }).show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.toast_retry), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Callback to load data when contents are updated,
     * or prompt retry when there is error updating content
     *
     * @param status
     */
    @Override
    public void OnContentUpdated(boolean status) {
        mCourseContentSwipeRefreshLayout.setRefreshing(false);
        if (status) {
            getLoaderManager().restartLoader(COURSE_CONTENT_LOADER_ID, null, this);
        } else {
            if (null != mCourseContentRecyclerView) {

                if (NetworkUtils.checkNetworkStatus(mContext)) {
                    Snackbar.make(mCourseContentRecyclerView, mContext.getString(R.string.session_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_fix), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DialogSessionExpired.newInstance().show(getActivity().getFragmentManager(), DialogSessionExpired.DIALOG_SEESION_EXPIRED_TAG);
                                }
                            }).show();
                } else {
                    Log.d(TAG, "OnContentUpdated: + " + status);
                    Snackbar.make(mCourseContentRecyclerView, mContext.getString(R.string.network_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateContent();
                                }
                            }).show();
                }
            } else {
                Toast.makeText(mContext, getString(R.string.network_error_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Call to update course content
     */
    public void updateContent() {
        mCourseContentSwipeRefreshLayout.setRefreshing(true);
        new CourseContentTask(mContext, mUpdateCourseCallBack, mContentFragment).execute(courseId);
    }

    /**
     * Asynchronous task to check if there's any saved data in previous run
     */
    class CheckTableContentTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "
                            + DataContract.CourseContentEntry.TABLE_NAME + " WHERE "
                            + DataContract.CourseContentEntry.COLUMN_COURSE_ID
                            + " = ? ",
                    new String[]{String.valueOf(courseId)});

            cursor.moveToFirst();
            if (cursor.getInt(0) != 0) {
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (!status) {
                updateContent();
            } else {
                getLoaderManager().restartLoader(COURSE_CONTENT_LOADER_ID, null, mLoaderCallback);
            }
        }
    }

    /**
     * Method to check if there's any saved data in previous run
     * ALERT: Not a good practice to query database in main thread
     *
     * @return
     */
    public boolean checkTableContent() {
        DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "
                        + DataContract.CourseContentEntry.TABLE_NAME + " WHERE "
                        + DataContract.CourseContentEntry.COLUMN_COURSE_ID
                        + " = ? ",
                new String[]{String.valueOf(courseId)});

        cursor.moveToFirst();
        if (cursor.getInt(0) != 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /**
     * Prompt callback to open forum for the current course
     */
    @Override
    public void onForumSelected() {
        mForumListener.viewForumRequest();
    }
}


/**
 * Class to download and parse Course Content
 */
class CourseContentTask extends AsyncTask<Integer, Void, Boolean> {
    private static final String TAG = "CourseContentTask";

    private Context mContext;
    private OnUpdateCourseContent mCallback;
    private Fragment mFragment;

    interface OnUpdateCourseContent {
        void OnContentUpdated(boolean status);
    }

    public CourseContentTask(Context mContext, OnUpdateCourseContent callback, Fragment mFragment) {
        this.mContext = mContext;
        this.mCallback = callback;
        this.mFragment = mFragment;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        int courseId;
        if (params != null && params.length > 0) {
            courseId = params[0];
        } else {
            return false;
        }

        URL courseContentQueryUrl = NetworkUtils.buildCourseContentQueryUrl(mContext, courseId);
        Log.d(TAG, "doInBackground: = " + courseContentQueryUrl);
        try {
            String jsonData = NetworkUtils.getResponseFromHttpsUrl(courseContentQueryUrl, mContext); //TODO
            if (jsonData == null || jsonData.isEmpty()) {
                return false;
            }

            return NetworkUtils.getCourseContent(jsonData, courseId, mContext);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean updateStatus) {
        if (mFragment.isAdded()) {
            mCallback.OnContentUpdated(updateStatus);
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.toast_retry), Toast.LENGTH_SHORT).show();
        }
    }
}


/**
 * Recycler view adapter for course content
 */
class CourseContentRecyclerViewAdapter extends RecyclerView.Adapter<CourseContentRecyclerViewAdapter.CourseContentViewHolder> {
    private static final String TAG = "CourseContentRecyclerVi";

    private static final int VIEW_TYPE_NEWS_FORUM = 0;
    private static final int VIEW_TYPE_COURSE_CONTENT = 1;

    private Cursor mCursor;
    private Context mContext;
    private ContentModuleAdapter mContentModuleAdapter;

    private ModuleFileAdapter.OnDownloadClickListener mDownloadCallback;
    private OnForumSelectedListener mForumCallback;

    interface OnForumSelectedListener {
        void onForumSelected();
    }

    public CourseContentRecyclerViewAdapter(Cursor cursor, Context context, ModuleFileAdapter.OnDownloadClickListener dlCallback, OnForumSelectedListener mForumCallback) {
        this.mCursor = cursor;
        this.mContext = context;
        this.mDownloadCallback = dlCallback;
        this.mForumCallback = mForumCallback;
    }


    public class CourseContentViewHolder extends RecyclerView.ViewHolder {

        TextView mContentTitleTextView;
        TextView mContentSummaryTextView;
        RecyclerView mContentRecyclerView;

        public CourseContentViewHolder(View itemView) {
            super(itemView);

            mContentTitleTextView = (TextView) itemView.findViewById(R.id.course_content_title);
            mContentSummaryTextView = (TextView) itemView.findViewById(R.id.course_content_summary);
            mContentRecyclerView = (RecyclerView) itemView.findViewById(R.id.course_content_recycler_view);
        }
    }

    @Override
    public CourseContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;

        switch (viewType) {
            case VIEW_TYPE_NEWS_FORUM:
                layoutId = R.layout.course_content_forum_fragment;
                break;

            case VIEW_TYPE_COURSE_CONTENT:
                layoutId = R.layout.course_content_list_fragment;
                break;

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        return new CourseContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseContentViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_NEWS_FORUM:

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                            if (!mCursor.moveToPosition(currentPosition)) {
//                                throw new IllegalStateException("Couldn't move cursor to position " + currentPosition);
//                            }
                        mForumCallback.onForumSelected();
                    }
                });

                break;

            case VIEW_TYPE_COURSE_CONTENT:
                if (mCursor == null || mCursor.getCount() == 0) {
                    holder.mContentTitleTextView.setText("");
                } else {
                    if (!mCursor.moveToPosition(position)) {
                        throw new IllegalStateException("Couldn't move cursor to position " + position);
                    }
                    String title = mCursor.getString(mCursor.getColumnIndex(DataContract.CourseContentEntry.COLUMN_CONTENT_TITLE));
                    holder.mContentTitleTextView.setText(title);

                    //Check is summary is empty, because some of them is empty
                    String summaryHtml = mCursor.getString(mCursor.getColumnIndex(DataContract.CourseContentEntry.COLUMN_CONTENT_SUMMARY));
                    holder.mContentSummaryTextView.setVisibility(View.VISIBLE);
                    if (null != summaryHtml && !summaryHtml.isEmpty()) {
                        String summary;
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                            summary = Html.fromHtml(summaryHtml, Html.FROM_HTML_MODE_COMPACT).toString();
                        } else {
                            summary = Html.fromHtml(summaryHtml).toString();
                        }
                        holder.mContentSummaryTextView.setText(summary);
                    } else {
                        holder.mContentSummaryTextView.setVisibility(View.GONE);
                    }

//                    String summaryHtml = mCursor.getString(mCursor.getColumnIndex(DataContract.CourseContentEntry.COLUMN_CONTENT_SUMMARY));
//                    holder.mContentSummaryTextView.setVisibility(View.VISIBLE);
//                    if (null != summaryHtml && !summaryHtml.isEmpty()) {
//                        String summary;
//                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
//                            summary = Html.fromHtml(summaryHtml, Html.FROM_HTML_MODE_COMPACT).toString();
//                            SpannableStringBuilder strBuilder = new SpannableStringBuilder(summary);
//                            URLSpan[] urls = strBuilder.getSpans(0, summary.length(), URLSpan.class);
//                            for(URLSpan span : urls) {
//                                makeLinkClickable(strBuilder, span);
//                            }
//                        } else {
//                            summary = Html.fromHtml(summaryHtml).toString();
//                        }
//                        holder.mContentSummaryTextView.setText(summary);
//                        holder.mContentSummaryTextView.setMovementMethod(LinkMovementMethod.getInstance());
//                    } else {
//                        holder.mContentSummaryTextView.setVisibility(View.GONE);
//                    }

                    int contentId = mCursor.getInt(mCursor.getColumnIndex(DataContract.CourseContentEntry.COLUMN_CONTENT_ID));
                    Cursor moduleCursor = checkIfContainModule(contentId);
                    if (moduleCursor != null && moduleCursor.getCount() > 0) {
                        holder.mContentRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mContentModuleAdapter = new ContentModuleAdapter(moduleCursor, contentId, mContext, mDownloadCallback);
                        holder.mContentRecyclerView.setAdapter(mContentModuleAdapter);
                        mContentModuleAdapter.notifyDataSetChanged();
                    } else {
                        holder.mContentRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mContentModuleAdapter = new ContentModuleAdapter(null, contentId, mContext, mDownloadCallback);
                        holder.mContentRecyclerView.setAdapter(mContentModuleAdapter);
                        mContentModuleAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

//    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
//    {
//        int start = strBuilder.getSpanStart(span);
//        int end = strBuilder.getSpanEnd(span);
//        int flags = strBuilder.getSpanFlags(span);
//        ClickableSpan clickable = new ClickableSpan() {
//            public void onClick(View view) {
//                // Do something with span.getURL() to handle the link click...
//                Toast.makeText(mContext, "OPEN", Toast.LENGTH_SHORT).show();
//            }
//        };
//        strBuilder.setSpan(clickable, start, end, flags);
//        strBuilder.removeSpan(span);
//    }

    @Override
    public int getItemCount() {
        if ((mCursor == null) || (mCursor.getCount() == 0)) {
            return 1;
        } else {
            return mCursor.getCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_NEWS_FORUM;

            default:
                return VIEW_TYPE_COURSE_CONTENT;
        }
    }

    Cursor swapCursor(Cursor newData) {
        if (mCursor == newData) {
            return null;
        }

        Cursor oldCursor = mCursor;
        mCursor = newData;

        if (mCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    /**
     * Asynchronous task to check if there's any module bound to the course content
     * ALERT: But this doesn't work well
     */
    class CheckIfContainModuleTask extends AsyncTask<Integer, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Integer... params) {
            int contentId = params[0];

            Cursor cursor;

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.query(DataContract.ContentModuleEntry.TABLE_NAME,
                    null,
                    " content_id = ?",
                    new String[]{String.valueOf(contentId)},
                    null,
                    null,
                    null);


            if (cursor.getCount() > 0) {
                return cursor;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                mContentModuleAdapter.swapCursor(cursor);
            }
        }
    }

    /**
     * Method check if there's any module bound to the course content
     * ALERT: Not a good practice to query database in main thread
     *
     * @return cursor queried
     */
    private Cursor checkIfContainModule(final int contentId) {

//        DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.query(DataContract.ContentModuleEntry.TABLE_NAME,
//                null,
//                " content_id = ?",
//                new String[]{String.valueOf(contentId)},
//                null,
//                null,
//                null);

        Cursor cursor = mContext.getContentResolver().query(
                DataContract.ContentModuleEntry.CONTENT_URI,
                null,
                DataContract.ContentModuleEntry.COLUMN_CONTENT_ID + "= ?",
                new String[]{String.valueOf(contentId)},
                null);

        if (null != cursor && cursor.getCount() > 0) {
            return cursor;
        } else {
            return null;
        }
    }
}


/**
 * To display individual module file
 */
class ContentModuleAdapter extends RecyclerView.Adapter<ContentModuleAdapter.ContentModuleViewHolder> {
    private static final String TAG = "ContentModuleAdapter";

    private Context mContext;
    private Cursor mCursor;
    private int contentId;
    private ModuleFileAdapter.OnDownloadClickListener mDlCallback;
    private ModuleFileAdapter mModuleFileAdapter;


    public ContentModuleAdapter(Cursor cursor, int contentId, Context context, ModuleFileAdapter.OnDownloadClickListener dlCallback) {
        this.mCursor = cursor;
        this.contentId = contentId;
        this.mContext = context;
        this.mDlCallback = dlCallback;
    }

    public class ContentModuleViewHolder extends RecyclerView.ViewHolder {
        TextView mModuleNameTextView;
        TextView mModuleDescriptionTextView;
        RecyclerView mFileRecyclerView;

        public ContentModuleViewHolder(View itemView) {
            super(itemView);

            mModuleNameTextView = (TextView) itemView.findViewById(R.id.course_module_name);
            mModuleDescriptionTextView = (TextView) itemView.findViewById(R.id.course_module_description);
            mFileRecyclerView = (RecyclerView) itemView.findViewById(R.id.file_recycler_view);
        }
    }


    @Override
    public ContentModuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_content_module_list, parent, false);

        return new ContentModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentModuleViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }
        holder.mModuleNameTextView.setText(mCursor.getString(mCursor.getColumnIndex(DataContract.ContentModuleEntry.COLUMN_MODULE_NAME)));

        String descriptionHtml = mCursor.getString(mCursor.getColumnIndex(DataContract.ContentModuleEntry.COLUMN_MODULE_DESCRIPTION));
        if (null != descriptionHtml && !descriptionHtml.isEmpty()) {
            String description;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                description = Html.fromHtml(descriptionHtml, Html.FROM_HTML_MODE_COMPACT).toString();
            } else {
                description = Html.fromHtml(descriptionHtml).toString();
            }
            holder.mModuleDescriptionTextView.setText(description);
        } else {
            holder.mModuleDescriptionTextView.setVisibility(View.GONE);
        }

        int moduleId = mCursor.getInt(mCursor.getColumnIndex(DataContract.ContentModuleEntry.COLUMN_MODULE_ID));
        Cursor fileCursor = checkIfContainFile(moduleId);
        if (fileCursor != null && fileCursor.getCount() > 0) {
            holder.mFileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mModuleFileAdapter = new ModuleFileAdapter(fileCursor, mContext, mDlCallback);
            holder.mFileRecyclerView.setAdapter(mModuleFileAdapter);
            mModuleFileAdapter.notifyDataSetChanged();
        } else {
            holder.mFileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mModuleFileAdapter = new ModuleFileAdapter(null, mContext, mDlCallback);
            holder.mFileRecyclerView.setAdapter(mModuleFileAdapter);
            mModuleFileAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }


    Cursor swapCursor(Cursor newData) {
        if (mCursor == newData) {
            return null;
        }

        Cursor oldCursor = mCursor;
        mCursor = newData;

        if (mCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    /**
     * Asynchronous task to check if there's any file bound to the content's module
     * ALERT: But this doesn't work well
     */
    class CheckIfContainFileTask extends AsyncTask<Integer, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Integer... params) {
            int moduleId = params[0];

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DataContract.ModuleFileEntry.TABLE_NAME,
                    null,
                    " module_id = ?",
                    new String[]{String.valueOf(moduleId)},
                    null,
                    null,
                    null);

            if (cursor.getCount() > 0) {
                return cursor;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                mModuleFileAdapter.swapCursor(cursor);
            }
        }
    }

    /**
     * Method to check if there's any file bound to the content
     * ALERT: Not a good practice to query database in main thread
     *
     * @return cursor queried
     */
    private Cursor checkIfContainFile(int moduleId) {
//        DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.query(DataContract.ModuleFileEntry.TABLE_NAME,
//                null,
//                " module_id = ?",
//                new String[]{String.valueOf(moduleId)},
//                null,
//                null,
//                null);

        Cursor cursor = mContext.getContentResolver().query(
                DataContract.ModuleFileEntry.CONTENT_URI,
                null,
                DataContract.ModuleFileEntry.COLUMN_MODULE_ID + "= ?",
                new String[]{String.valueOf(moduleId)},
                null);

        if (null != cursor && cursor.getCount() > 0) {
            return cursor;
        } else {
            return null;
        }

    }
}


/**
 * To display individual module file
 */
class ModuleFileAdapter extends RecyclerView.Adapter<ModuleFileAdapter.ModuleFileViewHolder> {
    private static final String TAG = "ModuleContentAdapter";

    private Cursor mCursor;
    private Context mContext;
    private OnDownloadClickListener mDownloadCallback;
    private Toast fileToast;

    interface OnDownloadClickListener {
        void OnDownloadClick(String fileUrl);
    }

    public ModuleFileAdapter(Cursor mCursor, Context context, OnDownloadClickListener mDownloadCallback) {
        this.mCursor = mCursor;
        this.mContext = context;
        this.mDownloadCallback = mDownloadCallback;
    }

    public class ModuleFileViewHolder extends RecyclerView.ViewHolder {
        ImageView mFileIconImageView;
        TextView mFileNameTextView;
        TextView mAuthorTextView;
        ImageView mDownloadIconImageView;

        public ModuleFileViewHolder(View view) {
            super(view);

            mFileIconImageView = (ImageView) view.findViewById(R.id.module_file_icon);
            mFileNameTextView = (TextView) view.findViewById(R.id.module_file_name);
            mAuthorTextView = (TextView) view.findViewById(R.id.module_author);
            mDownloadIconImageView = (ImageView) view.findViewById(R.id.module_download_icon);

        }
    }

    @Override
    public ModuleFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_module_file_list, parent, false);

        return new ModuleFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ModuleFileViewHolder holder, final int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }

        String fileName = mCursor.getString(mCursor.getColumnIndex(DataContract.ModuleFileEntry.COLUMN_FILE_NAME));
        holder.mFileNameTextView.setText(fileName);

        String fileExt = getFileExtension(fileName);
        holder.mFileIconImageView.setImageResource(GeneralUtils.getFileIcon(fileExt));

        String author = mCursor.getString(mCursor.getColumnIndex(DataContract.ModuleFileEntry.COLUMN_AUTHOR));
        if (author.equals("null")) {
            holder.mAuthorTextView.setVisibility(View.GONE);
        } else {
            holder.mAuthorTextView.setText(author);
        }

        holder.mDownloadIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor.moveToPosition(position)) {
                    String fileUrl = mCursor.getString(mCursor.getColumnIndex(DataContract.ModuleFileEntry.COLUMN_FILE_URL));

                    mDownloadCallback.OnDownloadClick(fileUrl);
                } else {
                    if (fileToast != null) {
                        fileToast.cancel();
                    }

                    fileToast = Toast.makeText(mContext, "No download link found", Toast.LENGTH_SHORT);
                    fileToast.show();
                }
            }
        });
    }
//    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    Cursor swapCursor(Cursor newData) {
        if (mCursor == newData) {
            return null;
        }

        Cursor oldCursor = mCursor;
        mCursor = newData;

        if (mCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            return ext.toLowerCase();
        }
    }
}
