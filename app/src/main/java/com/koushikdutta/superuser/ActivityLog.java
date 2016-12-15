package com.koushikdutta.superuser;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.koushikdutta.superuser.helper.Theme;

public class ActivityLog extends ATHToolbarActivity {

    public int textToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = Theme.setTheme(this, pref);

        textToolbar = bundle.getInt(Theme.TEXT_COLOR_TOOLBAR);
        String theme = bundle.getString(Theme.THEME_CURRENT);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Theme.handleTheme(this, theme, textToolbar, null, null, toolbar, null);
    }
}
