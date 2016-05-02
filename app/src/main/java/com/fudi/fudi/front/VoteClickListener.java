package com.fudi.fudi.front;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Comment;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.Vote;

/**
 * This processes (only on the UI) vote clicking.
 * Created by chijioke on 4/23/16.
 */
public class VoteClickListener implements View.OnClickListener {

    private Vote.Type type;
    private TextView voteText;
    private Vote vote;
    private boolean oneVotePressed;
    private ImageButton otherVote;
    private Comment comment;
    private String fudID;
    private String userID;

    public VoteClickListener(Vote vote, Vote.Type type, ImageButton otherVote,
                             TextView voteText, boolean ovp, Comment comment,
                             String fudID, String userID){
        this.voteText = voteText;
        this.type = type;
        this.vote = vote;
        this.oneVotePressed = ovp;
        this.otherVote = otherVote;
        this.comment = comment;
        this.fudID = fudID;
        this.userID = userID;


    }

    @Override
    public void onClick(View v) {
        ImageButton voteButton = (ImageButton) v;
        if(!oneVotePressed){
            switch(type){
                case UPFU:
                    vote.vote(Vote.Type.UPFU);
                    voteText.setText(Long.toString(vote.getNet()));
                    if(isComment()) {
                        voteButton.setBackgroundResource(R.drawable.fudi_up_arrow_comment_pressed);
                    } else {
                        voteButton.setBackgroundResource(R.drawable.fudi_up_arrow_pressed);
                    }
                    break;
                case DOWNFU:
                    vote.vote(Vote.Type.DOWNFU);
                    if(isComment()) {
                        voteButton.setBackgroundResource(R.drawable.fudi_up_down_comment_pressed);
                    } else {
                        voteButton.setBackgroundResource(R.drawable.fudi_down_arrow_pressed);
                    }
                    voteText.setText(Long.toString(vote.getNet()));
                    break;
            }
            otherVote.setClickable(false);
            oneVotePressed = true;
        } else {
            vote.undo(type);
            switch(type){
                case UPFU:
                    if(isComment()) {
                        voteButton.setBackgroundResource(R.drawable.fudi_up_arrow_comment);
                    } else {
                        voteButton.setBackgroundResource(R.drawable.fudi_up_arrow);
                    }
                    break;
                case DOWNFU:
                    if(isComment()) {
                        voteButton.setBackgroundResource(R.drawable.fudi_down_arrow_comment);
                    } else {
                        voteButton.setBackgroundResource(R.drawable.fudi_down_arrow);
                    }
                    break;
            }
            oneVotePressed = false;
            otherVote.setClickable(true);
            voteText.setText(Long.toString(vote.getNet()));
        }
    }

    public boolean isComment(){
        return comment != null;
    }

    //TODO, this, details on the task stack

    /**
     * Pushes vote changes to the databse.
     */
    public void push(){}
}