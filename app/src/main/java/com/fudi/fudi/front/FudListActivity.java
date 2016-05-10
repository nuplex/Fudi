package com.fudi.fudi.front;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Toast;

import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.TestDatabase;

import java.util.TreeSet;

/**
 * This activity is called from the MeActivity screen, displaying either the fuds the user
 * has poster or commented on.
 */
public class FudListActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener  {

    private LinearLayout fudList;
    private TreeSet<FudView> fudViews;
    private ScrollView scroll;

    private SwipeRefreshLayout swipeRefreshLayout;
    private String userID;

    private boolean comment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fud_list);

        Intent i = getIntent();
        String button = i.getStringExtra("button");
        userID = i.getStringExtra("userID");
        if(button.equals(MeActivity.COMMENT_PRESSED)){
            setTitle("Commented On");
            comment = true;
        } else {
            setTitle("Your Fuds");
            comment = false;
        }

        if(!networkCheck()){
            return;
        }

        fudList = (LinearLayout) findViewById(R.id.fud_list_list);
        fudViews = new TreeSet<FudView>();
        scroll = (ScrollView) findViewById(R.id.fud_list_scrollview);

        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    for (FudView fv : fudViews) {
                        Rect viewR = new Rect();
                        fv.getDrawingRect(viewR);
                        scroll.getDrawingRect(viewR);
                        int functionalTop = viewR.top - ImageHandler.pfdp(232 + 20, FudListActivity.this);
                        int functionalBottom = viewR.bottom;
                        int thisTop = fv.getTopInScroll();
                        if (thisTop < functionalBottom && thisTop > functionalTop) {
                            if (!fv.imageIsLoaded()) {
                                fv.loadImage();
                            }
                        } else {
                            if (fv.imageIsLoaded()) {
                                fv.unloadImage();
                            }
                        }
                    }
                }
                return false;
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fud_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        pull();

    }

    private void commentActions(){}

    private void ownFudsActions(){}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fud_list, menu);
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
        } else if (id == R.id.fudi_action_back){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pull(){
        removeAll();
        TreeSet<FudDetail> fudDetails = new TreeSet<>();
        if(comment) {
            fudDetails = FudiApp.getInstance().getCurrentUsersCommentedOnFudDetails();
        } else {
            fudDetails = FudiApp.getInstance().getCurrentUsersFudDetails();
        }
        if(fudDetails == null){
            fudDetails =  new TreeSet<FudDetail>();
            Log.e("ERROR","fuds was null in main, loading empty");
        }
        addFudDetailsToList(fudDetails);
        if(!fudDetails.isEmpty()) {
            display();
        } else {
            fudList.addView(View.inflate(this, R.layout.nothing_here, null));
        }
    }

    public void display(){
        int toDisplay = 3;
        int pos = 0;
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(FudListActivity.this);
            fudList.addView(between);
            between.getLayoutParams().height = ImageHandler.pfdp(20,FudListActivity.this);
            fv.setTopInScroll(pos);
            if(toDisplay >= 0){
                fv.loadImage();
                toDisplay--;
            }
            pos = pos + ImageHandler.pfdp(232 + 20, this);
        }
    }

    private void addFudsToList(TreeSet<Fud> fuds){
        for(Fud f : fuds){
            FudView fv = new FudView(FudListActivity.this, f);
            addFudView(fv);
        }
    }

    private void addFudDetailsToList(TreeSet<FudDetail> fuds){
        for(FudDetail fd : fuds){
            FudView fv = new FudView(FudListActivity.this, fd.simplify());
            addFudView(fv);
        }
    }
    /**
     * A logical renaming of pull();
     */
    public void refresh(){
        pull();
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

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true); // sets the refresh animation
        pull();
        swipeRefreshLayout.setRefreshing(false);
    }
}
