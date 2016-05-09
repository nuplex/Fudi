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

    static public void push(Voteable v){

        // All the database interaction is done in the FudiApp class
        // Grab an instance of it and let it update the Vote.
        FudiApp.getInstance().updateVote(v);
    }
}
