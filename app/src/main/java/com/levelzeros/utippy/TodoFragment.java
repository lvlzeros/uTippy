package com.levelzeros.utippy;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.levelzeros.utippy.data.DataContract;
import com.levelzeros.utippy.data.TodoObject;

/**
 * Created by Poon on 26/2/2017.
 */

public class TodoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "TodoFragment";

    private static final int TODO_LOADER_ID = 500;

    private Context mContext;

    private RecyclerView mTodoRecyclerView;
    private FloatingActionButton mTodoFab;
    private TodoRecyclerViewAdapter mTodoAdapter;
    private CardView mEmptyTooltipCardView;


    public TodoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.todo_main_fragment, container, false);

        mTodoRecyclerView = (RecyclerView) view.findViewById(R.id.todo_recycler_view_container);
        mTodoRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mTodoAdapter = new TodoRecyclerViewAdapter(null);
        mTodoRecyclerView.setAdapter(mTodoAdapter);
        mEmptyTooltipCardView = (CardView) view.findViewById(R.id.empty_tooltip_card_view);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Object object = viewHolder.itemView.getTag();
                if (object == null) {
                    Toast.makeText(mContext, getString(R.string.toast_empty_view), Toast.LENGTH_SHORT).show();
                } else {
                    long id = (long) viewHolder.itemView.getTag();
                    Cursor mCursor = getContext().getContentResolver().query(DataContract.TodoEntry.CONTENT_URI,
                            new String[]{DataContract.TodoEntry.COLUMN_DISPLAY},
                            DataContract.TodoEntry._ID + "=?",
                            new String[]{"" + id},
                            null);

                    if (null != mCursor && mCursor.getCount() > 0) {
                        mCursor.moveToFirst();
                        switch (mCursor.getInt(mCursor.getColumnIndex(DataContract.TodoEntry.COLUMN_DISPLAY))) {

                            case 1:
                                ContentValues cv = new ContentValues();
                                cv.put(DataContract.TodoEntry.COLUMN_DISPLAY, 0);
                                getContext().getContentResolver().update(DataContract.TodoEntry.CONTENT_URI,
                                        cv,
                                        DataContract.TodoEntry._ID + "=?",
                                        new String[]{"" + id});
                                break;

                            case 0:
                                Uri delUri = DataContract.TodoEntry.buildTodoUri(id);
                                getContext().getContentResolver().delete(delUri, null, null);
                                break;
                        }
                    }
                }
                getLoaderManager().restartLoader(TODO_LOADER_ID, null, TodoFragment.this);

            }
        }).attachToRecyclerView(mTodoRecyclerView);

        mTodoFab = (FloatingActionButton) view.findViewById(R.id.todo_fab);
        mTodoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorRequest(null);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(TODO_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = DataContract.TodoEntry.COLUMN_DISPLAY + " DESC,"
                + DataContract.TodoEntry.COLUMN_PRIORITY + " DESC, "
                + DataContract.TodoEntry.COLUMN_TODO_NAME + " ASC";

        switch (id) {
            case TODO_LOADER_ID:
                return new CursorLoader(mContext,
                        DataContract.TodoEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        sortOrder);

            default:
                throw new UnsupportedOperationException("Unknown Loader ID: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTodoAdapter.swapCursor(data);
    }

    void editorRequest(TodoObject todo) {
        if (todo == null) {
            Intent addNewTodoIntent = new Intent(getActivity(), TodoEditorActivity.class);
            startActivity(addNewTodoIntent);
        } else {
            Intent editTodoIntent = new Intent(getActivity(), TodoEditorActivity.class);
            editTodoIntent.putExtra(TodoObject.class.getSimpleName(), todo);
            startActivity(editTodoIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTodoAdapter.swapCursor(null);
    }

    class TodoRecyclerViewAdapter extends RecyclerView.Adapter<TodoRecyclerViewAdapter.TodoViewHolder> {
        private final int HIGH_PRIORITY = 3;
        private final int MID_PRIORITY = 2;
        private final int LOW_PRIORITY = 1;
        private final int NULL_PRIORITY = 0;

        private final int UNDONE_DISPLAY = 1;
        private final int DONE_DISPLAY = 0;

        private Cursor mCursor;

        TodoRecyclerViewAdapter(Cursor cursor) {
            this.mCursor = cursor;
        }

        class TodoViewHolder extends RecyclerView.ViewHolder {

            TextView mTodoNameTextView;
            TextView mTodoDescriptionTextView;
            CardView mTodoCardView;
            ImageView mPriorityImageView;

            public TodoViewHolder(View itemView) {
                super(itemView);

                mTodoNameTextView = (TextView) itemView.findViewById(R.id.todo_name_text_view);
                mTodoDescriptionTextView = (TextView) itemView.findViewById(R.id.todo_description_text_view);
                mTodoCardView = (CardView) itemView.findViewById(R.id.todo_card_view);
                mPriorityImageView = (ImageView) itemView.findViewById(R.id.todo_priority_image_view);
            }
        }

        @Override
        public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_list_fragment, parent, false);
            return new TodoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TodoViewHolder holder, final int position) {
            if (mCursor == null || mCursor.getCount() == 0) {
                mTodoRecyclerView.setVisibility(View.INVISIBLE);
                mEmptyTooltipCardView.setVisibility(View.VISIBLE);
            } else {
                mTodoRecyclerView.setVisibility(View.VISIBLE);
                mEmptyTooltipCardView.setVisibility(View.GONE);

                if (!mCursor.moveToPosition(position)) {
                    throw new IllegalStateException("Can't move cursor to position " + position);
                }

                final TodoObject todoObj = new TodoObject(
                        mCursor.getLong(mCursor.getColumnIndex(DataContract.TodoEntry._ID)),
                        mCursor.getString(mCursor.getColumnIndex(DataContract.TodoEntry.COLUMN_TODO_NAME)),
                        mCursor.getString(mCursor.getColumnIndex(DataContract.TodoEntry.COLUMN_TODO_DESCRIPTION)),
                        mCursor.getInt(mCursor.getColumnIndex(DataContract.TodoEntry.COLUMN_PRIORITY)),
                        mCursor.getInt(mCursor.getColumnIndex(DataContract.TodoEntry.COLUMN_DISPLAY))
                );

                holder.itemView.setTag(todoObj.getId());

                holder.mTodoNameTextView.setPaintFlags(0);
                switch (todoObj.getDisplayNum()) {
                    case UNDONE_DISPLAY:
                        holder.mTodoNameTextView.setText(todoObj.getTodoName());
                        break;

                    case DONE_DISPLAY:
                        holder.mTodoNameTextView.setText(todoObj.getTodoName());
                        holder.mTodoNameTextView.setPaintFlags(holder.mTodoNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        break;
                }


                switch (todoObj.getDisplayNum()) {
                    case UNDONE_DISPLAY:
                        switch (todoObj.getPriority()) {
                            case HIGH_PRIORITY:
                                holder.mPriorityImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.highPriorityColor));
                                break;

                            case MID_PRIORITY:
                                holder.mPriorityImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.midPriorityColor));
                                break;

                            case LOW_PRIORITY:
                                holder.mPriorityImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lowPriorityColor));
                                break;

                            default:
                                holder.mPriorityImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.nullPriorityColor));
                        }

                        break;

                    case DONE_DISPLAY:
                        holder.mPriorityImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.nullPriorityColor));
                        break;
                }


                holder.mTodoDescriptionTextView.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(todoObj.getTodoDescription())) {
                    holder.mTodoDescriptionTextView.setVisibility(View.GONE);
                } else {
                    holder.mTodoDescriptionTextView.setPaintFlags(0);
                    switch (todoObj.getDisplayNum()) {
                        case UNDONE_DISPLAY:
                            holder.mTodoDescriptionTextView.setText(todoObj.getTodoDescription());
                            break;

                        case DONE_DISPLAY:
                            holder.mTodoDescriptionTextView.setText(todoObj.getTodoDescription());
                            holder.mTodoDescriptionTextView.setPaintFlags(holder.mTodoDescriptionTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            break;
                    }
                }


                final int displayNum = todoObj.getDisplayNum();
                switch (displayNum) {
                    case UNDONE_DISPLAY:
                        break;

                    case DONE_DISPLAY:
                        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                            @Override
                            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                                menu.add(Menu.NONE, 0, 0, getResources().getString(R.string.option_restore))
                                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {

                                                ContentValues cv = new ContentValues();
                                                cv.put(DataContract.TodoEntry.COLUMN_DISPLAY, UNDONE_DISPLAY);

                                                mContext.getContentResolver().update(DataContract.TodoEntry.CONTENT_URI,
                                                        cv,
                                                        DataContract.TodoEntry._ID + "=?",
                                                        new String[]{String.valueOf(todoObj.getId())});

                                                return true;
                                            }
                                        });
                            }
                        });
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (displayNum) {
                            case UNDONE_DISPLAY:
                                editorRequest(todoObj);
                                break;
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (mCursor == null || mCursor.getCount() == 0) {
                return 1;
            }
            return mCursor.getCount();
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
