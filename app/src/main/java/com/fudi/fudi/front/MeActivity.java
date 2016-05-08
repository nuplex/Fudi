package com.fudi.fudi.front;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.core.utilities.Tree;
import com.fudi.fudi.R;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.FudiNotification;
import com.fudi.fudi.back.User;

import java.util.TreeSet;

public class MeActivity extends AppCompatActivity{

    LinearLayout infoholder;
    LinearLayout notificationList;
    TreeSet<NotificationView> notificationViews;

    Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        Firebase.setAndroidContext(getApplicationContext());

        infoholder = (LinearLayout) findViewById(R.id.me_info_holder);
        notificationList = (LinearLayout) findViewById(R.id.me_notification_list);
        notificationViews = new TreeSet<NotificationView>();

        register = (Button) findViewById(R.id.me_register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MeActivity.this, RegistrationActivity.class));
                finish();
            }
        });

        if(!FudiApp.getInstance().getThisUser().isRegistered()){
            infoholder.setVisibility(View.GONE);
            register.setVisibility(View.VISIBLE);
            return;
        }

        User me = FudiApp.getInstance().getThisUser();

        TextView username = (TextView) findViewById(R.id.me_username);
        username.setText(me.getUsername());

        TextView fu = (TextView) findViewById(R.id.me_fu);
        fu.setText(Long.toString(me.getFu()));

        Button myFuds = (Button) findViewById(R.id.me_my_fuds_button);
        myFuds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button commentedOn = (Button) findViewById(R.id.me_my_commented_on_button);
        commentedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        pullNotifications();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_me, menu);
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
        } else if (id == R.id.action_location_here){

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotifications();

    }

    private void pullNotifications(){
        if(infoholder.getVisibility() == View.VISIBLE) {
            notificationList.removeAllViews();
            notificationViews.clear();
            notificationViews = convertAll(FudiApp.getInstance().getCurrentOperatingNotifications());
        }
    }


    private void displayNotifcations(){
        for(NotificationView nv :  notificationViews){
            notificationList.addView(nv.getView());
        }
    }

    private void refreshNotifications(){
        pullNotifications();
        displayNotifcations();
    }

    private TreeSet<NotificationView> convertAll(TreeSet<FudiNotification> notifs){
        TreeSet<NotificationView> views = new TreeSet<NotificationView>();
        for(FudiNotification fn : notifs) {
            views.add(new NotificationView(fn, this));
        }

        return views;
    }
}
