package com.fudi.fudi.back;

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

}
