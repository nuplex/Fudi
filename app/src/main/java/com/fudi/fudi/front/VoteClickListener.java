package com.fudi.fudi.front;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

    public VoteClickListener(Vote vote, Vote.Type type, ImageButton otherVote,
                             TextView voteText, Boolean ovp){
        this.voteText = voteText;
        this.type = type;
        this.vote = vote;
        this.oneVotePressed = ovp;
        this.otherVote = otherVote;

    }

    @Override
    public void onClick(View v) {
        ImageButton voteButton = (ImageButton) v;
        if(!oneVotePressed){
            switch(type){
                case UPFU:
                    vote.vote(Vote.Type.UPFU);
                    voteText.setText(Long.toString(vote.getNet()));
                    break;
                case DOWNFU:
                    vote.vote(Vote.Type.DOWNFU);
                    voteText.setText(Long.toString(vote.getNet()));
                    break;
            }
            voteButton.setEnabled(false);
            oneVotePressed = true;
        } else {
            vote.undo(type);
            voteText.setText(Long.toString(vote.getNet()));
            voteButton.setEnabled(true);
            otherVote.setClickable(true);
        }
    }
}