package com.lcjian.lib.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;

public class ImageChooseHelper {

    private static final int REQUEST_CODE_GET = 0xffff;

    private static final int REQUEST_CODE_CROP = REQUEST_CODE_GET - 1;

    private static final int REQUEST_CODE_CAPTURE = REQUEST_CODE_GET - 2;

    private static final int REQUEST_CODE_ALL = REQUEST_CODE_GET - 3;

    private Activity mActivity;

    private Fragment mFragment;

    private Context mContext;

    private boolean mNeedCrop;

    private Callback mCallback;

    private Uri mUri;

    private Uri mCropUri;

    private ImageChooseHelper(Activity activity, Callback callback) {
        this.mActivity = activity;
        this.mContext = activity;
        this.mCallback = callback;
    }

    private ImageChooseHelper(Fragment fragment, Callback callback) {
        this.mFragment = fragment;
        this.mContext = fragment.getActivity();
        this.mCallback = callback;
    }

    public static ImageChooseHelper create(Activity activity, Callback callback) {
        return new ImageChooseHelper(activity, callback);
    }

    public static ImageChooseHelper create(Fragment fragment, Callback callback) {
        return new ImageChooseHelper(fragment, callback);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = MediaStore.Images.ImageColumns._ID + "=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.Images.ImageColumns.DATA;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private File getCaptureFile() {
        String fileName = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(new Date()) + ".jpg";
        String filePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            filePath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        } else {
            filePath = mContext.getFilesDir().getAbsolutePath() + File.separator + "Capture";
        }
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, fileName);
    }

    private File getCropFile() {
        String fileName = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(new Date()) + ".jpg";
        String filePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            filePath = mContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + "Crop";
        } else {
            filePath = mContext.getFilesDir().getAbsolutePath() + File.separator + "Crop";
        }
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        return new File(path, fileName);
    }

    public void startGet(boolean needCrop) {
        mNeedCrop = needCrop;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (mActivity == null) {
            mFragment.startActivityForResult(intent, REQUEST_CODE_GET);
        } else {
            mActivity.startActivityForResult(intent, REQUEST_CODE_GET);
        }
    }

    public void startCapture(boolean needCrop) {
        mNeedCrop = needCrop;

        mUri = Uri.fromFile(getCaptureFile());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        if (mActivity == null) {
            mFragment.startActivityForResult(intent, REQUEST_CODE_CAPTURE);
        } else {
            mActivity.startActivityForResult(intent, REQUEST_CODE_CAPTURE);
        }
    }

    private void startCrop(Uri uri) {
        mCropUri = Uri.fromFile(getCropFile());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("output", mCropUri);
        intent.putExtra("outputFormat", "JPEG");

        if (mActivity == null) {
            mFragment.startActivityForResult(intent, REQUEST_CODE_CROP);
        } else {
            mActivity.startActivityForResult(intent, REQUEST_CODE_CROP);
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GET: {
                    uri = data == null ? null : data.getData();
                    if (mNeedCrop) {
                        startCrop(uri);
                    } else {
                        mCallback.onResult(getPath(mContext, uri));
                    }
                }
                break;
                case REQUEST_CODE_CAPTURE:
                    uri = mUri;
                    if (mNeedCrop) {
                        startCrop(uri);
                    } else {
                        mCallback.onResult(getPath(mContext, uri));
                    }
                    break;
                case REQUEST_CODE_CROP:
                    mCallback.onResult(getPath(mContext, mCropUri));
                    break;
                case REQUEST_CODE_ALL: {
                    final boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        if (action == null) {
                            isCamera = false;
                        } else {
                            isCamera = action
                                    .equals(MediaStore.ACTION_IMAGE_CAPTURE);
                        }
                    }
                    Uri selectedImageUri;
                    if (isCamera) {
                        selectedImageUri = mUri;
                    } else {
                        selectedImageUri = data == null ? null : data.getData();
                    }
                    if (mNeedCrop) {
                        startCrop(selectedImageUri);
                    } else {
                        mCallback.onResult(getPath(mContext, selectedImageUri));
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    public void openImageIntent() {

        // Determine Uri of camera image to save.
        String filePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            filePath = mContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + "Image";
        } else {
            filePath = mContext.getFilesDir().getAbsolutePath() + File.separator + "Image";
        }
        File path = new File(filePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        final String fname = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(new Date()) + ".jpg";
        final File sdImageMainDirectory = new File(filePath, fname);
        mUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = mContext.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        if (mActivity == null) {
            mFragment.startActivityForResult(chooserIntent, REQUEST_CODE_ALL);
        } else {
            mActivity.startActivityForResult(chooserIntent, REQUEST_CODE_ALL);
        }
    }

    public interface Callback {
        void onResult(String path);
    }
}
