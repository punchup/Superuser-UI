package com.kabouzeid.appthemehelper.common;

import android.support.v7.widget.Toolbar;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;

public class ATHActionBarActivity extends ATHToolbarActivity {

    @Override
    protected Toolbar getATHToolbar() {
        return ToolbarContentTintHelper.getSupportActionBarView(getSupportActionBar());
    }
}
