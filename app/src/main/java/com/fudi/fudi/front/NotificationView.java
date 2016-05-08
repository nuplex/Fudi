package com.fudi.fudi.front;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fudi.fudi.R;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.FudiNotification;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents a notification of a new comment on a FudPost either of the user's or one they've
 * commented on, or a certain number of upvotes on a FudPost posted by the user.
 * Created by chijioke on 5/7/16.
 */
public class NotificationView extends View implements Comparable<NotificationView> {

    private Context context;

    private LinearLayout view;
    private TextView timeAgo;

    private String[] messages = {
            "Someone commented on your fud: ",
            "Someone else commented on this fud: "
    };

    private Spannable notification;

    private FudiNotification.NotificationType nt;
    private FudiNotification notif;

    public static final String FROM_NOTIFICATION = "fromNotification";


    public NotificationView(FudiNotification fn, final Context context) {
        super(context);
        this.nt = notif.getNotificationType();
        this.notif = fn;
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        view = (LinearLayout) inflater.inflate(R.layout.notification, null);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NotificationView.this.context, FudDetailActivity.class);
                i.putExtra("fudID", notif.getDishName());
                i.putExtra(FROM_NOTIFICATION, true);
                context.startActivity(i);
            }
        });

        String msg_start = "";

        if(nt == FudiNotification.NotificationType.FUD_POST){
            msg_start = messages[0];
        } else if (nt == FudiNotification.NotificationType.COMMENTED_ON){
            msg_start = messages[1];
        }

        String msg = msg_start + "\""+notif.getDishName()+"\".";
        notification = Spannable.Factory.getInstance().newSpannable(msg);
        notification.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.fudi_text_color)), 0,
                msg_start.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        TextView notificationText = (TextView) view.findViewById(R.id.notification_text);
        notificationText.setText(notification, TextView.BufferType.SPANNABLE);

        timeAgo = (TextView) view.findViewById(R.id.notification_time);
        timeAgo.setText(FudiApp.getTimeSincePostedString(notif.getTime()));

        if(!notif.isSeen()){
            setAsNew();
        }
    }

    public void setAsNew(){
        view.setBackgroundColor(getResources().getColor(R.color.secondary_color));
        timeAgo.setTextColor(getResources().getColor(R.color.default_text_color));
    }

    public void setAsOld(){
        view.setBackgroundColor(Color.WHITE);
        timeAgo.setTextColor(getResources().getColor(R.color.secondary_color_darker));
    }

    public LinearLayout getView(){
        return view;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        } else if (!(o instanceof NotificationView)){
            return false;
        } else {
            NotificationView nv = (NotificationView) o;
            return notif.equals(nv.notif);
        }
    }

    @Override
    public int compareTo(NotificationView another) {
        return (new NotificationViewTimeComparator()).compare(this,another);
    }

    private class NotificationViewTimeComparator implements Comparator<NotificationView> {

        @Override
        public int compare(NotificationView lhs, NotificationView rhs) {
            return rhs.notif.getTime().compareTo(lhs.notif.getTime());
        }
    }

}
