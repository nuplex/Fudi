package com.fudi.fudi.front;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.TestDatabase;

import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private LinearLayout fudList;
    private TreeSet<FudView> fudViews;
    protected static final int FUD_CREATION_SUCCESS =  1;
    protected static final int FUD_CREATION_FAILURE =  2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fudList = (LinearLayout) findViewById(R.id.main_fud_list);
        fudViews = new TreeSet<FudView>();
        Firebase.setAndroidContext(getApplicationContext());

        //set button onClickListener
        ImageButton newFudButton = (ImageButton) findViewById(R.id.main_new_fud_button);
        newFudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FudCreationActivity.class);
                startActivityForResult(intent, FUD_CREATION_SUCCESS);
            }
        });

        //TODO: load in any new posts from database (real when thats finished)
        pull();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAll();
        System.gc();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*TODO; show proper popups, do FudiApp.pullFud() and/or FudiApp.getFuds() instead when
        * the database is up and working.
        */
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO everything below shold probably be done asynchronously

    /**
     * Pulls the default amount of Fuds from the databse and displays them on the screen.
     * This removes all Fuds that wereon the screen beforehand.
     */
    public void pull(){
        removeAll();
        addFudsToList(TestDatabase.getInstance().getFuds()); //TODO get from real database
        display();
    }

    public void display(){
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(this);
            fudList.addView(between);
            between.getLayoutParams().height = ImageHandler.pfdp(20,this);
        }
    }

    public void displayWithAtTop(Fud fud){
        fudList.addView(new FudView(this, fud));
        Space between1 = new Space(this);
        between1.getLayoutParams().height = ImageHandler.pfdp(20,this);

        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(this);
            between.getLayoutParams().height = ImageHandler.pfdp(20,this);
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

    private void addFudsToList(TreeSet<Fud> fuds){
        for(Fud f : fuds){
            addFudView(new FudView(MainActivity.this, f));
        }
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




}
