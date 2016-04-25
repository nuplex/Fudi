package com.fudi.fudi.back;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Represents a comment on a FudDetail, of which there can be two types: GeneralComment and ReviewComment.
 *
 * Created by chijioke on 4/14/16.
 */
public abstract class Comment implements Comparable<Comment>, Voteable {

    private String text;
    private User whoPosted;
    private CommentSection parent;
    private Date timestamp;
    private Vote vote;

    protected Comment(String text, User whoPosted, CommentSection parent){
        this.text = text;
        this.whoPosted = whoPosted;
        this.parent = parent;
        timestamp = Calendar.getInstance().getTime();
        vote = new Vote(whoPosted.getUserID());
    }

    public String getText() {
        return text;
    }

    public User getWhoPosted() {
        return whoPosted;
    }

    public CommentSection getParent() {
        return parent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Vote getVote() {
        return vote;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof Comment)){
            return false;
        } else {
            Comment c = (Comment) o;
            return text.equals(c.text) && whoPosted.equals(c.whoPosted)
                    && parent.equals(c.parent) && timestamp.equals(c.timestamp);
        }
    }

    @Override
    public int compareTo(Comment another){
        return (new CommentComparable()).compare(this, another);
    }

    public class CommentComparable implements Comparator<Comment> {

        @Override
        public int compare(Comment lhs, Comment rhs) {
            return lhs.timestamp.compareTo(rhs.timestamp);
        }
    }
}
