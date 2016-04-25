package com.fudi.fudi.back;

/**
 * Represents a general comment to a FudDetail. A general comment is simply just a standard reply
 * to a fud post: text and the username of the person who posted it (as well as how long ago it
 * was posted).
 *
 * Created by chijioke on 4/14/16.
 */
public class GeneralComment extends Comment {

    public GeneralComment(String text, User whoPosted, CommentSection parent){
        super(text, whoPosted, parent);
    }

}
