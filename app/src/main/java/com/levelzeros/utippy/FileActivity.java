package com.levelzeros.utippy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.levelzeros.utippy.utility.GeneralUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity to display downloaded files based on selected course
 */
public class FileActivity extends AppCompatActivity {
    private static final String TAG = "FileActivity";

    //Variables
    private String courseName;
    private List<File> fileList = new ArrayList<>();
    private boolean emptyFlag = false;

    //Views
    private SwipeRefreshLayout mFileSwipeRefreshLayout;
    private RecyclerView mFileListRecyclerView;
    private FileRecyclerViewAdapter mFileAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Obtain Course Name from Intent's arguments
        courseName = getIntent().getStringExtra(MainActivity.INTENT_FILE_ACTIVITY_KEY);

        getSupportActionBar().setTitle(courseName);

        mFileSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.file_swipe_refresh_container);
        mFileListRecyclerView = (RecyclerView) findViewById(R.id.file_recycler_view_container);
        mFileAdapter = new FileRecyclerViewAdapter(this, courseName, fileList);

        mFileSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFileList();
            }
        });

        mFileListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFileListRecyclerView.setAdapter(mFileAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFileList();
    }

    /**
     * Method to refresh the content
     */
    public void updateFileList() {
        fileList.clear();
        File rootFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + getString(R.string.app_name), courseName);
        rootFileDir.mkdirs();

        File[] folders = rootFileDir.listFiles();
        if (folders != null) {
            Arrays.sort(folders);
            for (File f : folders) {
                fileList.add(f);
            }
        }
        mFileListRecyclerView.getAdapter().notifyDataSetChanged();
        mFileSwipeRefreshLayout.setRefreshing(false);
    }


    /**
     * Adapter to display downloader files
     */
    class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.FileViewHolder> {
        private static final String TAG = "FileRecyclerViewAdapter";

        private Context mContext;
        private String courseName;
        private List<File> mFileList = new ArrayList<>();


        FileRecyclerViewAdapter(Context mContext, String courseName, List<File> mFileList) {
            this.mContext = mContext;
            this.courseName = courseName;
            this.mFileList = mFileList;
        }

        class FileViewHolder extends RecyclerView.ViewHolder {
            TextView mFileNameTextView;
            CardView mEmptyTooltipCardView;
            ImageView mFileIconImageView;
            LinearLayout mFileListContainer;

            FileViewHolder(View itemView) {
                super(itemView);

                mFileNameTextView = (TextView) itemView.findViewById(R.id.file_name_text_view);
                mEmptyTooltipCardView = (CardView) itemView.findViewById(R.id.empty_tooltip_card_view);
                mFileIconImageView = (ImageView) itemView.findViewById(R.id.file_icon_image_view);
                mFileListContainer = (LinearLayout) itemView.findViewById(R.id.file_list);
            }
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list, parent, false);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, final int position) {

            if (emptyFlag) {
                holder.mEmptyTooltipCardView.setVisibility(View.VISIBLE);
                holder.mFileListContainer.setVisibility(View.GONE);

            } else {
                holder.mEmptyTooltipCardView.setVisibility(View.GONE);
                holder.mFileListContainer.setVisibility(View.VISIBLE);

                final String fileName = mFileList.get(position).getName();
                holder.mFileNameTextView.setText(fileName);

                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        menu.setHeaderTitle(fileName);
                        menu.add(Menu.NONE, 0, 0, mContext.getString(R.string.option_delete)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                final File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                        + File.separator + mContext.getString(R.string.app_name) + File.separator + courseName, fileName);

                                if (DeleteRecursive(fileDir)) {
                                    updateFileList();
                                    Toast.makeText(mContext, mContext.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                                }

                                /**
                                 * Second verification for user's request to delete file/directory
                                 */
//                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                                builder.setMessage(mContext.getString(R.string.message_prompt_delete_file))
//                                        .setTitle(mContext.getString(R.string.title_prompt_delete_file))
//                                        .setPositiveButton(mContext.getString(R.string.option_delete), new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                if (DeleteRecursive(fileDir)) {
//                                                    updateFileList();
//                                                    Toast.makeText(mContext, mContext.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        })
//                                        .setNegativeButton(mContext.getString(R.string.option_cancel), null);
//
//                                AlertDialog alert = builder.create();
//                                alert.setCanceledOnTouchOutside(true);
//                                alert.show();

                                return true;
                            }
                        });
                    }
                });

                String fileExt = getFileExtension(mFileList.get(position));
                holder.mFileIconImageView.setImageResource(GeneralUtils.getFileIcon(fileExt));


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File f = mFileList.get(position);
                        onFileClick(f);

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (mFileList.isEmpty()) {
                emptyFlag = true;
                return 1;
            }
            emptyFlag = false;
            return mFileList.size();
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


        /**
         * Handle user's request to open file
         *
         * @param f desired File object to be opened
         */
        public void onFileClick(File f) {

            String fileExt = getFileExtension(f);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);
            Uri fileUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", f);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mime);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setDataAndType(Uri.fromFile(f), mime);

            //Option 1
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "No handler to open this file type", Toast.LENGTH_SHORT).show();
            }

            //Option 2
//        try {
//
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(mContext, "No handler to open this file type", Toast.LENGTH_SHORT).show();
//        }
        }

        /**
         * Method to get file extension
         *
         * @param f desired File object to be opened
         * @return
         */
        String getFileExtension(File f) {
            String fileName = Uri.fromFile(f).getLastPathSegment();

            if (fileName.lastIndexOf(".") == -1) {
                return null;
            } else {
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                return ext.toLowerCase();
            }
        }
    }
}
