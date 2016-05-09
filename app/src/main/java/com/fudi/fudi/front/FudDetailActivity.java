package com.fudi.fudi.front;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Comment;
import com.fudi.fudi.back.CommentSection;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.GeneralComment;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.NotificationService;
import com.fudi.fudi.back.ReviewComment;
import com.fudi.fudi.back.Vote;
import com.yelp.clientlib.entities.Business;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

public class FudDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String SERVER_LOCATION = "https://fudiapp.firebaseio.com/";

    private TreeSet<CommentView> comments;
    private LinearLayout commentList;
    private FudDetail fudDetail;
    private FrameLayout mainframe;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FrameLayout popupChooser;
    private FrameLayout popupAddComment;
    private FrameLayout popupProcessing;
    private FrameLayout addReviewFrame;
    private FrameLayout addCommentFrame;
    private ImageView reviewCommentImage;

    private boolean loadingAgain = false;
    private boolean imageSet = false;

    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fud_detail);
        Firebase.setAndroidContext(getApplicationContext());

        String fudID = getIntent().getStringExtra(Fud.EXTRA_TAG_ID);
        Log.i("FudDetailActivity", fudID);

        comments = new TreeSet<CommentView>();
        if(getIntent().hasExtra(NotificationView.FROM_NOTIFICATION)){
            Log.i("FudDetailActivity", "Calling pullFudDetail");
            //FudDetail fd = FudiApp.getInstance().pullFudDetail(fudID);
            fudDetail = NotificationService.notifyFud;
            if(fudDetail == null){
                Toast.makeText(this, "There was an error getting the fud :(", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            for (FudDetail fd : FudiApp.getInstance().getCurrentlyDisplayedFudDetails()) {
                if (fd.getFudID().equals(fudID)) {
                    fudDetail = fd;
                    break;
                }
            }
        }

        Vote vote = fudDetail.getVote();
        boolean oneVotePressed = false; //TODO; this actually needs to be gotten from the database :( or stored in the comment
        mainframe = (FrameLayout) findViewById(R.id.fud_detail_frame_layout);


        TextView dish = (TextView) findViewById(R.id.fud_detail_dish_title);
        dish.setText(fudDetail.getDishName());

        TextView restaurant = (TextView) findViewById(R.id.fud_detail_restaurant);
        restaurant.setText(fudDetail.getRestaurant());

        TextView cost = (TextView) findViewById(R.id.fud_detail_cost);
        cost.setText(fudDetail.getCost());

        TextView username = (TextView) findViewById(R.id.fud_detail_username);
        username.setText(fudDetail.getWhoPosted().getUsername());

        TextView time = (TextView) findViewById(R.id.fud_detail_time);
        time.setText(FudiApp.getFormattedTime(fudDetail.getTimestamp()));

        TextView text = (TextView) findViewById(R.id.fud_detail_desc_text);
        text.setText(fudDetail.getDescription());

        TextView netVote = (TextView)  findViewById(R.id.fud_detail_netvote);
        netVote.setText(Long.toString(vote.getNet()));

        //Set button actions
        ImageButton upvoteButton = (ImageButton) findViewById(R.id.fud_detail_upvote_button);
        ImageButton downvoteButton = (ImageButton) findViewById(R.id.fud_detail_downvote_button);
        ImageButton yelpButton = (ImageButton) findViewById(R.id.fud_detail_yelp_button);

        upvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.UPFU, downvoteButton,
                netVote, oneVotePressed, null, null, fudDetail,
                fudDetail.getWhoPosted().getUserID()));
        downvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.DOWNFU,
                upvoteButton, netVote, oneVotePressed, null, null,
                fudDetail, fudDetail.getWhoPosted().getUserID()));
        yelpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Business yelpBis = fudDetail.getYelpBusiness();
                if (yelpBis != null) {
                    Uri yelpSite = Uri.parse(yelpBis.mobileUrl());
                    if (yelpSite != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, yelpSite);
                        Log.i("Yelp", "Trying to go to URL");
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Business Not found on Yelp Mobile",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //Load in the image
        ImageView image = (ImageView) findViewById(R.id.fud_detail_dish);
        ImageHandler.getInstance().loadImageIntoImageView(this,image,fudDetail.getImageURL());

        //Comment Handling
        commentList = (LinearLayout) findViewById(R.id.fud_detail_comment_section);

        fudDetail.setLocationOfRestaurant(fudDetail.getRestaurant());
        pull();


        //Comment Button Handling
        ImageButton addCommentButton = (ImageButton) findViewById(R.id.fudi_detail_add_comment_button);
        addCommentButton.setOnClickListener(new AddCommentOnClickListener());

        /* TODO Much like in the Main activity, implement a pull down top of screen refresh
              function for this activity. On refresh you will need to pull the data for this
              comment section again, and update the comment section sturctures so that it displays
              the new data.

         */


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
    }


    @Override
    public void onBackPressed() {
        if(popupChooser != null) {
            mainframe.removeView(popupChooser);
        } else if (popupAddComment != null) {
            popupAddComment.removeView(popupAddComment);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fud_detail, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onPause(){
        super.onPause();
        commentList.removeAllViews();
        comments.clear();
        System.gc();
    }

    public boolean networkCheck(){
        if(!FudiApp.hasNetworkConnection()){
            commentList.removeAllViews();
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
            commentList.addView(noNetwork);
            return false;
        } else {
            return true;
        }
    }

    private void pull(){
        convertAll(fudDetail.getCommentSection().getComments());
        constructCommentSection();
    }

    private void refresh(){
        commentList.removeAllViews();
        comments.clear();
        System.gc();
        pull();
    }

    private void convert(Comment c){
        if(c instanceof ReviewComment){
            comments.add(new ReviewCommentView(this, (ReviewComment) c));
        } else {
            comments.add(new CommentView(this,(GeneralComment) c));
        }
    }

    private void convertAll(TreeSet<Comment> comms){
        for(Comment c : comms){
            convert(c);
        }
    }

    private void add(CommentView cv){
        if(!comments.contains(cv)) {
            comments.add(cv);
        }
        updateCommentSection();
    }

    private void updateCommentSection(){
        commentList.removeAllViews();
        CommentSection cs = fudDetail.getCommentSection();
        if(cs == null){
            Log.e("ERROR", "Comment section for " + fudDetail.getFudID() + " was null");
            cs = new CommentSection();
        }
        convertAll(cs.getComments());
        constructCommentSection();
    }

    private void constructCommentSection(){
        for(CommentView cv : comments) {
            View divider = getLayoutInflater().inflate(R.layout.comment_section_divider, null);
            commentList.addView(divider);
            commentList.addView(cv.getView());
        }
    }

    @Override
    protected void onDestroy() {
        View v = findViewById(R.id.fud_detail_frame_layout);
        ImageHandler.unbindDrawables(v);
        v.invalidate();
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            Bitmap b = null;
            try {
                if(resultCode == RESULT_OK &&
                        requestCode == FudCreationActivity.IMAGE_CAPTURE_SUCCESS) {
                    Uri fileLocation = imagePath;
                    b = ImageHandler.decodeSampledBitmapUri(fileLocation, reviewCommentImage.getWidth(),
                            ImageHandler.pfdp(150, this));
                    reviewCommentImage.setImageBitmap(b);
                } else if(resultCode == RESULT_OK &&
                        requestCode == FudCreationActivity.IMAGE_UPLOAD_SUCCESS){
                    Uri uri = data.getData();
                    AssetFileDescriptor fileDescriptor =null;
                    fileDescriptor =
                            getContentResolver().openAssetFileDescriptor(uri, "r");
                    b = ImageHandler.decodeSampledBitmapFD(fileDescriptor.getFileDescriptor(),
                            reviewCommentImage.getWidth(), ImageHandler.pfdp(250, this));

                /*
                b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ImageHandler.getInstance().scaleBitmapForImageView(b, imageContainer,
                        imageContainer.getWidth(), ImageHandler.pfdp(250, this));*/

                    reviewCommentImage.setImageBitmap(b);
                } else {
                    imageFail();
                    return;
                }
            } catch (Exception e) {
                imageFail();
                return;
            }

            if (b == null) {
                imageFail();
                return;
            }

            imageSet = true;
        }
    }

    private void imageFail(){
        Toast.makeText(this, "Unable to load image.", Toast.LENGTH_LONG).show();
    }

    private void submitFail(){
        Toast.makeText(this, "There was a probelm :(", Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        refresh();
        swipeRefreshLayout.setRefreshing(false);
    }


    private class AddCommentOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            FrameLayout choiceFrame = (FrameLayout)
                    getLayoutInflater().inflate(R.layout.add_comment_chooser_popup, null);
            mainframe.addView(choiceFrame);
            popupChooser = choiceFrame;

            //Reiew Button
            Button reviewButton =  (Button) mainframe.findViewById(R.id.comment_chooser_popup_review_button);
            reviewButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    addReviewFrame = (FrameLayout)
                            getLayoutInflater().inflate(R.layout.add_review_comment_popup, null);
                    mainframe.addView(addReviewFrame);
                    mainframe.removeView(popupChooser);
                    popupChooser.invalidate();
                    popupChooser = null;
                    System.gc();
                    popupAddComment = addReviewFrame;

                    final EditText edit = (EditText) addReviewFrame.findViewById(R.id.review_comment_popup_entry);
                    final MinMaxTextWatcher<EditText> emmtw =
                            new MinMaxTextWatcher<EditText>(edit,40,512,FudDetailActivity.this);
                    edit.addTextChangedListener(emmtw);

                    final ImageView image = (ImageView) addReviewFrame.findViewById(R.id.review_comment_popup_image);
                    reviewCommentImage = image;

                    ImageButton cancel = (ImageButton) addReviewFrame.findViewById(R.id.review_comment_popup_cancel);
                    cancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FudiApp.hideSoftKeyboard(FudDetailActivity.this);
                            addReviewFrame.setVisibility(View.GONE);
                            mainframe.removeView(addReviewFrame);
                            popupAddComment = null;
                        }
                    });

                    ImageButton upload = (ImageButton) addReviewFrame.findViewById(R.id.review_comment_popup_take);
                    upload.setOnClickListener(new TakePicOnClickListener());
                    ImageButton take  = (ImageButton) addReviewFrame.findViewById(R.id.review_comment_popup_upload);
                    take.setOnClickListener(new LoadPicOnClickListener());

                    ImageButton submit = (ImageButton) addReviewFrame.findViewById(R.id.review_comment_popup_submit);
                    submit.setOnClickListener(new SubmitReviewOnClick(emmtw, edit, image));
                }
            });
            reviewButton.setEnabled(false); //TODO fix this


            //General Button
            Button generalButton =  (Button) mainframe.findViewById(R.id.comment_chooser_popup_general_button);
            generalButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    addCommentFrame = (FrameLayout)
                            getLayoutInflater().inflate(R.layout.add_comment_popup, null);
                    mainframe.addView(addCommentFrame);
                    mainframe.removeView(popupChooser);
                    popupChooser = null;
                    System.gc();
                    popupAddComment = addCommentFrame;

                    final EditText edit = (EditText) addCommentFrame.findViewById(R.id.comment_popup_entry);
                    final MinMaxTextWatcher<EditText> emmtw =
                            new MinMaxTextWatcher<EditText>(edit,1,256,FudDetailActivity.this);
                    edit.addTextChangedListener(emmtw);

                    ImageButton cancel = (ImageButton) addCommentFrame.findViewById(R.id.comment_popup_cancel);
                    cancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FudiApp.hideSoftKeyboard(FudDetailActivity.this);
                            addCommentFrame.setVisibility(View.GONE);
                            mainframe.removeView(addCommentFrame);
                            popupAddComment = null;
                            addCommentFrame = null;
                        }
                    });

                    ImageButton submit = (ImageButton) addCommentFrame.findViewById(R.id.comment_popup_submit);
                    submit.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            if(emmtw.isGood()){
                                FudiApp.hideSoftKeyboard(FudDetailActivity.this);
                                String text = edit.getText().toString();
                                //Get popup for loading
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        popupProcessing =
                                                (FrameLayout) getLayoutInflater().inflate(R.layout.processing_popup, null);
                                        mainframe.addView(popupProcessing);
                                    }
                                });

                                final GeneralComment comment = new GeneralComment(text,
                                        FudiApp.getInstance().getThisUser(),
                                        fudDetail.getCommentSection());
                                add(new CommentView(FudDetailActivity.this, comment));
                                (new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        FudiApp.getInstance().pushCommentForFudDetail(fudDetail.getFudID(),comment);
                                        return null;
                                    }

                                }).execute();

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainframe.removeView(popupProcessing);
                                        addCommentFrame.setVisibility(View.GONE);
                                        mainframe.removeView(addCommentFrame);
                                        addCommentFrame = null;
                                        popupProcessing = null;
                                    }
                                });

                                /*TODO just do CommentSection.pushComment(the comment) when db works
                                 */
                            } else {
                                Toast.makeText(FudDetailActivity.this, "It can't be blank!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

    }

    private class TakePicOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FudiApp.hideSoftKeyboard(FudDetailActivity.this);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            boolean fail = false;
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = ImageHandler.createImageFile();
                } catch (IOException ex) {
                    fail = true;
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    if(imagePath != null){
                        imagePath = null;
                        System.gc();
                    }
                    imagePath = Uri.fromFile(photoFile);
                    startActivityForResult(intent, FudCreationActivity.IMAGE_CAPTURE_SUCCESS);
                } else {
                    fail = true;
                }
            } else {
                Toast.makeText(FudDetailActivity.this, "You have no available Camera.", Toast.LENGTH_LONG).show();
                fail = true;

            }
            if(fail){
                imageFail();
            }

        }
    }

    private class LoadPicOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FudiApp.hideSoftKeyboard(FudDetailActivity.this);
            if(!loadingAgain){
                loadingAgain = true;
            } else {
                reviewCommentImage.setImageBitmap(null);
            }
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), FudCreationActivity.IMAGE_UPLOAD_SUCCESS);

        }
    }

    private class SubmitReviewOnClick implements View.OnClickListener{

        private MinMaxTextWatcher<EditText> emmtw;
        private EditText edit;
        private ImageView image;

        public SubmitReviewOnClick(MinMaxTextWatcher<EditText> mmtw, EditText editText,
                                   ImageView image){
            emmtw = mmtw;
            edit = editText;
            this.image = image;
        }

        @Override
        public void onClick(View v) {
            FudiApp.hideSoftKeyboard(FudDetailActivity.this);
            if(emmtw.isGood()) {
                if (!imageSet) {
                    Toast.makeText(FudDetailActivity.this, "What about the image!?",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        popupProcessing =
                                (FrameLayout) getLayoutInflater().inflate(R.layout.processing_popup, null);
                        mainframe.addView(popupProcessing);
                        //First, upload the image to the image server

                    }
                });

                (new AsyncTask<Void, Void, String>() {

                    private ImageHandler.UploadImageTask uit;

                    @Override
                    protected void onPreExecute() {
                        uit = ImageHandler.getInstance().uploadImageToDatabase(
                                FudDetailActivity.this, image);
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        while (uit == null) {
                            publishProgress();
                            try {
                                synchronized (this) {
                                    this.wait(10);
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }

                        while (uit.getURLUploadedTo() == null) {
                            if (uit.isCancelled()) {
                                submitFail();
                            }
                            publishProgress();
                            try {
                                synchronized (this) {
                                    this.wait(300);
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }

                        String imageURL = uit.getURLUploadedTo();
                        return imageURL;
                    }

                    @Override
                    protected void onProgressUpdate(Void... values) {
                        super.onProgressUpdate(values);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        String imageURL = uit.getURLUploadedTo();
                        if (imageURL == null) {
                            submitFail();
                        }
                        ReviewComment review = new ReviewComment(edit.getText().toString(),
                                imageURL,
                                ReviewComment.Rating.GREAT, FudiApp.getInstance().getThisUser(),
                                fudDetail.getCommentSection());
                        add(new ReviewCommentView(FudDetailActivity.this, review));
                        FudiApp.getInstance().pushCommentForFudDetail(fudDetail.getFudID(), review);

                        mainframe.removeView(popupProcessing);
                        addReviewFrame.setVisibility(View.GONE);
                        mainframe.removeView(addReviewFrame);
                        popupProcessing = null;
                        addReviewFrame = null;
                    }

                }).execute();

            }else {
                    Toast.makeText(FudDetailActivity.this, "You forgot the review!",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

}
