package com.fudi.fudi.back;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents a notification in Fudi.
 * Created by chijioke on 5/7/16.
 */
public class FudiNotification implements Comparable<FudiNotification> {

    private static final String PREFIX = "NT";

    public enum NotificationType {FUD_POST, COMMENTED_ON}

    private String dishName;
    private String fudID;
    private String notificationID;
    private Date time;
    private NotificationType nt;
    private boolean seen;

    private FudiNotification(){}

    public FudiNotification(String dishName, final String fudID, final Date time, boolean seen,
                            NotificationType nt){
        this.dishName = dishName;
        this.fudID = fudID;
        this.time = time;
        this.nt = nt;
        this.seen = seen;
        this.notificationID = genarateID();

    }

    public String getNotificationID(){
        return notificationID;
    }


    public NotificationType getNotificationType() {
        return nt;
    }

    public Date getTime() {
        return time;
    }

    public String getDishName() {
        return dishName;
    }

    public String getFudID() {
        return fudID;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        } else if (!(o instanceof  FudiNotification)){
            return false;
        } else {
            FudiNotification fn = (FudiNotification) o;
            return fudID.equals(fn.getFudID()) && time.equals(fn.getTime());
        }
    }

    public HashMap<String, Object> toFirebase(){
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("fudID", fudID);
        hm.put("dishName", dishName);
        hm.put("timestamp", time);
        hm.put("userID", FudiApp.getInstance().getThisUser().getUserID());
        hm.put("seen", seen);
        hm.put("notificationID", notificationID);

        return hm;
    }

    public String genarateID(){
        return PREFIX + FudiApp.generateID(25);
    }

    public static FudiNotification fromFirebaseToFudiNotification(HashMap<String, Object> hm){
        FudiNotification fn =  new FudiNotification();
        fn.fudID = (String) hm.get("fudID");
        fn.dishName = (String) hm.get("dishName");
        fn.time = new Date((Long) hm.get("timestamp"));
        fn.seen = (boolean) hm.get("seen");
        fn.notificationID = (String) hm.get("notificationID");

        return fn;
    }

    @Override
    public int compareTo(FudiNotification another) {
        return (new FudiNotificationTimeComparator()).compare(this,another);
    }

    private class FudiNotificationTimeComparator implements Comparator<FudiNotification> {

        @Override
        public int compare(FudiNotification lhs, FudiNotification rhs) {
            return rhs.getTime().compareTo(lhs.getTime());
        }
    }

}
