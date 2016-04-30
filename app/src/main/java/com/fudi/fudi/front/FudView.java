package com.fudi.fudi.front;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.Image;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.Vote;

import java.util.Comparator;

/**
 * The View Object for a Fud Object
 * Created by chijioke on 4/20/16.
 */
public class FudView extends View implements Comparable<FudView> {

    private LinearLayout fudView;
    private Fud fud;
    private Vote vote;
    private boolean oneVotePressed;
    private Context context;
    private ImageView image;
    private boolean imageLoaded;

    public FudView(Context context, Fud fud) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        fudView = (LinearLayout) inflater.inflate(R.layout.fud_post, null);
        init(context, fud);
    }

    public FudView(Context context, ViewGroup parent, Fud fud){
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        fudView =(LinearLayout) inflater.inflate(R.layout.fud_post, parent, false);
        init(context, fud);
    }

    public LinearLayout getView(){
        return fudView;
    }

    /**
     * Copies data from one FudView to the other
     * @param view The FudView that will have it's data replaced with the current one.
     * @return the modified FudView
     */
    public FudView copyTo(FudView view, Context context){
        view.init(context, getFud());
        view.oneVotePressed = oneVotePressed;
        return view;
    }

    private void init(Context context, Fud fud){
        this.fud = fud;
        oneVotePressed = false;
        this.context = context;
        this.vote = fud.getVote();

        FudOnClickListener focl = new FudOnClickListener();

        imageLoaded = false;

        //Set visible elements
        image = (ImageView) fudView.findViewById(R.id.fud_post_image);

        TextView dishTitle = (TextView) fudView.findViewById(R.id.fud_post_dishtitle);
        dishTitle.setText(fud.getDishName());

        TextView restaurant = (TextView) fudView.findViewById(R.id.fud_post_restaurant);
        restaurant.setText(fud.getRestaurant());

        TextView netVote = (TextView) fudView.findViewById(R.id.fud_post_netvote_text);
        netVote.setText(Long.toString(vote.getNet()));

        TextView time = (TextView) fudView.findViewById(R.id.fud_post_time);
        time.setText(fud.getTimeSincePostedString());

        TextView cost = (TextView) fudView.findViewById(R.id.fud_post_cost);
        cost.setText(fud.getCost());

        //Set button actions
        ImageButton upvoteButton = (ImageButton) fudView.findViewById(R.id.fud_post_upvote_button);
        ImageButton downvoteButton = (ImageButton) fudView.findViewById(R.id.fud_post_downvote_button);

        upvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.UPFU, downvoteButton,
                netVote, oneVotePressed, null, fud.getFudID(),
                fud.getUserID()));
        downvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.DOWNFU,
                upvoteButton, netVote, oneVotePressed, null,
                fud.getFudID(), fud.getUserID()));

        //Set comment button
        Button commentButton = (Button) fudView.findViewById(R.id.fud_post_comment_button);
        commentButton.setText(Integer.toString(fud.getComments()));

        //Set areas where clicking opens a FudDetail
        LinearLayout main = (LinearLayout) fudView.findViewById(R.id.fud_post_main);
        main.setOnClickListener(focl);
        commentButton.setOnClickListener(focl);
    }

    public Fud getFud(){
        return fud;
    }

    public void loadImage(){
        ImageHandler.getInstance().loadImageIntoImageView(context, image, fud.getImageURL());
        imageLoaded = true;
    }

    public void unloadImage(){
        image.setImageDrawable(null);
        System.gc();
        imageLoaded = false;
    }

    public boolean imageIsLoaded(){
        return imageLoaded;
    }



    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        } else if(!(o instanceof  FudView)){
            return false;
        } else {
            return fud.getFudID().equals(((FudView) o).fud.getFudID());
        }
    }

    @Override
    public int compareTo(FudView another) {
        return (new FudViewTimeComparator()).compare(this, another);
    }

    private class FudOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = fud.pass();
            intent.setClass(FudView.this.context, FudDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            context.startActivity(intent);
        }
    }

    private class FudViewTimeComparator implements Comparator<FudView>{

        @Override
        public int compare(FudView lhs, FudView rhs) {
            Fud flhs = lhs.fud;
            Fud frhs = rhs.fud;
            return frhs.getTimestamp().compareTo(flhs.getTimestamp());
        }
    }

}
