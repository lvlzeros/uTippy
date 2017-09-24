package com.levelzeros.utippy.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.levelzeros.utippy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


/**
 * Created by Poon on 15/2/2017.
 */

/**
 * Utility to download files
 */
public class DownloadHelper extends AsyncTask<URL, Integer, Integer> {
    private static final String TAG = "DownloadHelper";

    //Code to identify different outcomes
    private static final int DOWNLOAD_SUCCESS = 100;
    private static final int DOWNLOAD_ERROR = 101;
    private static final int CONNECTION_TIMEOUT = 102;

    //Variables
    private final Context mContext;
    private final String mCourseName;
    private String mValidatedCourseName;
    private File mDownloadFile;
    private URL mFileUrl;
    private String mFileName;
    private static ProgressDialog mProgressDialog;
    private Toast mToast;

    //Callback to handle retry request
    private OnTaskExecuted mCallback;

    //Interface to handle retry request
    public interface OnTaskExecuted {
        void onRetry(boolean status, String fileUrl);
    }

    public DownloadHelper(Context mContext, String mCourseName, OnTaskExecuted mCallback) {
        this.mContext = mContext;
        this.mCourseName = mCourseName;
        this.mCallback = mCallback;
    }

    /**
     * Setting up download task, check existing file and connection
     */
    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("Downloading file..."); //TODO
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(URL... params) {
        String str;
        int response = 0;
        if (params.length <= 0) {
            return null;
        }
        mFileUrl = params[0];

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(R.raw.utpedumy);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca = " + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);


            HttpsURLConnection connection = (HttpsURLConnection) mFileUrl.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(25000);
            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                    File rootFileDir;
                    File fileDir;

                    str = connection.getURL().toString();
                    mFileName = str.substring(str.lastIndexOf("/") + 1)
                            .split("\\?")[0]
                            .replace("%20", " ")
                            .replace("%21", "!")
                            .replace("%22", "\"")
                            .replace("%23", "#")
                            .replace("%24", "$")
                            .replace("%25", "%")
                            .replace("%26", "&")
                            .replace("%27", "'")
                            .replace("%28", "(")
                            .replace("%29", ")");

                    mValidatedCourseName = mCourseName.replace("/", "_");

                    rootFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mContext.getString(R.string.app_name));
                    rootFileDir.mkdirs();

                    fileDir = new File(rootFileDir, mValidatedCourseName);
                    fileDir.mkdirs();

                    mDownloadFile = new File(fileDir, mFileName);


                    if (DownloadFile()) {
                        response = DOWNLOAD_SUCCESS;
                    } else {
                        response = DOWNLOAD_ERROR;
                    }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = CONNECTION_TIMEOUT;
        }
        return response;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(null !=  mProgressDialog){
            mProgressDialog.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer response) {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
        switch (response) {
            case DOWNLOAD_SUCCESS:
                mToast = Toast.makeText(mContext, "Download success", Toast.LENGTH_SHORT);
                mToast.show();
                return;

            case DOWNLOAD_ERROR:
                mCallback.onRetry(false, mFileUrl.toString());
                return;

            case CONNECTION_TIMEOUT:
                mCallback.onRetry(false, mFileUrl.toString());
                return;

            default:
                mCallback.onRetry(false, mFileUrl.toString());
        }

    }

    /**
     * Prompt dialog to user's, to decide whether to overwrite existing file
     */
    private void promptOverwriteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.title_file_existed))
                .setMessage(mContext.getString(R.string.message_file_existed))
                .setPositiveButton(mContext.getString(R.string.option_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadFile.delete();
                        DownloadFile();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.option_no), null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * Method to download files
     *
     * @return
     */
    private boolean DownloadFile() {
        //Check if user disabled Download Manager
//        int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
//        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
//                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
//                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
//
//            // Prompt user to enable Android Download Manager
//            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            builder.setMessage(mContext.getString(R.string.title_request_download_manager))
//                    .setTitle(mContext.getString(R.string.message_request_download_manager))
//                    .setPositiveButton(mContext.getString(R.string.option_enable_download_manager), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            try {
//                                //Open the specific App Info page:
//                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                intent.setData(Uri.parse("package:com.android.providers.downloads"));
//                                mContext.startActivity(intent);
//                            } catch (ActivityNotFoundException e) {
//                                //Open the generic Apps page:
//                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//                                mContext.startActivity(intent);
//                            }
//                        }
//                    })
//                    .setNegativeButton(mContext.getString(R.string.option_cancel), null);
//            AlertDialog dialog = builder.create();
//            dialog.setCanceledOnTouchOutside(true);
//            dialog.show();
//
//            return false;
//        } else {
//            mToast = Toast.makeText(mContext, mContext.getString(R.string.toast_downloading), Toast.LENGTH_LONG);
//            mToast.show();
//
//            DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mFileUrl.toString()));
//
//            request.setTitle(mFileName);
//            request.setDescription(mContext.getString(R.string.message_downloading));
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            request.setDestinationUri(Uri.fromFile(mDownloadFile));
//            manager.enqueue(request);
//
//            return true;
//        }

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = mContext.getResources().openRawResource(R.raw.utpedumy);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca = " + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection urlConnection = (HttpsURLConnection) mFileUrl.openConnection();
            urlConnection.setConnectTimeout(7000);
            urlConnection.setReadTimeout(7000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            InputStream input = null;
            FileOutputStream output;

            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:

                    int fileLength = urlConnection.getContentLength();

                    try {
                        // download the file
                        input = urlConnection.getInputStream();
                        output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                + File.separator + "uTippy"
                                + File.separator + mValidatedCourseName
                                + File.separator + mFileName);

                        byte data[] = new byte[4096];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            // allow canceling with back button
                            if (isCancelled()) {
                                input.close();
                                return false;
                            }
                            total += count;
                            // publishing the progress....
                            if (fileLength > 0) // only if total length is known
                                publishProgress((int) (total * 100 / fileLength));
                            output.write(data, 0, count);
                        }

                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != input) {
                            input.close();
                        }
                    }

                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}




