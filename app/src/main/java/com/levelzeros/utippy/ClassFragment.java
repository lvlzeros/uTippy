package com.levelzeros.utippy;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.levelzeros.utippy.data.ClassObject;
import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.utility.PreferenceUtils;
import com.levelzeros.utippy.utility.ReminderReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Poon on 1/3/2017.
 */

public class ClassFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ClassFragment";

    private static final int CLASS_LOADER_ID = 400;

    private Context mContext;
    private Calendar calendar = Calendar.getInstance();
    private Date currentTime = calendar.getTime();

    private FloatingActionButton mClassEditorFab;
    private RecyclerView mClassRecyclerView;
    private ClassListAdapter mClassAdapter;

    private final int dayOfWeekToday = calendar.get(Calendar.DAY_OF_WEEK);
    public static int UPCOMING_CLASS_POSITION = 0;
    public static long UPCOMING_CLASS_ID = 0;
    public static boolean FOUND_UPCOMING_CLASS = false;
    public static boolean SET_UPCOMING_REMINDER = false;
    private List<ClassObject> mClassList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.class_main_fragment, container, false);

        mClassRecyclerView = (RecyclerView) view.findViewById(R.id.class_recycler_view_container);
        mClassRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mClassAdapter = new ClassListAdapter(mClassList);
        mClassRecyclerView.setAdapter(mClassAdapter);

        mClassEditorFab = (FloatingActionButton) view.findViewById(R.id.class_editor_fab);
        mClassEditorFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestClassEditor(null);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SET_UPCOMING_REMINDER = false;
        FOUND_UPCOMING_CLASS = false;
        currentTime = calendar.getTime();

//        mClassRecyclerView.getLayoutManager().smoothScrollToPosition(mClassRecyclerView, null, UPCOMING_CLASS_POSITION);
        getLoaderManager().restartLoader(CLASS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM + "," + DataContract.ClassEntry.COLUMN_CLASS_START_TIME;

        return new CursorLoader(mContext,
                DataContract.ClassEntry.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null != mClassList) {
            mClassList.clear();
        }

        mClassList = obtainClass(data);
        mClassAdapter.swapList(mClassList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mClassAdapter.swapList(null);
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

    void updatePosition() {
        if (UPCOMING_CLASS_POSITION >= 0) {
            mClassRecyclerView.getLayoutManager().smoothScrollToPosition(mClassRecyclerView, null, UPCOMING_CLASS_POSITION);
        }
    }


    class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ClassListViewHolder> {

        private List<ClassObject> classList;


        public ClassListAdapter(List<ClassObject> classList) {
            this.classList = classList;
        }

        class ClassListViewHolder extends RecyclerView.ViewHolder {
            TextView mClassNameTextView;
            TextView mClassDescriptionTextView;
            TextView mClassDayTextView;
            TextView mStartTimeTextView;
            TextView mEndTimeTextView;
            TextView mClassVenueTextView;
            ImageView mIndicatorImageView;
            LinearLayout mClassListContainer;
            LinearLayout mEmptyTooltipContainer;

            public ClassListViewHolder(View itemView) {
                super(itemView);

                mClassNameTextView = (TextView) itemView.findViewById(R.id.class_name_text_view);
                mClassDescriptionTextView = (TextView) itemView.findViewById(R.id.class_description_text_view);
                mClassDayTextView = (TextView) itemView.findViewById(R.id.class_day_text_view);
                mStartTimeTextView = (TextView) itemView.findViewById(R.id.start_time_text_view);
                mEndTimeTextView = (TextView) itemView.findViewById(R.id.end_time_text_view);
                mClassVenueTextView = (TextView) itemView.findViewById(R.id.class_venue_text_view);
                mIndicatorImageView = (ImageView) itemView.findViewById(R.id.class_indicator_image_view);
                mClassListContainer = (LinearLayout) itemView.findViewById(R.id.class_list_container);
                mEmptyTooltipContainer = (LinearLayout) itemView.findViewById(R.id.empty_tooltip_container);
            }
        }

        @Override
        public ClassListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_list_item, parent, false);

            return new ClassListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ClassListViewHolder holder, int position) {
            if (classList == null || classList.size() == 0) {
                holder.mClassListContainer.setVisibility(View.GONE);
                holder.mEmptyTooltipContainer.setVisibility(View.VISIBLE);
            } else {
                holder.mClassListContainer.setVisibility(View.VISIBLE);
                holder.mEmptyTooltipContainer.setVisibility(View.GONE);


                final ClassObject classObj = classList.get(position);

                holder.mIndicatorImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.classDefaultColor));

                long currentTimeInMillis = currentTime.getTime();
                if (!SET_UPCOMING_REMINDER) {

                    /**
                     * Check if the class's day of week is after or before the day of week today
                     */
                    if (classObj.getClassDayNum() >= dayOfWeekToday) {

                        /**
                         * Check if the class's start time whether it is over or upcoming
                         */
                        if (!FOUND_UPCOMING_CLASS) {
                            if (classObj.getClassStartTime() > currentTimeInMillis) {
                                UPCOMING_CLASS_POSITION = position;
                                UPCOMING_CLASS_ID = classObj.getId();
                                FOUND_UPCOMING_CLASS = true;

                                updatePosition();
                            }
                        }

                        if (classObj.getClassStartTime() - PreferenceUtils.getUserPreferredReminderTime(mContext) * 60 * 1000 - 30000 > currentTimeInMillis) {
                            SET_UPCOMING_REMINDER = true;

                            ReminderReceiver.setAlarm(mContext, classObj, classList);
                        }

                    } else {

                        if (classObj.getClassStartTime() < currentTimeInMillis) {
                            long newStartTime = classObj.getClassStartTime() + AlarmManager.INTERVAL_DAY * 7;
                            long newEndTime = classObj.getClassEndTime() + AlarmManager.INTERVAL_DAY * 7;

                            ContentValues cv = new ContentValues();
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_TIME, newStartTime);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_TIME, newEndTime);

                            getContext().getContentResolver().update(DataContract.ClassEntry.CONTENT_URI,
                                    cv,
                                    DataContract.ClassEntry._ID + "=?",
                                    new String[]{"" + classObj.getId()});

                        }
                    }
                }

                if (FOUND_UPCOMING_CLASS && classObj.getId() == UPCOMING_CLASS_ID) {
                    holder.mIndicatorImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.classUpcomingColor));
                }

                holder.mClassNameTextView.setText(classObj.getClassName());

                holder.mClassDescriptionTextView.setVisibility(View.VISIBLE);
                String description = classObj.getClassDescription();
                if (!TextUtils.isEmpty(description)) {
                    holder.mClassDescriptionTextView.setVisibility(View.VISIBLE);
                    holder.mClassDescriptionTextView.setText(description);
                } else {
                    holder.mClassDescriptionTextView.setVisibility(View.GONE);
                }

                holder.mClassDayTextView.setText(classObj.getClassDay());
                holder.mStartTimeTextView.setText(formatTime(classObj.getClassStartTime()));
                holder.mEndTimeTextView.setText(formatTime(classObj.getClassEndTime()));

                holder.mClassVenueTextView.setVisibility(View.VISIBLE);
                String venue = classObj.getClassVenue();
                if (!TextUtils.isEmpty(description)) {
                    holder.mClassVenueTextView.setVisibility(View.VISIBLE);
                    holder.mClassVenueTextView.setText(venue);
                } else {
                    holder.mClassVenueTextView.setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestClassEditor(classObj);
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            if (classList == null || classList.size() == 0) {
                return 1;
            } else {
                return classList.size();
            }
        }

        String formatTime(long timeInMillis) {
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
            return formatter.format(timeInMillis);
        }

        void swapList(List<ClassObject> classObjectList) {
            if (null != classList && classList.size() > 0) {
                classList.clear();
            }
            classList = classObjectList;
            notifyDataSetChanged();
        }

    }

    void requestClassEditor(ClassObject classObj) {
        if (classObj == null) {
            Intent classEditorIntent = new Intent(getActivity(), ClassEditorActivity.class);
            startActivity(classEditorIntent);
        } else {
            Intent classEditorIntent = new Intent(getActivity(), ClassEditorActivity.class);
            classEditorIntent.putExtra(ClassObject.class.getSimpleName(), classObj);
            startActivity(classEditorIntent);
        }
    }

}
