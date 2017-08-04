package com.levelzeros.utippy;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.levelzeros.utippy.data.ClassObject;
import com.levelzeros.utippy.data.DataContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class editor input fragment
 */
public class ClassEditorActivityFragment extends Fragment {
    private static final String TAG = "ClassEditorActivityFrag";

    private static final int MODE_ADD_CLASS = 1;
    private static final int MODE_EDIT_CLASS = 2;

    private static final int START_TIME_IDENTIFIER = 1;
    private static final int END_TIME_IDENTIFIER = 2;

    private Context mContext;
    private Toast mToast;
    private int currentEditMode = 0;

    private EditText mClassNameEditText;
    private EditText mClassDescriptionEditText;
    private Spinner mClassDaySpinner;
    private EditText mClassStartTimeEditText;
    private EditText mClassEndTimeEditText;
    private EditText mClassVenueEditText;
    private Button mSaveButton;
    private TextView mDeleteTextView;

    private OnClassSaveListener mListener;

    private ClassObject classArg;
    private long classId;
    private String className;
    private String classDescription;
    private String classDay;
    private int classDayNum;
    private long startTime = 0;
    private int classStartHour = 0;
    private int classStartMinute = 0;
    private long endTime = 0;
    private int classEndHour = 0;
    private int classEndMinute = 0;
    private String classVenue;


    interface OnClassSaveListener {
        void onSaveClicked();
        void onDeleteClicked();
    }

    public ClassEditorActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnClassSaveListener) {
            mListener = (OnClassSaveListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnClassSaveListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        Bundle args = getActivity().getIntent().getExtras();
        if (args == null) {
            currentEditMode = MODE_ADD_CLASS;
        } else {
            currentEditMode = MODE_EDIT_CLASS;
            classArg = args.getParcelable(ClassObject.class.getSimpleName());

            classId = classArg.getId();
            className = classArg.getClassName();
            classDescription = classArg.getClassDescription();
            classDay = classArg.getClassDay();
            classDayNum = classArg.getClassDayNum();
            startTime = classArg.getClassStartTime();
            classStartHour = classArg.getClassStartHour();
            classStartMinute = classArg.getClassStartMinute();
            endTime = classArg.getClassEndTime();
            classEndHour = classArg.getClassEndHour();
            classEndMinute = classArg.getClassEndMinute();
            classVenue = classArg.getClassVenue();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_editor, container, false);

        mClassNameEditText = (EditText) view.findViewById(R.id.class_name_edit_text);
        mClassDescriptionEditText = (EditText) view.findViewById(R.id.class_decsription_edit_text);
        mClassDaySpinner = (Spinner) view.findViewById(R.id.class_day_spinner);
        mClassStartTimeEditText = (EditText) view.findViewById(R.id.class_start_time_edit_text);
        mClassEndTimeEditText = (EditText) view.findViewById(R.id.class_end_time_edit_text);
        mClassVenueEditText = (EditText) view.findViewById(R.id.class_venue_edit_text);
        mSaveButton = (Button) view.findViewById(R.id.class_save_button);
        mDeleteTextView = (TextView) view.findViewById(R.id.class_delete_button);

        if (!TextUtils.isEmpty(className)) {
            mClassNameEditText.setText(className);
        }

        if (!TextUtils.isEmpty(classDescription)) {
            mClassDescriptionEditText.setText(classDescription);
        }

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                mContext, R.array.dayOfWeek, android.R.layout.simple_spinner_dropdown_item);
        mClassDaySpinner.setAdapter(spinnerAdapter);
        if (!TextUtils.isEmpty(classDay)) {
            int dayPosition = spinnerAdapter.getPosition(classDay);
            mClassDaySpinner.setSelection(dayPosition);
        }
        mClassDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String day = (String) parent.getItemAtPosition(position);
                if (null != parent.getChildAt(0)) {
                    ((TextView) parent.getChildAt(0)).setTextSize(25);
                }

                classDay = day;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        if (startTime != 0) {
            mClassStartTimeEditText.setText(formatTime(startTime));
        }
        mClassStartTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(START_TIME_IDENTIFIER);
            }
        });

        if (endTime != 0) {
            mClassEndTimeEditText.setText(formatTime(endTime));
        }
        mClassEndTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(END_TIME_IDENTIFIER);
            }
        });

        if (!TextUtils.isEmpty(classVenue)) {
            mClassVenueEditText.setText(classVenue);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                Date date;
                long classStartTime;
                long classEndTime;

                if (checkValidity()) {
                    className = mClassNameEditText.getText().toString();
                    classDescription = mClassDescriptionEditText.getText().toString();
                    classDayNum = getDayNum(classDay);
                    classVenue = mClassVenueEditText.getText().toString();

                    ContentValues cv = new ContentValues();
                    switch (currentEditMode) {
                        case MODE_ADD_CLASS:

                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_NAME, className);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DESCRIPTION, classDescription);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DAY, classDay);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM, classDayNum);


                            calendar.set(Calendar.DAY_OF_WEEK, classDayNum);
                            calendar.set(Calendar.HOUR_OF_DAY, classStartHour);
                            calendar.set(Calendar.MINUTE, classStartMinute);
                            calendar.set(Calendar.SECOND, 0);
                            date = calendar.getTime();
                            classStartTime = date.getTime();
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_TIME, classStartTime);

                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_HOUR, classStartHour);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_MINUTE, classStartMinute);

                            calendar.set(Calendar.DAY_OF_WEEK, classDayNum);
                            calendar.set(Calendar.HOUR_OF_DAY, classEndHour);
                            calendar.set(Calendar.MINUTE, classEndMinute);
                            calendar.set(Calendar.SECOND, 0);
                            date = calendar.getTime();
                            classEndTime = date.getTime();
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_TIME, classEndTime);

                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_HOUR, classEndHour);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_MINUTE, classEndMinute);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_VENUE, classVenue);


                            getContext().getContentResolver().insert(DataContract.ClassEntry.CONTENT_URI, cv);

                            mListener.onSaveClicked();
                            break;

                        case MODE_EDIT_CLASS:
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_NAME, className);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DESCRIPTION, classDescription);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DAY, classDay);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_DAY_NUM, classDayNum);

                            calendar.set(Calendar.DAY_OF_WEEK, classDayNum);
                            calendar.set(Calendar.HOUR_OF_DAY, classStartHour);
                            calendar.set(Calendar.MINUTE, classStartMinute);
                            calendar.set(Calendar.SECOND, 0);
                            date = calendar.getTime();
                            classStartTime = date.getTime();
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_TIME, classStartTime);

                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_HOUR, classStartHour);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_START_MINUTE, classStartMinute);

                            calendar.set(Calendar.DAY_OF_WEEK, classDayNum);
                            calendar.set(Calendar.HOUR_OF_DAY, classEndHour);
                            calendar.set(Calendar.MINUTE, classEndMinute);
                            calendar.set(Calendar.SECOND, 0);
                            date = calendar.getTime();
                            classEndTime = date.getTime();
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_TIME, classEndTime);

                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_HOUR, classEndHour);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_END_MINUTE, classEndMinute);
                            cv.put(DataContract.ClassEntry.COLUMN_CLASS_VENUE, classVenue);

                            getContext().getContentResolver().update(DataContract.ClassEntry.CONTENT_URI,
                                    cv,
                                    DataContract.ClassEntry._ID + "=?",
                                    new String[]{"" + classId});

                            mListener.onSaveClicked();
                            break;

                        default:
                            throw new UnsupportedOperationException("Unknown Editor Mode: " + currentEditMode);
                    }
                }
            }
        });

        mDeleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentEditMode) {
                    case MODE_ADD_CLASS:
                        if (TextUtils.isEmpty(mClassNameEditText.getText().toString())
                                || TextUtils.isEmpty(mClassStartTimeEditText.getText().toString())
                                || TextUtils.isEmpty(mClassEndTimeEditText.getText().toString())) {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(mContext.getString(R.string.title_prompt_class_save))
                                    .setPositiveButton(mContext.getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            mListener.onDeleteClicked();

                                        }
                                    })
                                    .setNegativeButton(mContext.getString(R.string.option_cancel), null);
                            builder.show();
                        } else {
                            mListener.onDeleteClicked();
                        }
                        break;

                    case MODE_EDIT_CLASS:
                        Log.d(TAG, "onClick: ");
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(mContext.getString(R.string.title_prompt_class_delete))
                                .setPositiveButton(mContext.getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        getContext().getContentResolver().delete(DataContract.ClassEntry.CONTENT_URI,
                                                DataContract.ClassEntry._ID + "=?",
                                                new String[]{"" + classId});

                                        mListener.onDeleteClicked();

                                    }
                                })
                                .setNegativeButton(mContext.getString(R.string.option_cancel), null);
                        builder.show();
                }
            }
        });

        return view;
    }


    void showTimePickerDialog(final int identifier) {

        TimePickerDialog timePicker;

        switch (identifier) {
            case START_TIME_IDENTIFIER:

                timePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String timeStr;

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        long timeInMillis = cal.getTimeInMillis();


                        if (endTime != 0 && timeInMillis >= endTime) {
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            if (startTime == endTime) {
                                mToast = Toast.makeText(mContext, getString(R.string.error_time_input_same), Toast.LENGTH_SHORT);
                                mToast.show();

                            } else {

                                mToast = Toast.makeText(mContext, getString(R.string.error_time_input_backwards), Toast.LENGTH_SHORT);
                                mToast.show();
                            }

                        } else {
                            classStartHour = hourOfDay;
                            classStartMinute = minute;

                            startTime = timeInMillis;
                            timeStr = formatTime(timeInMillis);
                            mClassStartTimeEditText.setText(timeStr);

                        }
                    }
                }, classStartHour, classStartMinute, false);
                timePicker.show();
                break;


            case END_TIME_IDENTIFIER:

                timePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String timeStr;

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        long timeInMillis = cal.getTimeInMillis();

                        if (startTime != 0 && startTime >= timeInMillis) {
                            if (mToast != null) {
                                mToast.cancel();
                            }

                            if (startTime == endTime) {
                                mToast = Toast.makeText(mContext, getString(R.string.error_time_input_same), Toast.LENGTH_SHORT);
                                mToast.show();

                            } else {

                                mToast = Toast.makeText(mContext, getString(R.string.error_time_input_backwards), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        } else {
                            classEndHour = hourOfDay;
                            classEndMinute = minute;

                            endTime = timeInMillis;
                            timeStr = formatTime(timeInMillis);
                            mClassEndTimeEditText.setText(timeStr);
                        }

                    }

                }, classEndHour, classEndMinute, false);
                timePicker.show();
                break;
        }
    }

    boolean checkValidity() {
        className = mClassNameEditText.getText().toString();
        if (TextUtils.isEmpty(className)) {
            mClassNameEditText.setError(getString(R.string.error_empty_input));
            mClassNameEditText.requestFocus();
            return false;
        }

        if (startTime == 0 || endTime == 0) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mContext, getString(R.string.error_time_input_empty), Toast.LENGTH_SHORT);
            mToast.show();

            return false;
        }

        return true;
    }

    String formatTime(long timeInMillis) {
        DateFormat formatter = new SimpleDateFormat("hh:mm a");

        return formatter.format(timeInMillis);
    }

    int getDayNum(String day) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEEE");
            Date date = formatter.parse(day);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return calendar.get(Calendar.DAY_OF_WEEK);

        } catch (ParseException e) {
            return 0;
        }
    }
}
