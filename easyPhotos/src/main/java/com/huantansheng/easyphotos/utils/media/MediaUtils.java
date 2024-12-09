package com.huantansheng.easyphotos.utils.media;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.blankj.utilcode.util.AppUtils;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.utils.system.SystemUtils;
import com.huantansheng.easyphotos.utils.uri.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MediaUtils {

    /**
     * 获取时长
     *
     * @param path path
     * @return duration
     */
    public static long getDuration(String path) {
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            return Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            Log.e("DurationUtils", e.toString());
        } finally {
            if (mmr != null) {
                try {
                    mmr.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * 格式化时长（不足一秒则显示为一秒）
     *
     * @param duration duration
     * @return "MM:SS" or "H:MM:SS"
     */
    public static String format(long duration) {
        double seconds = duration / 1000.0;
        return DateUtils.formatElapsedTime((long) (seconds + 0.5));
    }

    public static Uri createUri(String displayName, String mimeType) {
        long now = System.currentTimeMillis();
        ;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.DATE_ADDED, now / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, now / 1000);

        if (!SystemUtils.beforeAndroidTen()) {
            values.put(
                    // Added in API level 29
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + File.separator + AppUtils.getAppName()
            );
        }

        Uri uri = getUri(mimeType);
        return EasyPhotos.getApp().getContentResolver().insert(uri, values);
    }

    private static Uri getUri(String mimeType) {
        boolean enable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        Uri uri;
        if (mimeType.startsWith("image") && enable) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("image")) {
            uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("video") && enable) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
        }
        return uri;
    }

    public static boolean writeToUri(@NonNull Uri uri, @NonNull File src) {
        ContentResolver resolver = EasyPhotos.getApp().getContentResolver();
        try {
            InputStream is = null;
            final ContentValues values = new ContentValues();

            if (!SystemUtils.beforeAndroidTen()) {
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                resolver.update(uri, values, null, null);
            }

            OutputStream os = resolver.openOutputStream(uri);
            if (os == null) {
                return false;
            }
            int read;
            if (src.exists()) {
                is = new FileInputStream(src);
                byte[] buffer = new byte[4096];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
            if (is != null) is.close();
            os.close();

            if (!SystemUtils.beforeAndroidTen()) {
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            resolver.delete(uri, null, null);
            return false;
        }
    }

    public static boolean writeToUri(@NonNull Uri uri, @NonNull Bitmap bitmap) {
        ContentResolver resolver = EasyPhotos.getApp().getContentResolver();
        try {
            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            resolver.update(uri, values, null, null);

            OutputStream os = resolver.openOutputStream(uri);
            if (os == null) {
                return false;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            resolver.delete(uri, null, null);
            return false;
        }
    }

    @SuppressLint("Range")
    @Nullable
    public static Pair<String, Photo> getPhoto(File file) {
        Uri uri = UriUtils.getUriByPath(file.getPath());
        if (uri == null) {
            return null;
        }
        Cursor cursor = EasyPhotos.getApp().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        Pair<String, Photo> pair = null;

        if (cursor.moveToFirst()) {
            final long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            final String type = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
            final long size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
            final int width = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH));
            final int height = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT));
            final String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
            final long dateTime = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
            //final long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            final long duration = getDuration(file.getAbsolutePath());
            final String bucketName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME));
            Photo photo = new Photo(name, path, uri, dateTime, width, height, size, duration, type);
            pair = new Pair<>(bucketName, photo);
        }
        cursor.close();
        return pair;
    }
}
