package com.levelzeros.utippy;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.data.TodoObject;

import static android.content.ContentValues.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class TodoEditorActivityFragment extends Fragment {

    private static final int EDITOR_ADD_NEW_TODO = 1;
    private static final int EDITOR_EDIT_TODO = 2;

    private Context mContext;
    private int currentEditorMode = 0;

    private TodoObject todoArg;
    private long todoId;
    private String todoName;
    private String todoDescription;
    private int mPriority = 3;
    private int mDisplayNum;

    private EditText mTodoNameEditText;
    private EditText mTodoDescriptionEditText;
    private RadioButton mHighPriorRadio;
    private RadioButton mMidPriorRadio;
    private RadioButton mLowPriorRadio;
    private Button mHighPriorBtn;
    private Button mMidPriorBtn;
    private Button mLowPriorBtn;
    private Button mSaveButton;

    private OnTodoSaveListener mTodoListener;

    public TodoEditorActivityFragment() {
    }

    interface OnTodoSaveListener {
        void onSaveClicked();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        Bundle args = getActivity().getIntent().getExtras();

        if (null != args) {
            todoArg = args.getParcelable(TodoObject.class.getSimpleName());
            currentEditorMode = EDITOR_EDIT_TODO;

            todoId = todoArg.getId();
            todoName = todoArg.getTodoName();
            todoDescription = todoArg.getTodoDescription();
            mPriority = todoArg.getPriority();
            mDisplayNum = todoArg.getDisplayNum();

        } else {

            currentEditorMode = EDITOR_ADD_NEW_TODO;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_editor, container, false);
        Log.d(TAG, "onCreateView: ");
        mTodoNameEditText = (EditText) view.findViewById(R.id.todo_name_edit_text);
        mTodoDescriptionEditText = (EditText) view.findViewById(R.id.todo_description_edit_text);
        mHighPriorRadio = (RadioButton) view.findViewById(R.id.high_priority_radio);
        mMidPriorRadio = (RadioButton) view.findViewById(R.id.mid_priority_radio);
        mLowPriorRadio = (RadioButton) view.findViewById(R.id.low_priority_radio);
        mHighPriorBtn = (Button) view.findViewById(R.id.high_priority_button);
        mMidPriorBtn = (Button) view.findViewById(R.id.mid_priority_button);
        mLowPriorBtn = (Button) view.findViewById(R.id.low_priority_button);
        mSaveButton = (Button) view.findViewById(R.id.button_save_todo);

        if (!TextUtils.isEmpty(todoName)) {
            mTodoNameEditText.setText(todoName);
        }

        if (!TextUtils.isEmpty(todoDescription)) {
            mTodoDescriptionEditText.setText(todoDescription);
        }


        switch (mPriority) {
            case 3:
                mHighPriorRadio.setChecked(true);
                break;
            case 2:
                mMidPriorRadio.setChecked(true);
                break;
            case 1:
                mLowPriorRadio.setChecked(true);
                break;
        }

        mHighPriorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHighPriorRadio.setChecked(true);
                mPriority = 3;
            }
        });
        mMidPriorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMidPriorRadio.setChecked(true);
                mPriority = 2;
            }
        });
        mLowPriorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLowPriorRadio.setChecked(true);
                mPriority = 1;
            }
        });

        mHighPriorRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority = 3;
            }
        });
        mMidPriorRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority = 2;
            }
        });
        mLowPriorRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority = 1;
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean validity = true;

                String todoName = mTodoNameEditText.getText().toString();
                if (TextUtils.isEmpty(todoName)) {
                    mTodoNameEditText.requestFocus();
                    mTodoNameEditText.setError(getResources().getString(R.string.error_empty_input));
                    validity = false;
                }

                if (validity) {
                    String todoDescription = mTodoDescriptionEditText.getText().toString();
                    ContentValues cv;
                    switch (currentEditorMode) {
                        case EDITOR_ADD_NEW_TODO:
                            cv = new ContentValues();

                            cv.put(DataContract.TodoEntry.COLUMN_TODO_NAME, todoName);
                            cv.put(DataContract.TodoEntry.COLUMN_TODO_DESCRIPTION, todoDescription);
                            cv.put(DataContract.TodoEntry.COLUMN_PRIORITY, mPriority);
                            cv.put(DataContract.TodoEntry.COLUMN_DISPLAY, 1);

                            mContext.getContentResolver().insert(DataContract.TodoEntry.CONTENT_URI, cv);

                            mTodoListener.onSaveClicked();
                            break;

                        case EDITOR_EDIT_TODO:

                            cv = new ContentValues();

                            cv.put(DataContract.TodoEntry.COLUMN_TODO_NAME, todoName);
                            cv.put(DataContract.TodoEntry.COLUMN_TODO_DESCRIPTION, todoDescription);
                            cv.put(DataContract.TodoEntry.COLUMN_PRIORITY, mPriority);
                            cv.put(DataContract.TodoEntry.COLUMN_DISPLAY, 1);


                            mContext.getContentResolver().update(DataContract.TodoEntry.CONTENT_URI,
                                    cv,
                                    DataContract.TodoEntry._ID + "=?",
                                    new String[]{String.valueOf(todoId)});

                            mTodoListener.onSaveClicked();
                            break;

                        default:
                            throw new UnsupportedOperationException("Unknown Editor Mode: " + currentEditorMode);
                    }
                }
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTodoSaveListener) {
            mTodoListener = (OnTodoSaveListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must be implement OnTodoSaveListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTodoListener = null;
    }
}
