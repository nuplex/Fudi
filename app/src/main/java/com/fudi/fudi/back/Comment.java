package com.fudi.fudi.back;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

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
    private long commentNumber;

    public Comment(){}

    protected Comment(String text, User whoPosted, CommentSection parent){
        this.text = text;
        this.whoPosted = whoPosted;
        this.parent = parent;
        timestamp = Calendar.getInstance().getTime();
        vote = new Vote(whoPosted.getUserID());
        commentNumber = -1;
    }

    public void setCommentNumber(long l){
        commentNumber = l;
    }

    public long getCommentNumber(){
        return commentNumber;
    }

    public String getText() {
        return text;
    }

    public User getWhoPosted() {
        return whoPosted;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public TreeMap<String, Object> toFirebase(){
        String text = getText();
        String userID = getWhoPosted().getUserID();
        String type = (this instanceof ReviewComment) ? "review":"general";
        String imageURL = (this instanceof ReviewComment) ? ((ReviewComment) this).getProofImageURL():"";
        String rating = (this instanceof ReviewComment) ? ((ReviewComment) this).getRating().name() :"";
        Long time = getTimestamp().getTime();
        Vote v = getVote();

        TreeMap<String, Object> info = new TreeMap<String, Object>();
        info.put("text", text);
        info.put("userID", userID);
        info.put("username", whoPosted.getUsername());
        info.put("type", type);
        info.put("imageURL", imageURL);
        info.put("rating",rating);
        info.put("timestamp",time);
        info.put("upvotes", v.getUpvotes());
        info.put("downvotes", v.getDownvotes());
        info.put("netvote", v.getNet());
        return info;
    }


}
