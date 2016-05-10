package com.fudi.fudi.back;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents the CommentSection of a FudDetail.
 * That is the area below the Fud and it's details, where all the replies are.
 *
 * Created by chijioke on 4/14/16.
 */
public class CommentSection {

    private FudDetail parentFud;
    private TreeSet<Comment> comments; //ordered by first -> last

    private static final String PREFIX = "CS";
    public CommentSection(){comments = new TreeSet<Comment>();}

    public CommentSection(FudDetail parent){
        this.parentFud = parent;
        comments = new TreeSet<Comment>();
    }

    /**
     * Adds a comment to the comment section
     * @param comm the Comment to be added
     */
    public void postComment(Comment comm){
        //TODO: add comment to the comment section
        if(!comments.contains(comm)) {
            comments.add(comm);
        }

        //DO NOT call pushComment() from here
    }

    public FudDetail getParentFud() {
        return parentFud;
    }

    public int getCommentNumber(){
        return comments.size();
    }

    public TreeSet<Comment> getComments(){
        return comments;
    }

    public TreeMap<String, Object> getFirebaseableComments(){
        TreeMap<String, Object> firebaseable = new TreeMap<String, Object>();
        int i = 0;
        for(Comment c : comments){
            firebaseable.put(Integer.toString(i), c.toFirebase());
            i++;
        }
        if(firebaseable.isEmpty()){
            firebaseable.put("placeholder","placeholder");
        }

        return firebaseable;
    }

    public static CommentSection firebaseToCommentSection(HashMap<String, Object> hm, FudDetail parentFud){
        CommentSection cs = new CommentSection();
        TreeSet<Comment> comms = new TreeSet<Comment>();
        for(Object commentObject : hm.values()){
            if(!(commentObject instanceof HashMap)){
                continue;
            }
            HashMap<String, Object> commentMap = (HashMap<String, Object>) commentObject;
            if(commentMap.get("type") == null){
                continue;
            }
            if(((String) commentMap.get("type")).equals("review")){
                comms.add(ReviewComment.firebaseToReviewComment(commentMap, cs));
            } else {
                comms.add(GeneralComment.firebaseToGeneralComment(commentMap, cs));
            }
        }
        cs.comments = comms;
        cs.parentFud = parentFud;
        return cs;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof  CommentSection)) {
            return false;
        } else {
            return comments.equals(((CommentSection) o).comments);
        }
    }

}
