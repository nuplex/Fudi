package com.fudi.fudi.back;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.fudi.fudi.R;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Handles the image processing for the app.
 * All processing here is Asynchronous
 * Created by chijioke on 4/22/16.
 */
public class ImageHandler {

    private static ImageHandler inst = new ImageHandler();

    private static long imageNum = 0;

    public static ImageHandler getInstance(){
        return inst;
    }

    public static final String DIRECTORY = "Fudi";

    public void loadImageIntoImageView(Context context, ImageView imageView, String url) {
        (new LoadImageTask(context, imageView)).execute(url);
    }

    public void uploadImageToDatabase(Context context, Bitmap image){
        (new UploadImageTask(context)).execute(image);
    }

    public UploadImageTask uploadImageToDatabase(Context context, ImageView image){
        Bitmap b = drawableToBitmap(image.getDrawable());
        return (UploadImageTask) (new UploadImageTask(context)).execute(b);
    }


    public void uploadImageToDatabaseForFudDetail(Context context, FudDetail fudDetail, Bitmap image){
        (new UploadImageTask(context, fudDetail)).execute(image);
    }

    /**
     * Scales a bitmap down for an image view. Note that this uses the imageView's height and
     * width as the scale factor.
     * @param b The Bitmap to scale
     * @param imageView the ImageView it is being scaled to
     */
    public void scaleBitmapForImageView(Bitmap b, ImageView imageView){
        (new ProcessBitmapTask(imageView, imageView.getWidth(), imageView.getWidth())).execute(b);
    }

    /**
     * Scales a bitmap down for an image view. Note that this uses the imageView's height and
     * width as the scale factor.
     * @param b The Bitmap to scale
     * @param imageView the ImageView it is being scaled in
     * @param width the width for the new scale
     * @param height the height for the new scale
     */
    public void scaleBitmapForImageView(Bitmap b, ImageView imageView, int width, int height){
        (new ProcessBitmapTask(imageView, width, height)).execute(b);
    }

    /**
     * Scales a bitmap down for an image view. Note that this uses the imageView's height and
     * width as the scale factor.
     * @param b The Bitmap to scale
     * @param imageView the ImageView it is being scaled in
     * @param width the width for the new scale
     * @param height the height for the new scale
     * @param quality quality of the JPEG, 1-100
     */
    public void scaleBitmapForImageView(Bitmap b, ImageView imageView, int width, int height, int quality){
        (new ProcessBitmapTask(imageView, width, height, quality)).execute(b);
    }


    /**
     * Creates a [placeholder] file for an image.
     *
     * Adapted from:
     * http://developer.android.com/training/camera/photobasics.html
     *
     * Do "file: "+image.getAbsolutePath()" for ACTION_VIEW intents
     *
     * @return the File the image will be at
     */
    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        timeStamp.toUpperCase();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), DIRECTORY);
        storageDir.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    /**
     * Code from:
     * http://stackoverflow.com/a/10600736
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /* Below two methods from: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //adapted for ByteArray
    public static Bitmap decodeSampledBitmapStream(byte[] ba, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(ba,0,ba.length,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(ba, 0, ba.length, options);

    }

    public static Bitmap decodeSampledBitmapFD(FileDescriptor fd, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);

    }


    public static Bitmap decodeSampledBitmapUri(Uri uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri.getPath(),options);

    }

    /**
     * Returns the number of pixels for the desired dp
     * @param dp
     * @return pixels for dp
     */
    public static int pfdp(int dp, Context context){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static float pfdp(float dp, Context context){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * The asynchronous task downloads an image for an image view from a URL.
     */
    public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;
        private Context context;
        private BitmapFactory.Options bfo;

        public LoadImageTask(Context context, ImageView imageView){
            this.imageView = imageView;
            this.context = context;
            bfo =  new BitmapFactory.Options();
        }

        public LoadImageTask(Context context, ImageView imageView, BitmapFactory.Options bfo){
            this.imageView = imageView;
            this.context = context;
            this.bfo = bfo;
        }

        @Override
        protected Bitmap doInBackground(String... links) {
            String link = links[0];
            Bitmap image = null;
            try{
                URL url = new URL(link);

                image = BitmapFactory.decodeStream(url.openStream(), null, bfo);
            } catch (IOException e) {
                e.printStackTrace();
                image = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.fudi_error_loading_image);
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            Drawable d = new BitmapDrawable(context.getResources() ,result);
            imageView.setImageDrawable(d);
        }
    }

    public static Drawable bitmapToDrawable(Context context, Bitmap b){
        Drawable d = new BitmapDrawable(context.getResources(), b);
        return  d;
    }

    /**
     * Uploads an Image to the server.
     *
     * The server is located at fudi.us/dish
     *
     * <u>Info:</u>
     * <b>
     * FTP Username: imagecahce@fudi.us
     * FTP server: ftp.fudi.us
     * FTP & explicit FTPS port:  21
     * </b>
     *
     */
    public class UploadImageTask extends AsyncTask<Bitmap,Void,String>{

        private static final String FTP_SERVER = "ftp.fudi.us";
        private static final String FTP_USERNAME = "imagecahce@fudi.us";
        private static final String FTP_PASSWORD = "pficfFmi2016";
        private static final String FTP_ERROR = "FTP Error";
        private static final int FTP_PORT = 21;
        public static final String PREFIX = "DS";
        private static final String FILE_TYPE = ".jpg";
        private static final String FTP_FULL_QUALIFIED = "http://www.fudi.us/dish/";

        private Context context;
        private String id;
        private String imageUrl;

        private FudDetail fudDetail;

        private boolean alreadyDecoded = false;
        private int quality;
        /**
         *
         * @param context
         * @param fudDetail where the image url will be loaded into.
         */
        public UploadImageTask(Context context, FudDetail fudDetail) {
            this.context = context;
            this.fudDetail = fudDetail;
            this.id = fudDetail.getFudID();
            quality = 50;
        }

        /**
         *
         * @param context
         * @param fudDetail where the image url will be loaded into.
         * @param quality quality of the jpg to be uploaded 1-100
         */
        public UploadImageTask(Context context, FudDetail fudDetail, int quality) {
            this.context = context;
            this.fudDetail = fudDetail;
            this.id = fudDetail.getFudID();
            this.quality = quality;
        }


        /**
         * Call this if you are just uploading a photo.
         * @param context
         */
        public UploadImageTask(Context context){
            this.context = context;
            quality = 50;
            id = generateID();

        }

        /**
         * Call this if you are just uploading a photo.
         * @param context
         * @param quality Quality of the jpg to be uploaded 0-100
         */
        public UploadImageTask(Context context, int quality){
            this.context = context;
            this.quality = quality;
            id = generateID();
        }


        @Override
        protected String doInBackground(Bitmap... images) {
            FTPClient server = new FTPClient();
            try {
                server.connect(FTP_SERVER, FTP_PORT);
                if(!FTPReply.isPositiveCompletion(server.getReplyCode())){
                    Log.e(FTP_ERROR, "Could not connect to server: " + server.getReplyString());
                    Log.e(FTP_ERROR, server.getReplyString());
                    return null;
                }
                boolean success = server.login(FTP_USERNAME,FTP_PASSWORD);
                if(!success){
                    Log.e(FTP_ERROR, "Could not login to server: "+server.getReplyString());
                    Log.e(FTP_ERROR, server.getReplyString());
                    return null;
                }

                //prevent firewall blocks
                server.enterLocalPassiveMode();

                server.setFileType(FTP.BINARY_FILE_TYPE);

                //The meat
                Bitmap bitmap = images[0];

                //Give image a location
                File file = new File(context.getCacheDir(), "imgup"+imageNum++);

                //Image to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                byte[] bitmapData = baos.toByteArray();

                //write it to the file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapData);
                fos.flush();
                fos.close();

                //Now push the file itself to the server
                String remoteFile= PREFIX+id+FILE_TYPE;
                InputStream in = new FileInputStream(file);
                success = server.storeFile(remoteFile,in);
                in.close();
                if(!success){
                    Log.e(FTP_ERROR,"Could not write "+file.getName()+" to server as "+remoteFile);
                    Log.e(FTP_ERROR, server.getReplyString());
                    server.disconnect();
                    return null;
                }
                server.disconnect();
                return FTP_FULL_QUALIFIED+remoteFile;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(FTP_ERROR, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String url) {
            if(imageNum > 10000){
                imageNum = 0;
            }

            imageUrl = url;

            if(fudDetail != null) {
                if (url == null) {
                    fudDetail.setImageURL("http://www.fudi.us/dish/error.jpg");
                } else {
                    if (fudDetail != null) {
                        fudDetail.setImageURL(url);
                    }
                }
            }
        }

        /**
         * Only call this method when you are sure the task has ended.
         * @return the URL the image was uploaded to, otherwise null;
         */
        public String getURLUploadedTo(){
            return imageUrl;
        }

        public String generateID(){
            //TODO: implement a virutally collision free generation method
            //Should be 6 alphanumeric characters, DO NOT use the fuddetail, this is independent
            return FudiApp.generateID(20);
        }
    }

    public class ProcessBitmapTask extends AsyncTask<Bitmap,Void,Bitmap>{

        WeakReference<ImageView> toSet;
        int height;
        int width;
        int quality;

        public ProcessBitmapTask(ImageView toSet, int width, int height){
            this.toSet = new WeakReference<ImageView>(toSet);
            this.width = width;
            this.height = height;
            this.quality = 30;
        }

        public ProcessBitmapTask(ImageView toSet, int width, int height, int quality){
            this.toSet = new WeakReference<ImageView>(toSet);
            this.width = width;
            this.height = height;
            this.quality = quality;
        }


        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap b = params[0];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG,quality,baos);
            byte[] image = baos.toByteArray();
            return ImageHandler.decodeSampledBitmapStream(image, width, height);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            toSet.get().setImageBitmap(bitmap);
        }
    }

    //http://stackoverflow.com/questions/14759601/proper-ondestroy-how-to-avoid-memory-leaks
    public static void unbindDrawables(View view)
    {
        if (view.getBackground() != null)
        {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView))
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

}
