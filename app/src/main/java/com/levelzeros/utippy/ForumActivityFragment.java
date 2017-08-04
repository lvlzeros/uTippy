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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Poon on 24/2/2017.
 */

/**
 * Fragment to display News Forum of selected course
 */
public class ForumActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ForumFileRecyclerViewAdapter.OnForumFileDownloadClick,
        DownloadHelper.OnTaskExecuted {
    private static final String TAG = "ForumFragment";

    private static final int FORUM_LOADER_ID = 300;
    private static final int WRITE_EXT_STORAGE_REQUEST_CODE = 301;
    private static final String FORUM_ID_KEY = "forum_id";
    private static final String FORUM_ID_STATE_KEY = "forum_id_init";
    private static final String FORUM_CONTENT_STATE_KEY = "forum_content_init";

    //Forum ID will be dynamically obtained from database in this fragment
    //Variables to check if Forum ID and Forum Content have been initialized
    private static boolean forumIdInitialized = false;
    private static boolean forumContentInitialized = false;

    private Context mContext;
    private int courseId;
    private String courseName;
    private int forumId;

    private SwipeRefreshLayout mForumSwipeRefreshLayout;
    private RecyclerView mForumRecyclerView;
    private ForumRecyclerViewAdapter mAdapter;
    private Fragment mForumFragment;

    private ForumFileRecyclerViewAdapter.OnForumFileDownloadClick mFileDownloadListener;
    private DownloadHelper.OnTaskExecuted mStatusListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mForumFragment = this;
        mFileDownloadListener = this;

        courseId = ForumActivity.getCourseId();
        courseName = ForumActivity.getCourseName();

        /**
         * In the case of screen rotation, check savedInstanceState to obtain previously initialized value
         * to optimize performance
         */
        if (savedInstanceState != null) {
            forumId = savedInstanceState.getInt(FORUM_ID_KEY);
            forumIdInitialized = savedInstanceState.getBoolean(FORUM_ID_STATE_KEY);
            forumContentInitialized = savedInstanceState.getBoolean(FORUM_CONTENT_STATE_KEY);
        } else {

            //Else just initialize it
            new ForumInitializationTask().execute(courseId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.forum_main_fragment, container, false);
        mForumRecyclerView = (RecyclerView) view.findViewById(R.id.forum_recycler_view_container);
        mForumSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.forum_swipe_refresh_container);

        mForumSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //If Forum ID isn't initialized, Refresh method works on update the Forum ID
                //If Forum ID has been initialized, Refresh method works on update the Forum Content
                if (forumIdInitialized) {
                    updateForumContent();
                } else {
                    updateForumId(courseId);
                }
            }
        });

        mAdapter = new ForumRecyclerViewAdapter(null, mContext, mFileDownloadListener);
        mForumRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mForumRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Update forum ID if it hasn't been initialized
        if (!forumIdInitialized) {
            updateForumId(courseId);
        }

        final ConnectivityManager conMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            OnForumIdInitialized(forumIdInitialized);

        } else {
            //Update forum content if it hasn't been initialized
            if (!forumContentInitialized && forumIdInitialized) {
                updateForumContent();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(FORUM_LOADER_ID, null, this);
    }


    /**
     * In case of screen rotation, the state of Forum ID should be retained
     * to avoid additional call to re-perform same methods
     *
     * @param outState Values are stored inside here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(FORUM_ID_KEY, forumId);
        outState.putBoolean(FORUM_ID_STATE_KEY, forumIdInitialized);
        outState.putBoolean(FORUM_CONTENT_STATE_KEY, forumContentInitialized);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = DataContract.ForumEntry.COLUMN_TIME_MODIFIED + " DESC";
        String selection = DataContract.ForumEntry.COLUMN_FORUM_ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(forumId)};

        switch (id) {
            case FORUM_LOADER_ID:
                return new CursorLoader(mContext,
                        DataContract.ForumEntry.CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        sortOrder);

            default:
                throw new UnsupportedOperationException("Loader ID unidentified: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    /**
     * Initialize Forum Fragment by obtaining Forum ID
     * and check if the forum content has been initialized before
     */
    class ForumInitializationTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            int courseId = params[0];

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT " + DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID
                            + " FROM "
                            + DataContract.ContentModuleEntry.TABLE_NAME + " WHERE "
                            + DataContract.ContentModuleEntry.COLUMN_COURSE_ID
                            + " = ? ",
                    new String[]{String.valueOf(courseId)});

            //If forum ID has been initialized, check if there is any content save previously based on the Forum ID
            cursor.moveToFirst();
            if (cursor.getCount() != 0) {
                forumId = cursor.getInt(cursor.getColumnIndex(DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID));
                forumIdInitialized = true;

                cursor = db.rawQuery("SELECT COUNT(*) FROM "
                                + DataContract.ForumEntry.TABLE_NAME + " WHERE "
                                + DataContract.ForumEntry.COLUMN_FORUM_ID
                                + " = ? ",
                        new String[]{String.valueOf(forumId)});

                cursor.moveToFirst();
                if (cursor.getInt(0) != 0) {
                    forumContentInitialized = true;
                    cursor.close();

                    return true;
                } else {
                    forumContentInitialized = false;
                    cursor.close();

                    return false;
                }

            } else {
                forumId = 0;
                forumIdInitialized = false;
                cursor.close();

                return false;
            }
        }
    }

    /**
     * ASyncTask to update Forum ID
     */
    class getForumIdTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            int courseId = params[0];

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT " + DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID
                            + " FROM "
                            + DataContract.ContentModuleEntry.TABLE_NAME + " WHERE "
                            + DataContract.ContentModuleEntry.COLUMN_COURSE_ID
                            + " = ? ",
                    new String[]{String.valueOf(courseId)});

            cursor.moveToFirst();
            if (cursor.getCount() != 0) {
                forumId = cursor.getInt(cursor.getColumnIndex(DataContract.ContentModuleEntry.COLUMN_MODULE_FORUM_ID));
                forumIdInitialized = true;
                cursor.close();

                return true;
            } else {
                forumId = 0;
                forumIdInitialized = false;
                cursor.close();

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            mForumSwipeRefreshLayout.setRefreshing(false);

            OnForumIdInitialized(status);
        }
    }

    /**
     * Check if forum table has been initialized previously
     */
    class checkForumTableContent extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int forumId = params[0];

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();


            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "
                            + DataContract.ForumEntry.TABLE_NAME + " WHERE "
                            + DataContract.ForumEntry.COLUMN_FORUM_ID
                            + " = ? ",
                    new String[]{String.valueOf(forumId)});

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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mForumSwipeRefreshLayout.setRefreshing(false);

            onLoadForumContent();
        }
    }

    /**
     * Task to download and update latest forum content
     */
    class UpdateForumTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            int forumId;
            if (params != null && params.length > 0) {
                forumId = params[0];
            } else {
                return false;
            }

            URL courseContentQueryUrl = NetworkUtils.buildForumQueryUri(mContext, forumId);

            try {
                String jsonData = NetworkUtils.getResponseFromHttpsUrl(courseContentQueryUrl, mContext);

                return NetworkUtils.getForumData(jsonData, forumId, mContext);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);

            //Check if the current instance fragment is attached, rare occasions it would crash because current fragment is detached
            if (mForumFragment.isAdded()) {
                OnContentUpdated(status);
            } else {
                Toast.makeText(mContext, "Error occured, please retry", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * On the event of successful download and parsing data, load the data to display
     *
     * @param status
     */
    void OnContentUpdated(boolean status) {
        mForumSwipeRefreshLayout.setRefreshing(false);
        if (status) {
            getLoaderManager().restartLoader(FORUM_LOADER_ID, null, this);
        } else {
            if (null != mForumRecyclerView) {

                if (NetworkUtils.checkNetworkStatus(mContext)) {
                    Snackbar.make(mForumRecyclerView, mContext.getString(R.string.session_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_fix), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DialogSessionExpired.newInstance().show(getActivity().getFragmentManager(), DialogSessionExpired.DIALOG_SEESION_EXPIRED_TAG);
                                }
                            }).show();
                } else {
                    Snackbar.make(mForumRecyclerView, mContext.getString(R.string.network_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateForumContent();
                                }
                            }).show();
                }
            } else {
                Toast.makeText(mContext, "Network error, please retry", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Call to update forum content
     */
    void updateForumContent() {
        mForumSwipeRefreshLayout.setRefreshing(true);
        new UpdateForumTask().execute(forumId);
    }

    /**
     * Call to update forum ID
     *
     * @param courseId To locate forum ID corresponding to selected course ID
     */
    void updateForumId(int courseId) {
        mForumSwipeRefreshLayout.setRefreshing(true);
        new getForumIdTask().execute(courseId);
    }

    /**
     * Call to check forum content in database in the event of successful forum ID initialization
     */
    void OnForumIdInitialized(boolean status) {
        mForumSwipeRefreshLayout.setRefreshing(true);
        if (status) {
            new checkForumTableContent().execute(forumId);
        } else {
            mForumSwipeRefreshLayout.setRefreshing(false);
            if (null != mForumRecyclerView) {

                if (NetworkUtils.checkNetworkStatus(mContext)) {
                    Snackbar.make(mForumRecyclerView, mContext.getString(R.string.session_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_fix), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DialogSessionExpired.newInstance().show(getActivity().getFragmentManager(), DialogSessionExpired.DIALOG_SEESION_EXPIRED_TAG);
                                }
                            }).show();
                } else {
                    Snackbar.make(mForumRecyclerView, mContext.getString(R.string.network_error), 10000)
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                            .setAction(mContext.getString(R.string.prompt_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateForumContent();
                                }
                            }).show();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.forum_id_init_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Load forum content from database if data is stored in previous run
     */
    void onLoadForumContent() {
        getLoaderManager().restartLoader(FORUM_LOADER_ID, null, this);
    }

    /**
     * Handle download request of desired attachment
     *
     * @param fileUrl
     */
    @Override
    public void OnForumFileClick(String fileUrl) {
        //Always check if permission before attempting download task
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            URL fileDownloadLink = NetworkUtils.buildFileDownloadUrl(mContext, fileUrl);
            new DownloadHelper(mContext, courseName, mStatusListener).execute(fileDownloadLink);

        } else {
            Snackbar.make(mForumRecyclerView, "Please grant access to Storage", 10000)
                    .setAction("Grant Access", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXT_STORAGE_REQUEST_CODE);
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
     * Handle event when attempt to download attachment failed
     *
     * @param status  Result of the download task
     * @param fileUrl URL to download desired attachment
     */
    public void onRetry(boolean status, final String fileUrl) {
        if (!status) {
            if (null != mForumRecyclerView) {
                Snackbar.make(mForumRecyclerView, "Network Error", 10000)
                        .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                OnForumFileClick(fileUrl);
                            }
                        }).show();
            } else {
                Toast.makeText(mContext, "Network error, please retry", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

/**
 * Adapter to populate forum posts
 */
class ForumRecyclerViewAdapter extends RecyclerView.Adapter<ForumRecyclerViewAdapter.ForumViewHolder> {
    private static final String TAG = "ForumRecyclerViewAdapte";

    private Cursor mCursor;
    private Context mContext;
    private ForumFileRecyclerViewAdapter mForumFileAdapter;
    private ForumFileRecyclerViewAdapter.OnForumFileDownloadClick mListener;

    ForumRecyclerViewAdapter(Cursor mCursor, Context context, ForumFileRecyclerViewAdapter.OnForumFileDownloadClick mListener) {
        this.mCursor = mCursor;
        this.mContext = context;
        this.mListener = mListener;
    }

    class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView mPostTitleTextView;
        TextView mPostAuthorTextView;
        TextView mTimeModifiedTextView;
        TextView mPostMessageTextView;
        RecyclerView mForumFileRecyclerView;
        CardView mForumListCardView;
        CardView mEmptyTooltipCardView;

        ForumViewHolder(View view) {
            super(view);

            mPostTitleTextView = (TextView) view.findViewById(R.id.post_title_text_view);
            mPostAuthorTextView = (TextView) view.findViewById(R.id.post_author_text_view);
            mTimeModifiedTextView = (TextView) view.findViewById(R.id.post_time_text_view);
            mPostMessageTextView = (TextView) view.findViewById(R.id.post_message_text_view);
            mForumFileRecyclerView = (RecyclerView) view.findViewById(R.id.forum_file_recycler_view);
            mForumListCardView = (CardView) view.findViewById(R.id.forum_list_card_view);
            mEmptyTooltipCardView = (CardView) view.findViewById(R.id.empty_tooltip_card_view);
        }
    }

    @Override
    public ForumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forum_list_fragment, parent, false);

        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForumViewHolder holder, int position) {
        if (mCursor == null || mCursor.getCount() == 0) {
            holder.mEmptyTooltipCardView.setVisibility(View.VISIBLE);
            holder.mForumListCardView.setVisibility(View.GONE);
        } else {
            holder.mEmptyTooltipCardView.setVisibility(View.GONE);
            holder.mForumListCardView.setVisibility(View.VISIBLE);

            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }

            String postTitle = mCursor.getString(mCursor.getColumnIndex(DataContract.ForumEntry.COLUMN_POST_TITLE));
            holder.mPostTitleTextView.setText(postTitle);

            String author = mCursor.getString(mCursor.getColumnIndex(DataContract.ForumEntry.COLUMN_AUTHOR));
            holder.mPostAuthorTextView.setVisibility(View.VISIBLE);
            if (author != null || !author.isEmpty()) {
                holder.mPostAuthorTextView.setText(author);
            } else {
                holder.mPostAuthorTextView.setVisibility(View.GONE);
            }

            long timeModified = mCursor.getLong(mCursor.getColumnIndex(DataContract.ForumEntry.COLUMN_TIME_MODIFIED));
            holder.mTimeModifiedTextView.setVisibility(View.VISIBLE);
            if (timeModified != 0) {
                String formattedDate = getDateFormat(timeModified);
                holder.mTimeModifiedTextView.setText(formattedDate);
            } else {
                holder.mTimeModifiedTextView.setVisibility(View.GONE);
            }

            String messageHtml = mCursor.getString(mCursor.getColumnIndex(DataContract.ForumEntry.COLUMN_DISCUSSION_MESSAGE));
            holder.mPostMessageTextView.setVisibility(View.VISIBLE);
            if (messageHtml != null || !messageHtml.isEmpty()) {
                String message;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    message = Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_COMPACT).toString();
                } else {
                    message = Html.fromHtml(messageHtml).toString();
                }
                holder.mPostMessageTextView.setText(message);
            } else {
                holder.mPostMessageTextView.setVisibility(View.GONE);
            }

            int discussionId = mCursor.getInt(mCursor.getColumnIndex(DataContract.ForumEntry.COLUMN_DISCUSSION_ID));
//            new CheckFileTask().execute(discussionId);
//            Cursor forumFileCursor = null;
            Cursor forumFileCursor = checkIfContainAttachment(discussionId);
            if (forumFileCursor != null && forumFileCursor.getCount() > 0) {
                holder.mForumFileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mForumFileAdapter = new ForumFileRecyclerViewAdapter(forumFileCursor, mContext, mListener);
                holder.mForumFileRecyclerView.setAdapter(mForumFileAdapter);
                mForumFileAdapter.notifyDataSetChanged();
            } else {
                holder.mForumFileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mForumFileAdapter = new ForumFileRecyclerViewAdapter(null, mContext, mListener);
                holder.mForumFileRecyclerView.setAdapter(mForumFileAdapter);
                mForumFileAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null || mCursor.getCount() == 0) {
            return 1;
        }
        return mCursor.getCount();
    }

    /**
     * Check if there is any attachment bound to the current forum post
     * ALERT: sometimes doesn't work well
     */
    private class CheckFileTask extends AsyncTask<Integer, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Integer... params) {
            int discussionId = params[0];

            Cursor cursor;

            DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            cursor = db.query(DataContract.ForumAttachmentEntry.TABLE_NAME,
                    null,
                    DataContract.ForumAttachmentEntry.COLUMN_DISCUSSION_ID + " = ?",
                    new String[]{String.valueOf(discussionId)},
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
            super.onPostExecute(cursor);

            updateFileDisplay(cursor);
        }
    }

    /**
     * Check if there is any attachment bound to the current forum post
     * ALERT: Not a good practice, will crash if the data size is huge
     *
     * @param discussionId To locate attachment bounded
     * @return Queried cursor
     */
    Cursor checkIfContainAttachment(int discussionId) {
        Cursor cursor;

//        DataDbHelper dbHelper = DataDbHelper.getInstance(mContext);
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//
//        cursor = db.query(DataContract.ForumAttachmentEntry.TABLE_NAME,
//                null,
//                DataContract.ForumAttachmentEntry.COLUMN_DISCUSSION_ID + " = ?",
//                new String[]{String.valueOf(discussionId)},
//                null,
//                null,
//                null);
//

        cursor = mContext.getContentResolver().query(DataContract.ForumAttachmentEntry.CONTENT_URI,
                null,
                DataContract.ForumAttachmentEntry.COLUMN_DISCUSSION_ID + "=?",
                new String[]{String.valueOf(discussionId)},
                null);

        if (null != cursor && cursor.getCount() > 0) {
            return cursor;
        } else {
            return null;
        }
    }

    /**
     * Notify ForumFileAdapter when attachments has been loaded
     *
     * @param cursor
     */
    private void updateFileDisplay(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            mForumFileAdapter.swapCursor(cursor);
        }
    }

    /**
     * Convert Epoch time to friendlier display of date
     *
     * @param EpochValue Epoch time
     * @return Friendly date
     */
    private String getDateFormat(long EpochValue) {
        String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(EpochValue * 1000));
        return date;
    }

    Cursor swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        return mCursor;
    }

}

/**
 * Adapter to populate attachment bound to individual forum post
 */
class ForumFileRecyclerViewAdapter extends RecyclerView.Adapter<ForumFileRecyclerViewAdapter.ForumFileViewHolder> {
    private static final String TAG = "ForumFileRecyclerViewAd";

    private Cursor mCursor;
    private Context mContext;
    private OnForumFileDownloadClick mListener;
    private Toast fileToast;

    /**
     * Interface to handle download request
     */
    interface OnForumFileDownloadClick {
        void OnForumFileClick(String fileUrl);
    }

    ForumFileRecyclerViewAdapter(Cursor mCursor, Context mContext, OnForumFileDownloadClick mListener) {
        this.mCursor = mCursor;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    class ForumFileViewHolder extends RecyclerView.ViewHolder {
        TextView mFileNameTextView;
        ImageView mFileDownloadIcon;
        ImageView mFileIcon;

        public ForumFileViewHolder(View itemView) {
            super(itemView);

            mFileNameTextView = (TextView) itemView.findViewById(R.id.forum_file_name_text_view);
            mFileDownloadIcon = (ImageView) itemView.findViewById(R.id.forum_file_download_icon);
            mFileIcon = (ImageView) itemView.findViewById(R.id.forum_file_icon);
        }
    }

    @Override
    public ForumFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forum_file_list_fragment, parent, false);

        return new ForumFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForumFileViewHolder holder, final int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }

        String fileName = mCursor.getString(mCursor.getColumnIndex(DataContract.ForumAttachmentEntry.COLUMN_FILE_NAME));
        holder.mFileNameTextView.setText(fileName);

        String fileExt = getFileExtension(fileName);
        holder.mFileIcon.setImageResource(GeneralUtils.getFileIcon(fileExt));

        holder.mFileDownloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor.moveToPosition(position)) {
                    String fileUrl = mCursor.getString(mCursor.getColumnIndex(DataContract.ForumAttachmentEntry.COLUMN_FILE_URL));

                    mListener.OnForumFileClick(fileUrl);
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

    @Override
    public int getItemCount() {
        if (mCursor == null || mCursor.getCount() == 0) {
            return 0;
        }
        return mCursor.getCount();
    }

    Cursor swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        return mCursor;
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

