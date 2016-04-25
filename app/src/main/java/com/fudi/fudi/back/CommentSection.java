package com.fudi.fudi.back;

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
    private String commentSectionID;

    private static final String PREFIX = "CS";
    private CommentSection(){}

    public CommentSection(FudDetail parent){
        this.parentFud = parent;
        comments = new TreeSet<Comment>();
        commentSectionID = generateID();
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

    /**
     * Deletes a comment
     * @param comm the Comment to be deleted
     */
    public void deleteComment(Comment comm){
        //TODO: delete comment from the comment section
        
        //TODO: delete comment from the database
    }

    /**
     * Pushes the CommentSection to the database. Should only be called once from within
     * the CommentSection class constructor.
     */
    public void push(){
        //TODO: push this CommentSection to the database
        //Need to push ID, all current comments,
    }

    /**
     * Pushes a comment to the database for this CommentSection
     * @param comm the Comment to be pushed
     */
    public void pushComment(Comment comm){
        //TODO: push a comment to the database for the CommentSection/FudDetail
        //Note a ReviewComment and GeneralComment should be distinguishable in the database
    }

    public void pushComments(){
        //TODO: push all current comments to the database for the CommentSection/FudDetail
        //Note a ReviewComment and GeneralComment should be distinguishable in the database

        /*If this is a new Comment section, add it to the database. You will need to add a
        reference to it's parent id as well.
         */
    }

    public TreeSet<Comment> pullComments(){
        //TODO: pull all comments for this CommentSection from the database
        //Probably need to request by the CommentSection's ID
        return null;
    }

    public String generateID(){
        /* TODO: generate a unique ID for this comment section.
            PREFIX+FUD_ID+something generated here
         */
        return PREFIX;
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

    public String getCommentSectionID() {
        return commentSectionID;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof  CommentSection)){
            return false;
        } else {
            return commentSectionID.equals(((CommentSection) o).commentSectionID);
        }
    }

}
