package com.fudi.fudi.front;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.User;

public class LoginActivity extends AppCompatActivity {

    private static boolean firstTimeEver = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(getApplicationContext());

        Button register = (Button) findViewById(R.id.login_register_button);
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        String firstTime = sharedPref.getString("firstTime", "false");
        if(firstTime.equals("false")){
            register.setText("Login");
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {

            //TODO: implement registration
        }

        Button justContinue = (Button) findViewById(R.id.login_just_continue_button);
        justContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void goToMain(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        ImageHandler.unbindDrawables(findViewById(R.id.login_frame));
        findViewById(R.id.login_frame).invalidate();
        System.gc();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
