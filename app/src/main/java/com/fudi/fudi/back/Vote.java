package com.fudi.fudi.back;

/**
 * Represents a Vote on Fud post or on a (optionally) on a comment.
 *
 *
 * Created by chijioke on 4/19/16.
 */
public class Vote {

    private long upvotes;
    private long downvotes;
    private String userID;

    public enum Type{UPFU,DOWNFU}

    /**
     * Creates a new Vote object for handling upvotes and downvotes.
     * @param associate the user (ID) associated with this vote. This allows allows user's to have
     *                  their Fu updated when anyone upvotes or downvotes something they posted.
     */
    public Vote(String associate){
        upvotes = 0;
        downvotes = 0;
    }

    /**
     * Creates a new Vote object for handling upvotes and downvotes.
     * @param upvotes
     * @param downvotes
     * @param associate the user (ID) associated with this vote. This allows allows user's to have
     *                  their Fu updated when anyone upvotes or downvotes something they posted.
     */
    public Vote(long upvotes, long downvotes, String associate){
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public void vote(Vote.Type type) {
        if (type == Type.UPFU) {
            upvotes++;
        } else if (type == Type.DOWNFU) {
            downvotes++;
        }
    }

    public void push(Voteable v){
        //TODO: First, push the changed vote data for this user

        if(v instanceof FudDetail){
            FudDetail fudDetail = (FudDetail) v;
            //TODO: Update the fud detail
        } else if(v instanceof Fud) {
            Fud fud = (Fud) v;
            //TODO: Update the fud detail associated with Fud
        } else if(v instanceof Comment){
            Comment comment = (Comment) v;
            //Need to discriminate between a GeneralComment and ReviewComment
            //TODO: Don't worry about this yet, or, you can. Same as above just storing the vote data.
        }
    }

    public long getNet(){
        return upvotes - downvotes;
    }

    public long getDownvotes() {
        return downvotes;
    }

    public long getUpvotes() {
        return upvotes;
    }

    public void undo(Type vote){
        if(vote == Type.UPFU){
            upvotes--;
        } else {
            downvotes--;
        }
    }

    public void setUpvotes(long upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(long downvotes) {
        this.downvotes = downvotes;
    }
}
