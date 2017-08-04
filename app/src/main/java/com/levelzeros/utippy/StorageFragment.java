package com.levelzeros.utippy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.levelzeros.utippy.MainActivity.REQUEST_CODE_WRITE_EXT_STORAGE;

/**
 * Created by Poon on 22/2/2017.
 */

public class StorageFragment extends Fragment {
    private static final String TAG = "StorageFragment";

    //Variable
    private Context mContext;
    private List<String> mCourseNameList = new ArrayList<>();

    //Views
    private SwipeRefreshLayout mStorageSwipeRefreshLayout;
    private RecyclerView mStorageRecyclerView;

    //Callback
    private OnStorageItemClickListener mListener;

    //Interface to handle user's request to open folder
    interface OnStorageItemClickListener {
        void onStorageItemClick(String courseName);
    }

    public StorageFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.storage_main_fragment, container, false);

        mStorageRecyclerView = (RecyclerView) view.findViewById(R.id.storage_recycler_view_container);
        mStorageSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.storage_swipe_refresh_container);


        mStorageSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int hasWriteStoragePermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                    updateCourseList();
                } else {

                    Snackbar.make(mStorageRecyclerView, getString(R.string.storage_permission_error), 10000)
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
        });

        StorageRecyclerViewAdapter mAdapter = new StorageRecyclerViewAdapter(mCourseNameList, mContext);
        mStorageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mStorageRecyclerView.setAdapter(mAdapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateCourseList();
    }

    /**
     * Ensure the context this fragment attached to is an instance of StorageItemClickListener
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStorageItemClickListener) {
            mListener = (OnStorageItemClickListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must be implement OnStorageItemClickListener");
        }
    }

    /**
     * Reset StorageItemClickListener
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Method to refresh the course's main folder
     */
    public void updateCourseList() {
        mCourseNameList.clear();
        File rootFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name));
        rootFileDir.mkdirs();

        File[] folders = rootFileDir.listFiles();
        if (folders != null) {
            Arrays.sort(folders);
            for (File f : folders) {
                mCourseNameList.add(f.getName());
            }
        }

        mStorageRecyclerView.getAdapter().notifyDataSetChanged();
        mStorageSwipeRefreshLayout.setRefreshing(false);
    }


    /**
     * Adapter to populate views of folders in main directory
     */
    class StorageRecyclerViewAdapter extends RecyclerView.Adapter<StorageRecyclerViewAdapter.StorageViewHolder> {
        private static final String TAG = "StorageRecyclerViewAdap";

        private List<String> mCourseNameList;
        private Context mContext;


        public StorageRecyclerViewAdapter(List<String> mCourseNameList, Context context) {
            this.mCourseNameList = mCourseNameList;
            this.mContext = context;
        }

        public class StorageViewHolder extends RecyclerView.ViewHolder {
            TextView mCourseNameTextView;
            CardView mEmptyToolTipCardView;
            CardView mStorageCardView;

            public StorageViewHolder(View itemView) {
                super(itemView);

                mCourseNameTextView = (TextView) itemView.findViewById(R.id.storage_course_name);
                mEmptyToolTipCardView = (CardView) itemView.findViewById(R.id.empty_tooltip_card_view);
                mStorageCardView = (CardView) itemView.findViewById(R.id.storage_folder_card_view);
            }
        }

        @Override
        public StorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_course_list_fragment, parent, false);

            return new StorageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final StorageViewHolder holder, int position) {
            if (mCourseNameList == null || mCourseNameList.size() == 0) {
                holder.mEmptyToolTipCardView.setVisibility(View.VISIBLE);
                holder.mStorageCardView.setVisibility(View.GONE);
            } else {
                holder.mEmptyToolTipCardView.setVisibility(View.GONE);
                holder.mStorageCardView.setVisibility(View.VISIBLE);

                final String courseName = mCourseNameList.get(position);
                holder.mCourseNameTextView.setText(courseName);

                //Callback to open selected folder
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onStorageItemClick(courseName);
                    }
                });

                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.setHeaderTitle(courseName);
                        menu.add(Menu.NONE, 0, 0, mContext.getString(R.string.option_delete)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                final File folderDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                        + File.separator + mContext.getString(R.string.app_name), courseName);

                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage(mContext.getString(R.string.message_prompt_delete_folder))
                                        .setTitle(mContext.getString(R.string.title_prompt_delete_folder))
                                        .setPositiveButton(mContext.getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (DeleteRecursive(folderDir)) {
                                                    int position = mCourseNameList.indexOf(courseName);
                                                    mStorageRecyclerView.getAdapter().notifyItemRemoved(position);
                                                    mCourseNameList.remove(position);
                                                    Toast.makeText(mContext, mContext.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setNegativeButton(mContext.getString(R.string.option_cancel), null);

                                AlertDialog alert = builder.create();
                                alert.setCanceledOnTouchOutside(true);
                                alert.show();
                                return true;
                            }
                        });
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            if(mCourseNameList.size() == 0){
                return 1;
            }
            return mCourseNameList.size();
        }
    }

    /**
     * Method to delete file/directory
     *
     * @param fileOrDirectory File object pointing to file/directory
     * @return
     */
    public boolean DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                child.delete();
                DeleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }
}
