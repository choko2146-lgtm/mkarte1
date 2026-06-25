package com.example.mkarte1.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public final class PhotoImageLoader {
    private PhotoImageLoader() {
    }

    public static boolean loadFileInto(ImageView imageView, File file) {
        if (imageView == null || file == null || !file.exists()) {
            return false;
        }

        int targetWidth = resolveTargetWidth(imageView);
        int targetHeight = resolveTargetHeight(imageView);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, targetWidth, targetHeight);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap == null) {
            return false;
        }

        Bitmap rotatedBitmap = applyExifOrientation(bitmap, file);
        imageView.setImageBitmap(rotatedBitmap);
        return true;
    }

    private static int resolveTargetWidth(ImageView imageView) {
        if (imageView.getWidth() > 0) {
            return imageView.getWidth();
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params != null && params.width > 0) {
            return params.width;
        }
        return imageView.getResources().getDisplayMetrics().widthPixels;
    }

    private static int resolveTargetHeight(ImageView imageView) {
        if (imageView.getHeight() > 0) {
            return imageView.getHeight();
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params != null && params.height > 0) {
            return params.height;
        }
        return imageView.getResources().getDisplayMetrics().heightPixels;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        int inSampleSize = 1;

        if (imageHeight > targetHeight || imageWidth > targetWidth) {
            int halfHeight = imageHeight / 2;
            int halfWidth = imageWidth / 2;
            while ((halfHeight / inSampleSize) >= targetHeight
                    && (halfWidth / inSampleSize) >= targetWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap applyExifOrientation(Bitmap bitmap, File file) {
        Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return bitmap;
            }
        } catch (IOException ignored) {
            return bitmap;
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }
}
