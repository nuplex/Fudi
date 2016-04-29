package com.fudi.fudi.front;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fudi.fudi.R;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.GeneralComment;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.ReviewComment;
import com.fudi.fudi.back.Vote;

/**
 * Represents the ReviewComment object in a View.
 * Created by chijioke on 4/23/16.
 */
public class ReviewCommentView extends CommentView {

    private LinearLayout commentView;
    private Context context;
    private ReviewComment comment;
    private Vote vote;
    private boolean oneVotePressed;

    public ReviewCommentView(Context context, ReviewComment comment){
        super(context);
        this.context = context;
        this.comment = comment;
        this.vote = comment.getVote();
        oneVotePressed = false; //TODO; this actually needs to be gotten from the database :( or stored in the comment

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        commentView = (LinearLayout) inflater.inflate(R.layout.review_comment, null);

        TextView username = (TextView) commentView.findViewById(R.id.review_comment_main_username);
        username.setText(comment.getWhoPosted().getUsername());

        TextView time = (TextView) commentView.findViewById(R.id.review_comment_time);
        time.setText(FudiApp.getTimeSincePostedString(comment.getTimestamp()));

        TextView rating = (TextView) commentView.findViewById(R.id.review_comment_rating_text);
        rating.setText("Okay");

        TextView text = (TextView) commentView.findViewById(R.id.review_comment_main_text);
        text.setText(comment.getText());

        TextView netVote = (TextView)  commentView.findViewById(R.id.review_comment_vote_netvote);
        netVote.setText(Long.toString(vote.getNet()));

        //Set button actions
        ImageButton upvoteButton = (ImageButton) commentView.findViewById(R.id.review_comment_vote_upvote);
        ImageButton downvoteButton = (ImageButton) commentView.findViewById(R.id.review_comment_vote_downvote);

        upvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.UPFU, downvoteButton,
                netVote, oneVotePressed));

        downvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.DOWNFU, upvoteButton,
                netVote, oneVotePressed));

        //Load in the image
        ImageView image = (ImageView) commentView.findViewById(R.id.review_comment_image);
        ImageHandler.getInstance().loadImageIntoImageView(context,image,comment.getProofImageURL());
    }

    /**
     * This MUST be called when physically adding a view with {@link android.view.ViewGroup} addView();
     * @return the layout for this view
     */
    @Override
    public LinearLayout getView(){
        return commentView;
    }

}
