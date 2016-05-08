package com.fudi.fudi.back;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents a review in a FudDetail. A review is one of the types of comments that can be posted
 * to in reply to a Fud Post. It requires a rating and a photo.
 *
 * Optional: ReviewComments can have general comment replies
 *
 * Created by chijioke on 4/14/16.
 */
public class ReviewComment extends Comment {

    private String proofImageURL;
    private Rating rating;

    public static final Rating OKAY = Rating.OKAY;

    public ReviewComment(){
        super();
    }

    public ReviewComment(String text, String proofImageURL, Rating rating,
                         User whoPosted, CommentSection parent){
        super(text, whoPosted, parent);
        this.proofImageURL = proofImageURL;
        this.rating = rating;
    }

    public String getProofImageURL() {
        return proofImageURL;
    }

    public Rating getRating() {
        return rating;
    }

    /**
     * Represents a review's possible ratings. Can be added to or changed at anytime
     */
    public enum Rating{GREAT,OKAY,MEH,NO}

    public static ReviewComment firebaseToReviewComment(HashMap<String, Object> hm, CommentSection parent){
        String text = (String) hm.get("text");
        Rating r = Rating.valueOf((String) hm.get("rating"));
        String imgURL = (String) hm.get("imageURL");
        User whoPosted = User.getStandInUser((String) hm.get("userID"), (String) hm.get("username"));
        ReviewComment rc = new ReviewComment(text, imgURL, r, whoPosted, parent);
        Vote v = rc.getVote();
        v.setUpvotes((long) hm.get("upvotes"));
        v.setDownvotes((long) hm.get("downvotes"));
        rc.setTimestamp(new Date((Long) hm.get("timestamp")));
        return rc;
    }

}
