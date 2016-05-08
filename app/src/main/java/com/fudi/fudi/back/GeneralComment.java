package com.fudi.fudi.back;

import java.util.Date;
import java.util.HashMap;

/**
 * Represents a general comment to a FudDetail. A general comment is simply just a standard reply
 * to a fud post: text and the username of the person who posted it (as well as how long ago it
 * was posted).
 *
 * Created by chijioke on 4/14/16.
 */
public class GeneralComment extends Comment {

    public GeneralComment(){}

    public GeneralComment(String text, User whoPosted, CommentSection parent){
        super(text, whoPosted, parent);
    }

    public static GeneralComment firebaseToGeneralComment(HashMap<String, Object> hm, CommentSection parent){
        String text = (String) hm.get("text");
        User whoPosted = User.getStandInUser((String) hm.get("userID"), (String) hm.get("username"));
        GeneralComment gc = new GeneralComment(text, whoPosted, parent);
        Vote v = gc.getVote();
        v.setUpvotes((long) hm.get("upvotes"));
        v.setDownvotes((long) hm.get("downvotes"));
        gc.setTimestamp(new Date((Long) hm.get("timestamp")));
        return gc;
    }
}
