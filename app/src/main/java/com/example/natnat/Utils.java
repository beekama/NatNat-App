package com.example.natnat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import java.io.IOException;

public class Utils {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static int getImageOrientation(Context context, Uri uri, String relPath) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(uri, null);

            CursorLoader loader = new CursorLoader(context, uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            relPath = cursor.getString(col);
            ExifInterface exif = new ExifInterface(relPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 180;
                    break;
                default:
                    /* if orientation_tag in exif-data not filled */
                    rotate = 90;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void verifyStoragePermission(Activity activity) {
        int perm = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (perm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}