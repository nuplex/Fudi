package com.fudi.fudi.front;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.fudi.fudi.R;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Joan on 4/30/2016.
 * Initially copied from http://www.codehenge.net/2011/06/android-development-tutorial-asynchronous-lazy-loading-and-caching-of-listview-images/
 */
public class ImageManager {

    // store images for display
    private HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();

    // directory for longer-term image cache
    private File cacheDir;

    // manage the queue
    private ImageQueue imageQueue;

    public ImageManager(Context context) {

        // Find the dir to save cached images
        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            cacheDir = new File(sdDir, "data/Fudi");
        } else
            cacheDir = context.getCacheDir();

        if (!cacheDir.exists())
            cacheDir.mkdirs();

        imageQueue = new ImageQueue();
    }

    public void displayImage(String url, Activity activity, ImageView imageView) {
        if(imageMap.containsKey(url))
            imageView.setImageBitmap(imageMap.get(url));
        else {
            imageQueue.queueImage(url, imageView);
            // TODO - joan
            // add the image here, or may not need to use this method at all
//            imageView.setImageResource(R.drawable.icon);
        }
    }

    /**
     * Store URL and the corresponding ImageView for an image.
     * */
    private class ImageRef {
        public String url;
        public ImageView imageView;

        public ImageRef(String u, ImageView i) {
            url=u;
            imageView=i;
        }
    }

    /**
     * Manage which images should be shown to correspond with scrolling.
     * */
    private class ImageQueue {

        // TODO - joan
        // this is copied from the tutorial - why using Stack if this is a queue o.o
        private Stack<ImageRef> imageRefs = new Stack<ImageRef>();

        //removes all instances of this ImageView
        public void clean(ImageView view) {
            for(int i = 0 ;i < imageQueue.imageRefs.size();) {
                if(imageQueue.imageRefs.get(i).imageView == view)
                    imageQueue.imageRefs.remove(i);
                else ++i;
            }
        }

        // TODO - joan
        // need to test this
        private void queueImage(String url, ImageView imageView) {
            // This ImageView might have been used for other images, so we clear
            // the queue of old tasks before starting.
            clean(imageView);
            ImageRef p = new ImageRef(url, imageView);
            synchronized (imageQueue.imageRefs) {
                imageQueue.imageRefs.push(p);
                imageQueue.imageRefs.notifyAll();
            }
        }
    }

    // TODO - joan
    // replace downloading functionality with stuff from MainActivity pull
    /**
     * Run in background to watch the queue and get images (either from semi-persistent cache or
     * by downloading as necessary) as they are queued.
     * */
    private class ImageQueueManager implements Runnable {
        @Override
        public void run() {
            try {
                while(true) {
                    // Thread waits until there are images in the
                    // queue to be retrieved
                    if(imageQueue.imageRefs.size() == 0) {
                        synchronized(imageQueue.imageRefs) {
                            imageQueue.imageRefs.wait();
                        }
                    }
                    // When we have images to be loaded
                    if(imageQueue.imageRefs.size() != 0) {
                        ImageRef imageToLoad;
                        synchronized(imageQueue.imageRefs) {
                            imageToLoad = imageQueue.imageRefs.pop();
                        }
                        Bitmap bmp = getBitmap(imageToLoad.url);
                        imageMap.put(imageToLoad.url, bmp);
                        // TODO: Display image in ListView on UI thread

                        Object tag = imageToLoad.imageView.getTag();
                        // Make sure we have the right view - thread safety defender
                        if(tag != null && ((String)tag).equals(imageToLoad.url)) {
                            BitmapDisplayer bmpDisplayer =
                                    new BitmapDisplayer(bmp, imageToLoad.imageView);
                            Activity a =
                                    (Activity)imageToLoad.imageView.getContext();
                            a.runOnUiThread(bmpDisplayer);
                        }
                    }


                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {}
        }

        private Bitmap getBitmap(String url) {
            String filename = String.valueOf(url.hashCode());
            File f = new File(cacheDir, filename);
            // Is the bitmap in our cache?
            Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
            if(bitmap != null) return bitmap;
            // Nope, have to download it
            try {
                bitmap =
                        BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
                // save bitmap to cache for later
                writeFile(bitmap, f);
                return bitmap;
            } catch (Exception ex) { ex.printStackTrace(); return null; }
        }
        private void writeFile(Bitmap bmp, File f) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            } catch (Exception e) { e.printStackTrace(); }
            finally {
                try {
                    if (out != null ) out.close();
                } catch(Exception ex) {}
            }
        }
    }

    //Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ImageView imageView;
        public BitmapDisplayer(Bitmap b, ImageView i) {
            bitmap=b;
            imageView=i;
        }
        public void run() {
            if(bitmap != null)
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(R.drawable.fudi_icon);
        }
    }
}
