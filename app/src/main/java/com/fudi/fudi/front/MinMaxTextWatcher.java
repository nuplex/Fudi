package com.fudi.fudi.front;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

/**
 * Used for entry fields to cap their input.
 * Created by chijioke on 4/23/16.
 */
public class MinMaxTextWatcher<V extends TextView> implements TextWatcher{

    private V view;
    private int minChar;
    private int maxChar;
    private int currChar;
    private Context context;

    public MinMaxTextWatcher(V view, int minChar, int maxChar, Context context){
        this.view = view;
        this.minChar = minChar;
        this.maxChar = maxChar;
        this.context = context;
        currChar = this.view.getText().length();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        currChar = s.length();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        currChar = s.length();
    }

    @Override
    public void afterTextChanged(Editable s) {
        currChar = s.length();
        int diff = getLeftToMax();
        if(diff <= -1){
            String truncated = s.subSequence(0,
                    maxChar).toString();
            s.clear();
            s.append(truncated);
        }
    }

    public int getMinChar() {
        return minChar;
    }

    public int getMaxChar() {
        return maxChar;
    }

    /**
     * @return number of current characters
     */
    public int getCurrent() {
        return currChar;
    }

    /**
     * @return number of characters left before max;
     */
    public int getLeftToMax(){
        return maxChar - currChar;
    }

    /**
     * @return number of characters left before min;
     */
    public int getLeftToMin(){
        return currChar - minChar;
    }

    /**
     *
     * @return if the number of characters is less than max and bigger than min
     */
    public boolean isGood(){
        return (currChar >= minChar && currChar <= maxChar);
    }
}
