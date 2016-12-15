package com.koushikdutta.superuser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.koushikdutta.superuser.helper.Theme;


public class ActivitySettings extends ATHToolbarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = Theme.setTheme(this, pref);

        int textToolbarDefault = bundle.getInt(Theme.TEXT_COLOR_TOOLBAR);
        String theme = bundle.getString(Theme.THEME_CURRENT);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Theme.handleTheme(this, theme, textToolbarDefault, null, null, toolbar, null);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FragmentSettings()).commit();

        } else {
            FragmentSettings frag = (FragmentSettings) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.init();

        }
    }
}
