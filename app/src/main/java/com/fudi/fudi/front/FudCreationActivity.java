package com.fudi.fudi.front;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.TestDatabase;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;

public class FudCreationActivity extends AppCompatActivity {

    private Space flexSpace; //space is expanded on start, until a pic is taken or uploaded
    private ImageView imageContainer;
    private FrameLayout frame;
    private LinearLayout main;
    private EditText dish;
    private EditText restaurant;
    private EditText cost;
    private EditText desc;
    private Button submitButton;
    private Uri imagePath;

    private LocationManager locationManager;
    private Location location;

    private MinMaxTextWatcher<EditText> dmmtw;
    private MinMaxTextWatcher<EditText> rmmtw;
    private MinMaxTextWatcher<EditText> demmtw;

    private TextView progress;
    private ImageHandler.UploadImageTask uit;
    private boolean loadingAgain = false;

    /**
     * This variable is only called when an image has been set.
     */
    private boolean flexSpaceShrunkAndMainShown;

    public static final int IMAGE_CAPTURE_SUCCESS = 1000;
    public static final int IMAGE_UPLOAD_SUCCESS = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fud_creation);

        locationManager = FudiApp.getInstance().FudiLocationManager(getApplicationContext());

        FudiApp.getInstance().checkLocationPermission(FudCreationActivity.this);

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        flexSpaceShrunkAndMainShown = false;

        frame = (FrameLayout) findViewById(R.id.new_fud_frame);
        flexSpace = (Space) findViewById(R.id.new_fud_flex_space);
        main = (LinearLayout) findViewById(R.id.new_fud_main);
        imageContainer = (ImageView) findViewById(R.id.new_fud_image_preview);

        //Text entry listeners
        dish = (EditText) findViewById(R.id.new_fud_dish_entry);
        dmmtw = new MinMaxTextWatcher<EditText>(dish, getResources().getInteger(R.integer.entry_dish_min_char),
                getResources().getInteger(R.integer.entry_dish_max_char),this);
        dish.addTextChangedListener(dmmtw);

        restaurant = (EditText) findViewById(R.id.new_fud_restaurant_entry);
        rmmtw = new MinMaxTextWatcher<EditText>(restaurant, getResources().getInteger(R.integer.entry_restaurant_min_char),
                getResources().getInteger(R.integer.entry_restaurant_max_char),this);
        restaurant.addTextChangedListener(rmmtw);

        cost = (EditText) findViewById(R.id.new_fud_cost_entry);
        cost.setText(FudiApp.currencyFormat.format(0.01));
        cost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TextView costText = (TextView) v;
                String costS = costText.getText().toString();
                if(!hasFocus){
                    costS = costS.replace(FudiApp.currencyFormat.getCurrency().getSymbol(),"");
                    if(costS.equals("")){
                        costS = "0.01";
                    }
                    if(costS.lastIndexOf(".") == costS.length() - 1){
                        costS = costS.replaceAll(".","");
                    }
                    if(costS.indexOf(".") == 0){
                        String costSub = costS.substring(1,costS.length());
                        costSub = costSub.replaceAll(".","");
                        costS = 0 + "." + costSub;
                    }
                    Double cost = Double.parseDouble(costS);
                    NumberFormat nf = FudiApp.currencyFormat;
                    nf.setMinimumFractionDigits(2);
                    nf.setMaximumFractionDigits(2);
                    if(cost < 0.01){
                        cost = 0.01;
                    }
                    if(cost > 9999.99) {
                        cost = 9999.99;
                    }
                    costText.setText(nf.format(cost));
                } else {
                    costText.setText("");
                }
            }
        });

        desc = (EditText) findViewById(R.id.new_fud_desc_text);
        demmtw = new MinMaxTextWatcher<EditText>(desc, getResources().getInteger(R.integer.entry_desc_min_char),
                getResources().getInteger(R.integer.entry_desc_max_char),this);
        desc.addTextChangedListener(demmtw);

        //Image Take Button
        ImageButton takePic = (ImageButton) findViewById(R.id.new_fud_take_button);
        takePic.setOnClickListener(new TakePicOnClickListener());

        //Image Load Button
        ImageButton loadPic = (ImageButton) findViewById(R.id.new_fud_load_button);
        loadPic.setOnClickListener(new LoadPicOnClickListener());

        //Submit Button
        submitButton = (Button) findViewById(R.id.new_fud_submit_button);
        submitButton.setOnClickListener(new SubmitOnClick());

        Intent shared = getIntent();
        Uri data = shared.getParcelableExtra(Intent.EXTRA_STREAM);
        // Figure out what to do based on the intent type
        if (shared != null) {
            boolean itDoes = false;
            try{
                itDoes = shared.getType().contains("image/");
            } catch (NullPointerException e) {}
            if (itDoes) {
                if (data == null) {
                    imageFail();
                    return;
                }
                imagePath = data;
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data);
                    ImageHandler.getInstance().scaleBitmapForImageView(b, imageContainer,
                            imageContainer.getWidth(), ImageHandler.pfdp(250, this),
                            getResources().getInteger(R.integer.default_fud_creation_preview_quality));
                } catch (Exception e) {
                    e.printStackTrace();
                    imageFail();
                    return;
                }
                flexSpaceShrunkAndMainShown = true;
                flexSpace.setVisibility(View.GONE);
                main.setVisibility(View.VISIBLE);
                dish.requestFocus();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fud_creation, menu);
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
        } else if (id == R.id.fudi_action_back) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap b = null;
        try {
            if(resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_SUCCESS) {
                Uri fileLocation = imagePath;
                b = ImageHandler.decodeSampledBitmapUri(fileLocation, imageContainer.getWidth(),
                        ImageHandler.pfdp(250, this));
                imageContainer.setImageBitmap(b);
            } else if(resultCode == RESULT_OK && requestCode == IMAGE_UPLOAD_SUCCESS){
                Uri uri = data.getData();
                AssetFileDescriptor fileDescriptor =null;
                fileDescriptor =
                        getContentResolver().openAssetFileDescriptor(uri, "r");
                b = ImageHandler.decodeSampledBitmapFD(fileDescriptor.getFileDescriptor(),
                        imageContainer.getWidth(), ImageHandler.pfdp(250, this));

                /*
                b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ImageHandler.getInstance().scaleBitmapForImageView(b, imageContainer,
                        imageContainer.getWidth(), ImageHandler.pfdp(250, this));*/

                imageContainer.setImageBitmap(b);
            } else {
                imageFail();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flexSpaceShrunkAndMainShown = false;
            flexSpace.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
            imageFail();
            return;
        }

        //success
        flexSpaceShrunkAndMainShown = true;
        flexSpace.setVisibility(View.GONE);
        main.setVisibility(View.VISIBLE);
        dish.requestFocus();
    }

    @Override
    public void onBackPressed() {
        System.gc();
        finish();
    }

    @Override
    protected void onDestroy() {
        imageContainer.setImageBitmap(null);
        View v = findViewById(R.id.new_fud_frame);
        ImageHandler.unbindDrawables(v);
        v.invalidate();
        System.gc();
        super.onDestroy();
    }

    private void imageFail(){
        Toast.makeText(this,"Could not load photo.",Toast.LENGTH_LONG).show();
    }

    private void endFail(){
        setResult(RESULT_CANCELED);
        finish();
    }

    private class TakePicOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            boolean fail = false;
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = ImageHandler.createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    fail = true;
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    if(imagePath != null){
                        imageContainer.setImageBitmap(null);
                    }
                    imagePath = Uri.fromFile(photoFile);
                    startActivityForResult(intent, IMAGE_CAPTURE_SUCCESS);
                } else {
                    fail = true;
                }
            } else {
                Toast.makeText(FudCreationActivity.this, "You have no available Camera.", Toast.LENGTH_LONG).show();
                fail = true;

            }
            if(fail){
                FudCreationActivity.this.setResult(RESULT_CANCELED);
                FudCreationActivity.this.finish();
            }

        }
    }



    private class LoadPicOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(!loadingAgain){
                loadingAgain = true;
            } else {
                imageContainer.setImageBitmap(null);
            }

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), IMAGE_UPLOAD_SUCCESS);

        }
    }

    private class SubmitOnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FudiApp.hideSoftKeyboard(FudCreationActivity.this);
            if(rmmtw.isGood() && dmmtw.isGood() && flexSpaceShrunkAndMainShown){
                //Get popup for loading
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout popup = (FrameLayout) getLayoutInflater().inflate(R.layout.processing_popup, null);
                        frame.addView(popup);
                        progress = (TextView) popup.findViewById(R.id.processing_popup_text);
                    }
                });

                (new AsyncTask<Void, Void, String>() {

                    Intent intent = new Intent();
                    String dishName = dish.getText().toString();
                    String restName = restaurant.getText().toString();
                    String costText = cost.getText().toString();
                    String descText = desc.getText().toString();
                    int which = 0;


                    @Override
                    protected void onPreExecute() {
                        uit = ImageHandler.getInstance().uploadImageToDatabase(
                                FudCreationActivity.this, imageContainer);
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        //First, upload the image to the image server
                        String s = FudiApp.currencyFormat.getCurrency().getSymbol();
                        if(!costText.contains(s)){
                            costText = s + costText;
                        }

                        while(uit == null){
                            publishProgress();
                            try {
                                synchronized (this) {
                                    this.wait(10);
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }

                        while(uit.getURLUploadedTo() == null){
                            if (uit.isCancelled()) {
                                endFail();
                            }
                            publishProgress();
                            try {
                                synchronized (this) {
                                    this.wait(100);
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }

                        ;
                        String imageURL = uit.getURLUploadedTo();
                        return imageURL;
                    }

                        @Override
                    protected void onProgressUpdate(Void... values) {
                        /*TODO, make a ProgressView class and make this a method...*/
                        String[] toWrite = {"\uD83C\uDF5C"};
                        progress.setText(toWrite[which]);
                        which = ((which + 1) % toWrite.length);
                    }

                    @Override
                    protected void onPostExecute(String imageURL) {
                        //Construct FudDetail
                        if (imageURL == null) {
                            endFail();
                        }

                       final FudDetail fudDetail = new FudDetail(imageURL, dishName, restName, costText,
                                descText, FudiApp.getInstance().getThisUser(), "");

                        //Used for testing Yelp
                        fudDetail.setLocationPosted(location);
                        fudDetail.setLocationOfRestaurant(fudDetail.getRestaurant());

                        //Upload the FudDetail to the database
                        /**
                         * TODO: upload data to database if it fails, return the appropriate code
                         *
                         * Efficiency, delete image from database as well. (Optional for now!)
                         *
                         * Uncomment later (and get rid of TestDatabase reference):
                         * fudDetail.push();
                         *
                         */

                        (new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                FudiApp.getInstance().pushFudDetail(fudDetail);
                                return null;
                            }

                        }).execute();
                        TestDatabase.getInstance().put(fudDetail.getFudID(), fudDetail);

                        setResult(MainActivity.RESULT_OK);
                        finish();
                    }
                }).execute();

            } else {
                Toast.makeText(FudCreationActivity.this, "Forgot to enter something? :)", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
