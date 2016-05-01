package com.fudi.fudi.front;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.TestDatabase;
import com.fudi.fudi.back.User;

import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "Fudi-App";

    private LinearLayout fudList;
    private TreeSet<FudView> fudViews;
    private ScrollView scroll;
    private LoadImageInViewTask liivt;
    private FrameLayout mainframe;
    private SwipeRefreshLayout swipeRefreshLayout;
    protected static final int FUD_CREATION_SUCCESS =  1;
    protected static final int FUD_CREATION_FAILURE =  2;
    protected static final int LOGIN_SUCCESS = 3;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(getApplicationContext());

        fudList = (LinearLayout) findViewById(R.id.main_fud_list);
        fudViews = new TreeSet<FudView>();
        mainframe = (FrameLayout) findViewById(R.id.main_frame_layout);
        //set button onClickListener
        ImageButton newFudButton = (ImageButton) findViewById(R.id.main_new_fud_button);
        newFudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FudCreationActivity.class);
                startActivityForResult(intent, FUD_CREATION_SUCCESS);
            }
        });

        scroll = (ScrollView) findViewById(R.id.main_scrollview);

        if(!networkCheck()){
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        if(!sharedPref.contains("userID")){
            String userID = User.generateID();
            sharedPref.edit().putString("userID", userID).commit();
        }

        if(!sharedPref.contains("firstTime")){
            sharedPref.edit().putString("firstTime", "true").commit();
        }
        String firstTime = sharedPref.getString("firstTime", "false");
        FudiApp.getInstance().loadInThisID(sharedPref.getString("userID", "notfound"));

        boolean gotUser = false;
        if(firstTime.equals("true")){
            if(!FudiApp.getInstance().alreadyDidLogin){
                FudiApp.getInstance().getThisUser();
                gotUser = true;
                FudiApp.getInstance().alreadyDidLogin = true;
                Intent reg = new Intent(this, LoginActivity.class);
                startActivity(reg);
            }
        }


        if(!gotUser) {
            (new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    FudiApp.getInstance().getThisUser();
                    return null;
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //liivt = new LoadImageInViewTask(fudViews, scroll);

        //liivt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        (new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                FudiApp.getInstance().pullFudsByTime();
                return null;
            }
        }).execute();

        pull();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        pull();
       // refresh();
        //liivt = new LoadImageInViewTask(fudViews, scroll);
        //liivt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).execute();
        //System.gc();
    }

    @Override
    protected void onPause() {
        //pull();
        super.onPause();
        //liivt.cancel(true);
        //liivt = null;
        //removeAll();
        //System.gc();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //liivt.cancel(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*TODO; show proper popups, do FudiApp.pullFud() and/or FudiApp.getFuds() instead when
        * the database is up and working.
        */
        pull();
        if(requestCode == FUD_CREATION_SUCCESS  && resultCode == RESULT_OK){

           // refresh();
            /**
             * TODO: Uncomment when database works and remove above line
            fudList.add(FudiApp.getInstance().pullFudDetail(data.getStringExtra(Fud.EXTRA_TAG_ID)).simplify());
             */
        } else if (resultCode == FUD_CREATION_FAILURE && requestCode == FUD_CREATION_SUCCESS){
            Toast.makeText(this,R.string.fud_creation_error,Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED){}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean networkCheck(){
        if(!FudiApp.hasNetworkConnection()){
            fudList.removeAllViews();
            LinearLayout noNetwork = (LinearLayout)
                    getLayoutInflater().inflate(R.layout.no_network, null);
            noNetwork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(networkCheck()){
                        pull();
                    }
                }
            });
            fudList.addView(noNetwork);
            return false;
        } else {
            return true;
        }
    }

    public void pull(){
        removeAll();
        TreeSet<FudDetail> fudDetails = FudiApp.getInstance().getCurrentlyDisplayedFudDetails();
        if(fudDetails == null){
            fudDetails =  new TreeSet<FudDetail>();
            Log.e("ERROR","fuds was null in main, loading empty");
        }
        addFudDetailsToList(fudDetails);
        display();
    }

    public void display(){
        int toDisplay = 4;
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(MainActivity.this);
            fudList.addView(between);
            between.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);
            if(toDisplay >= 0){
                fv.loadImage();
                toDisplay--;
            }
        }
    }

    public void displayWithAtTop(Fud fud){
        FudView top = new FudView(MainActivity.this, fud);
        fudList.addView(top);
        top.loadImage();
        Space between1 = new Space(MainActivity.this);
        between1.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);


        int toDisplay = 3;
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(MainActivity.this);
            between.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);

            if(toDisplay > 0){
                fv.loadImage();
                toDisplay--;
            }
        }
    }

    private void addFudsToList(TreeSet<Fud> fuds){
        for(Fud f : fuds){
            addFudView(new FudView(MainActivity.this, f));
        }
    }

    private void addFudDetailsToList(TreeSet<FudDetail> fuds){
        for(FudDetail fd : fuds){
            addFudView(new FudView(MainActivity.this, fd.simplify()));
        }
    }
    /**
     * A logical renaming of pull();
     */
    public void refresh(){
        pull();
    }

    /**
     * Pull's new views from the database, clears the old ones, and display the new ones with
     * the selected {@param fud} up top.
     * @param fud The fud to place up top.
     */
    public void refreshWithAtTop(Fud fud){
        removeAll();
        addFudsToList(TestDatabase.getInstance().getFuds()); //TODO get from real database
        displayWithAtTop(fud);
    }

    public void addFudView(FudView fudView){
        fudViews.add(fudView);
    }

    public void addFudViews(TreeSet<FudView> fudViews){
        for(FudView fv : fudViews) {
            fudViews.add(fv);
        }
    }

    public void remove(FudView fudView){
        fudViews.remove(fudView);
        fudList.removeView(fudView);
    }

    public void removeAll() {
        fudViews.clear();
        fudList.removeAllViews();
    }


    /**
     * TODO: Implement a feature for the screen where if the user overscrolls the top, the list
     * is updated, that is, "Pull down top of screen and refresh"
     *
     * This is seen in many apps, like Facebook, Instagram, Yikyak
     * This probably invovles a listener, so a make a private class for that listener, code the
     * logic there, and add it to fudList (fudList is the one that is being scrolled).
     *
     *
     * the amount. This will get new Fuds which you will have to replace into the fudList
     * (just fudList.add(...);)
     */
    @Override
    public void onRefresh() {
        // TODO - Joan
        // query db and fetch new Fuds
        swipeRefreshLayout.setRefreshing(true); // sets the refresh animation
        pull();
        swipeRefreshLayout.setRefreshing(false);
    }

    private class LoadImageInViewTask extends AsyncTask<Void, Void, Void>{

        int count = 0;

        TreeSet<FudView> views;
        ScrollView scroll;
        public int pauseTime;

        public LoadImageInViewTask(TreeSet<FudView> views, ScrollView scroll){
            this.views = views;
            this.scroll = scroll;
            pauseTime = 500;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(!isCancelled()) {
                int cont = 3;
                count = 0;
                for (final FudView view : views) {
                    if(views.size() == 1){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                view.loadImage();
                            }
                        });
                        break;
                    }

                    if(cont < 0){
                        break;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Rect scrollBounds = new Rect();
                            Rect viewBounds = new Rect();

                            scroll.getHitRect(scrollBounds);
                            view.getLocalVisibleRect(viewBounds);
                            if (!view.getLocalVisibleRect(scrollBounds)) {
                                if(view.imageIsLoaded()){
                                    return;
                                }
                                view.loadImage();
                            } else {
                                view.unloadImage();
                            }
                        }
                    });
                    count++;
                }
                synchronized (this) {
                    try {
                        this.wait(pauseTime);
                    } catch (InterruptedException e) {
                    }
                }
            }

            return null;
        }
    }


}
