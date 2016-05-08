package com.fudi.fudi.front;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fudi.fudi.R;

/**
 * Created by chijioke on 5/5/16.
 */
public class ProcessingPopup extends View {

    private FrameLayout popup;
    private RelativeLayout popupWindow;
    private TextView processingText;

    public ProcessingPopup(Context context){
        super(context);
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        popup = (FrameLayout) inflater.inflate(R.layout.processing_popup, null);

        popupWindow = (RelativeLayout) popup.findViewById(R.id.processing_popup_window);

        processingText = (TextView) popup.findViewById(R.id.processing_popup_text);
    }

    public void setMessage(String msg){
        processingText.setText(msg);
    }

    public void grayout(boolean gray){
        if(true){
            popup.setBackgroundColor(getResources().getColor(R.color.grayout));
        } else {
            popup.setBackground(null);
        }
    }

    public void makeProcessingCircle(){
        //stuff
    }

    public FrameLayout getView(){
        return popup;
    }



}
