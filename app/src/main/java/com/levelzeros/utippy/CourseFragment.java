package com.levelzeros.utippy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.ui.DialogSessionExpired;
import com.levelzeros.utippy.utility.NetworkUtils;
import com.levelzeros.utippy.utility.PreferenceUtils;

import java.net.URL;

/**
 * Created by Poon on 16/2/2017.
 */

/**
 * Fragment to display user's enrolled course
 */
public class CourseFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CourseFragment";

    //Constantw
    public static final int USER_COURSE_LOADER_ID = 100;
    public static final String INTENT_COURSE_ID_KEY = "COURSE ID INTENT";
    public static final String INTENT_COURSE_NAME_KEY = "COURSE NAME INTENT";

    //Variables
    private Context mContext;
    private GetUserCourseTask mUserCourseTask;
    private Fragment mCourseFragment = this;

    //Views
    private RecyclerView userCourseRecyclerView;
    private UserCourseRecyclerViewAdapter userCourseAdapter;
    private SwipeRefreshLayout userCourseSwipeRefreshLayout;
    private CardView mEmptyTooltipCardView;

    /**
     * Public constructor is needed for instantiation
     */
    public CourseFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.course_main_fragment, container, false);
        userCourseSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.course_swipe_refresh_container);
        userCourseRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_course_container);
        userCourseRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        userCourseSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateUserCourse();
                    }
                });

        userCourseAdapter = new UserCourseRecyclerViewAdapter(null);
        userCourseRecyclerView.setAdapter(userCourseAdapter);
        mEmptyTooltipCardView = (CardView) view.findViewById(R.id.empty_tooltip_card_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * Check if user has previously save data about his/her enrolled course,
         * else initialize it
         */
        if (!PreferenceUtils.checkCourseInitializationStatus(mContext)) {
            userCourseSwipeRefreshLayout.setRefreshing(true);
            mUserCourseTask = new GetUserCourseTask(mContext);
            mUserCourseTask.execute();
        } else {
            final ConnectivityManager conMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                //Update user's enrolled course
                mUserCourseTask = new GetUserCourseTask(mContext);
                mUserCourseTask.execute();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(USER_COURSE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {DataContract.UserCourseEntry.COLUMN_COURSE_ID, DataContract.UserCourseEntry.COLUMN_COURSE_NAME};
        String sortOrder = DataContract.UserCourseEntry.COLUMN_COURSE_NAME;

        switch (id) {
            case USER_COURSE_LOADER_ID:
                return new CursorLoader(mContext,
                        DataContract.UserCourseEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown Loader ID: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        userCourseAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        userCourseAdapter.swapCursor(null);
    }

    /**
     * Update user's enrolled courses
     */
    public void updateUserCourse() {
        mUserCourseTask = new GetUserCourseTask(mContext);
        mUserCourseTask.execute();
    }


    /**
     * Callback when course is selected, open up CourseContentActivity
     *
     * @param c_id   course ID
     * @param c_name course name
     */
    public void onCourseClick(int c_id, String c_name) {

        Intent intent = new Intent(getActivity(), CourseContentActivity.class);
        intent.putExtra(INTENT_COURSE_ID_KEY, c_id);
        intent.putExtra(INTENT_COURSE_NAME_KEY, c_name);
        startActivity(intent);
    }

    /**
     * Prompt loader to reload data if data is successfully downloaded
     */
    public void userCourseInitialized() {
        userCourseSwipeRefreshLayout.setRefreshing(false);
        getLoaderManager().restartLoader(USER_COURSE_LOADER_ID, null, this);
    }

    /**
     * Prompt user to retry if data failed to be downloaded
     */
    public void userCourseFailedInit() {
        userCourseSwipeRefreshLayout.setRefreshing(false);

        if (NetworkUtils.checkNetworkStatus(mContext)) {
            Snackbar.make(userCourseRecyclerView, mContext.getString(R.string.session_error), 10000)
                    .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    .setAction(mContext.getString(R.string.prompt_fix), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogSessionExpired.newInstance().show(getActivity().getFragmentManager(), DialogSessionExpired.DIALOG_SEESION_EXPIRED_TAG);
                        }
                    }).show();
        } else {
            Snackbar.make(userCourseRecyclerView, mContext.getString(R.string.network_error), 10000)
                    .setActionTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    .setAction(mContext.getString(R.string.prompt_retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mUserCourseTask = new GetUserCourseTask(mContext);
                            mUserCourseTask.execute();
                        }
                    }).show();
        }
    }


    /**
     * Asynchronous task to download and update user's enrolled courses data
     */
    class GetUserCourseTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "GetUserCourseTask";

        private Context mContext;

        public GetUserCourseTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL userCourseQueryUrl = NetworkUtils.buildUserCourseQueryUrl(mContext);
            try {
                String jsonUserCourseResponse = NetworkUtils.getResponseFromHttpsUrl(userCourseQueryUrl, mContext);

                return NetworkUtils.getUserCourse(jsonUserCourseResponse, mContext);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean updateStatus) {
            //Update user's course initialization state upon success download,
            //else prompt retry
            if (mCourseFragment.isAdded()) {
                if (updateStatus != null && updateStatus) {
                    PreferenceUtils.updateCourseInitializationStatus(mContext, updateStatus);
                    userCourseInitialized();
                } else {
                    userCourseFailedInit();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.toast_retry), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Adapter to populate user's enrolled courses
     */
    class UserCourseRecyclerViewAdapter extends RecyclerView.Adapter<UserCourseRecyclerViewAdapter.UserCourseViewHolder> {
        private static final String TAG = "UserCourseRecyclerView";

        private Cursor mCursor;


        public UserCourseRecyclerViewAdapter(Cursor cursor) {
            mCursor = cursor;
        }

        public class UserCourseViewHolder extends RecyclerView.ViewHolder {
            TextView courseNameTextView;
            CardView mCourseCardView;

            public UserCourseViewHolder(View itemView) {
                super(itemView);

                courseNameTextView = (TextView) itemView.findViewById(R.id.course_name);
                mCourseCardView = (CardView) itemView.findViewById(R.id.course_card_view);

            }
        }

        @Override
        public UserCourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_list_fragment, parent, false);
            return new UserCourseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserCourseViewHolder holder, int position) {
            if ((mCursor == null) || (mCursor.getCount() == 0)) {
                mEmptyTooltipCardView.setVisibility(View.VISIBLE);
                userCourseRecyclerView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyTooltipCardView.setVisibility(View.GONE);
                userCourseRecyclerView.setVisibility(View.VISIBLE);
                if (!mCursor.moveToPosition(position)) {
                    throw new IllegalStateException("Couldn't move cursor to position " + position);
                }
                String courseName = mCursor.getString(mCursor.getColumnIndex(DataContract.UserCourseEntry.COLUMN_COURSE_NAME));
                holder.courseNameTextView.setText(courseName);

                final int currentPosition = position;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Prompt callback to open up CourseContentActivity for selected course
                     * @param v
                     */
                    @Override
                    public void onClick(View v) {
                        if (!mCursor.moveToPosition(currentPosition)) {
                            throw new IllegalStateException("Couldn't move cursor to position " + currentPosition);
                        }
                        String courseName = mCursor.getString(mCursor.getColumnIndex(DataContract.UserCourseEntry.COLUMN_COURSE_NAME));
                        int courseId = mCursor.getInt(mCursor.getColumnIndex(DataContract.UserCourseEntry.COLUMN_COURSE_ID));

                        onCourseClick(courseId, courseName);
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            if ((mCursor == null) || (mCursor.getCount() == 0)) {
                return 1;
            } else {
                return mCursor.getCount();
            }
        }


        Cursor swapCursor(Cursor newCursor) {
            if (mCursor == newCursor) {
                return null;
            }

            final Cursor oldCursor = mCursor;
            mCursor = newCursor;
            if (mCursor != null) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeRemoved(0, getItemCount());
            }
            return oldCursor;
        }
    }

}

