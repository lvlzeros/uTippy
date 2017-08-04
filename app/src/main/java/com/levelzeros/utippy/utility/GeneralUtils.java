package com.levelzeros.utippy.utility;

import com.levelzeros.utippy.R;

/**
 * Created by Poon on 8/3/2017.
 */

public class GeneralUtils {
    private static final String TAG = "GeneralUtils";

    /**
     * Provide matching file icon corresponding to the given file extension
     * @param extension String representing file extension
     * @return
     */
    public static int getFileIcon(String extension) {
        if ("ai".equals(extension)) {
            return R.drawable.ic_file_ai;
        } else if ("avi".equals(extension)) {
            return R.drawable.ic_file_avi;
        } else if ("css".equals(extension)) {
            return R.drawable.ic_file_css;
        } else if ("csv".equals(extension)) {
            return R.drawable.ic_file_csv;
        } else if ("dbf".equals(extension)) {
            return R.drawable.ic_file_dbf;
        } else if ("doc".equals(extension) || "docx".equals(extension)) {
            return R.drawable.ic_file_doc;
        } else if ("dwg".equals(extension)) {
            return R.drawable.ic_file_dwg;
        } else if ("exe".equals(extension)) {
            return R.drawable.ic_file_exe;
        } else if ("fla".equals(extension)) {
            return R.drawable.ic_file_fla;
        } else if ("html".equals(extension)) {
            return R.drawable.ic_file_html;
        } else if ("iso".equals(extension)) {
            return R.drawable.ic_file_iso;
        } else if ("js".equals(extension)) {
            return R.drawable.ic_file_javascript;
        } else if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return R.drawable.ic_file_jpg;
        } else if ("json".equals(extension)) {
            return R.drawable.ic_file_json_file;
        } else if ("mp3".equals(extension)) {
            return R.drawable.ic_file_mp3;
        } else if ("mp4".equals(extension)) {
            return R.drawable.ic_file_mp4;
        } else if ("pdf".equals(extension)) {
            return R.drawable.ic_file_pdf;
        } else if ("png".equals(extension)) {
            return R.drawable.ic_file_png;
        } else if ("ppt".equals(extension) || "pptx".equals(extension)) {
            return R.drawable.ic_file_ppt;
        } else if ("psd".equals(extension)) {
            return R.drawable.ic_file_psd;
        } else if ("rtf".equals(extension)) {
            return R.drawable.ic_file_rtf;
        } else if ("svg".equals(extension)) {
            return R.drawable.ic_file_svg;
        } else if ("txt".equals(extension)) {
            return R.drawable.ic_file_txt;
        } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
            return R.drawable.ic_file_xls;
        } else if ("xml".equals(extension)) {
            return R.drawable.ic_file_xml;
        } else if ("zip".equals(extension)) {
            return R.drawable.ic_file_zip;
        } else if ("rar".equals(extension) || "7z".equals(extension)) {
            return R.drawable.ic_file_zip_1;
        } else {
            return R.drawable.ic_file_file;
        }
    }
}
