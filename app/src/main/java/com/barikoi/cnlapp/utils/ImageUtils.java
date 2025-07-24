package com.barikoi.cnlapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static byte[] decodeFile(String f) {
        final long mRequestStartTime = System.currentTimeMillis();
        if (f != null) {
            Log.d("imageUtils", "filepath: " + f);
            BitmapFactory.Options o = new BitmapFactory.Options();

            File imageFile2 = new File(f);
            Log.d("imageUtils", "scale 1: " + imageFile2.length() / 1024 + " kb");

            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f, o);

            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;

            /*if (imageFile2.length()/1024 > 4000){
                IMAGE_MAX_SIZE = 600;
            }else{
                IMAGE_MAX_SIZE = 800;
            }*/

            if (o.outWidth > o.outHeight) {
                scale = o.outWidth / IMAGE_MAX_SIZE;
                Log.d("imageUtils", "Image max size width: " + IMAGE_MAX_SIZE);
            } else {
                scale = o.outHeight / IMAGE_MAX_SIZE;
                Log.d("imageUtils", "Image max size height: " + IMAGE_MAX_SIZE);
            }

            // Decode the image file into a Bitmap sized to fill the View
            /*if (IMAGE_MAX_SIZE > 1 ){
                scale=Math.max(o.outWidth/IMAGE_MAX_SIZE, o.outHeight/IMAGE_MAX_SIZE);
            }else if (o.outWidth/IMAGE_MAX_SIZE <= 1 && o.outHeight/IMAGE_MAX_SIZE > 1){
                scale=Math.min(o.outWidth, o.outHeight/IMAGE_MAX_SIZE);
            }else if(o.outWidth/IMAGE_MAX_SIZE > 1 && o.outHeight/IMAGE_MAX_SIZE <= 1){
                scale=Math.min(o.outWidth/IMAGE_MAX_SIZE, o.outHeight);
            }*/

            Log.d("imageUtils", "scale: " + scale + " outw: " + o.outWidth + " outh: " + o.outHeight);
            o.inSampleSize = scale;
            o.inJustDecodeBounds = false;
            o.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(f, o);

            Log.d("imageUtils", "bitmapsize " + bitmap.getByteCount() + " h: " + bitmap.getHeight() + " w: " + bitmap.getWidth());
            int rotate = 0;
            try {
                //getContentResolver().notifyChange(photoURI, null);

                ExifInterface exif = new ExifInterface(f);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                Log.d("Imagepos", "orientationHelper: " + orientation);
                Log.d("Imagepos", "orientationHelper: " + ExifInterface.ORIENTATION_ROTATE_270);
                Log.d("Imagepos", "orientationHelper: " + ExifInterface.ORIENTATION_ROTATE_180);
                Log.d("Imagepos", "orientationHelper: " + ExifInterface.ORIENTATION_ROTATE_90);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                }

                /****** Image rotation ****/
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                Log.d("Imagepos", "fmap: " + bitmap);


            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

            Log.d("imageUtils", "imagesize " + byteArrayOutputStream.size() / 1024 + " kb");
            Log.d("imageUtils", "imagesize length: " + byteArrayOutputStream.toByteArray().length / 1024 + " kb");
            Log.d("imageUtils", "time length: " + (System.currentTimeMillis() - mRequestStartTime) + " miliseconds");

            //Sentry.captureMessage("image convertion time: "+(System.currentTimeMillis() - mRequestStartTime)+" miliseconds");
            return byteArrayOutputStream.toByteArray();
        } else {
            return null;
        }

/*
        Bitmap b = null;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        InputStream fis = null;


        int IMAGE_MAX_SIZE = 1100;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = context.getContentResolver().openInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
            Log.d("imageutils","imagesize after inputstream"+b.getByteCount()+"");
        }  catch (Exception e) {
            Log.e("imageUtils",e.getMessage());
        }
       *//*try {
            b=rotateImageIfRequired(fis);
            Log.d("imageutils","imagesize after roatation"+b.getByteCount()+"");
        } catch (Exception e) {
            Log.e("imageUtils",e.getMessage());
        }*//*

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);

        Log.d("imagesize",byteArrayOutputStream.size()+"");
        return byteArrayOutputStream.toByteArray();*/
    }

    public static Bitmap rotateImageIfRequired(InputStream is) throws IOException {

        /*if (selectedImage.getScheme().equals("content")) {
            String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
            Cursor c = context.getContentResolver().query(selectedImage, projection, null, null, null);
            if (c.moveToFirst()) {
                if(c.getColumnCount()>0) {
                    final int rotation = c.getInt(0);
                    c.close();
                    return rotateImage(img, rotation);
                }else return img;

            }

        }*/
        Bitmap img = BitmapFactory.decodeStream(is);
        ExifInterface ei = new ExifInterface(is);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Log.d("orientation", orientation + "");
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }

    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        return rotatedImg;
    }


}
