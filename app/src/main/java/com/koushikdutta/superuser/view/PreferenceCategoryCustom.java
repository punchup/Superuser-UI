package com.koushikdutta.superuser.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.core.ThemeStore;


public class PreferenceCategoryCustom extends PreferenceCategory {


    public PreferenceCategoryCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategoryCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceCategoryCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategoryCustom(Context context) {
        super(context);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView textView = (TextView) holder.findViewById(android.R.id.title);
        textView.setAllCaps(true);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(ThemeStore.accentColor(getContext()));
    }
}
