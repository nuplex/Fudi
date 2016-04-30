package com.fudi.fudi.front;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fudi.fudi.R;
import com.fudi.fudi.back.Comment;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.GeneralComment;
import com.fudi.fudi.back.ReviewComment;
import com.fudi.fudi.back.Vote;

import java.util.Comparator;

/**
 * Represents the GeneralComment object in a View.
 * Created by chijioke on 4/23/16.
 */
public class CommentView extends View implements Comparable<CommentView>{
    private LinearLayout commentView;
    private Context context;
    private GeneralComment comment;
    private Vote vote;
    private boolean oneVotePressed;

    protected CommentView(Context context){super(context);}

    public CommentView(Context context, GeneralComment comment){
        super(context);
        this.context = context;
        this.comment = comment;
        this.vote = comment.getVote();
        oneVotePressed = false; //TODO; this actually needs to be gotten from the database :( or stored in the comment

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        commentView = (LinearLayout) inflater.inflate(R.layout.general_comment, null);

        TextView username = (TextView) commentView.findViewById(R.id.comment_main_username);
        username.setText(comment.getWhoPosted().getUsername());

        TextView time = (TextView) commentView.findViewById(R.id.comment_time);
        time.setText(FudiApp.getTimeSincePostedString(comment.getTimestamp()));

        TextView text = (TextView) commentView.findViewById(R.id.comment_main_text);
        text.setText(comment.getText());

        TextView netVote = (TextView)  commentView.findViewById(R.id.comment_vote_netvote);
        netVote.setText(Long.toString(vote.getNet()));

        //Set button actions
        ImageButton upvoteButton = (ImageButton) commentView.findViewById(R.id.comment_vote_upvote);
        ImageButton downvoteButton = (ImageButton) commentView.findViewById(R.id.comment_vote_downvote);

        upvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.UPFU, downvoteButton,
                netVote, oneVotePressed, comment, comment.getParent().getParentFud().getFudID(),
                comment.getWhoPosted().getUserID()));
        downvoteButton.setOnClickListener(new VoteClickListener(vote, Vote.Type.DOWNFU,
                upvoteButton, netVote, oneVotePressed, comment,
                comment.getParent().getParentFud().getFudID(), comment.getWhoPosted().getUserID()));
    }

    /**
     * This MUST be called when physically adding a view with {@link android.view.ViewGroup} addView();
     * @return the layout for this view
     */
    public LinearLayout getView(){
        return commentView;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        } else if (!(o instanceof  CommentView)){
            return false;
        } else {
            return comment.equals((Comment) o);
        }
    }

    @Override
    public int compareTo(CommentView another) {
        return (new CommentViewTimeComparator()).compare(this,another);
    }

    private class CommentViewTimeComparator implements Comparator<CommentView> {

        @Override
        public int compare(CommentView lhs, CommentView rhs) {
            Comment flhs = lhs.comment;
            Comment frhs = rhs.comment;
            return flhs.getTimestamp().compareTo(frhs.getTimestamp());
        }
    }

}
