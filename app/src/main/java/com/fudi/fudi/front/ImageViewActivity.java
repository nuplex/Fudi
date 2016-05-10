package com.fudi.fudi.front;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.fudi.fudi.R;
import com.fudi.fudi.back.ImageHandler;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        if(getIntent() == null){
            Toast.makeText(this, "There was an error loading your image.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String imageURL = getIntent().getStringExtra("imageURL");

        ImageView image = (ImageView) findViewById(R.id.imageview_image);
        ImageHandler.getInstance().loadImageIntoImageView(this, image, imageURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_view, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
