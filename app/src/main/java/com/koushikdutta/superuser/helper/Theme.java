/**
 * Superuser
 * Copyright (C) 2016 Pierre-Hugues Husson (phhusson)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/


package com.koushikdutta.superuser.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import com.kabouzeid.appthemehelper.core.ATH;
import com.kabouzeid.appthemehelper.core.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.koushikdutta.superuser.R;

import static com.koushikdutta.superuser.MainActivity.PREF_BLACK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_DARK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_LIGHT_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_THEME;

public class Theme {

    public static final String THEME_CURRENT = "theme";
    public static final String TEXT_COLOR_TOOLBAR = "text_color_toolbar";


    public static Bundle setTheme(Activity context, SharedPreferences pref) {

        String theme = PREF_LIGHT_THEME;
        int textToolbarDefault = 0xff424242;

        switch (pref.getString(PREF_THEME, PREF_LIGHT_THEME)) {
            case PREF_BLACK_THEME:
                theme = PREF_BLACK_THEME;
                textToolbarDefault = 0xffeaeaea;
                context.setTheme(R.style.AppTheme_Black_NoActionBar);
                break;

            case PREF_DARK_THEME:
                theme = PREF_DARK_THEME;
                context.setTheme(R.style.AppTheme_NoActionBar);
                break;

            case PREF_LIGHT_THEME:
                theme = PREF_LIGHT_THEME;
                context.setTheme(R.style.AppTheme_Light_NoActionBar);
                break;
        }

        Bundle bundle = new Bundle();

        bundle.putString(THEME_CURRENT, theme);
        bundle.putInt(TEXT_COLOR_TOOLBAR, pref.getInt(TEXT_COLOR_TOOLBAR, textToolbarDefault));

        return bundle;
    }


    public static void handleTheme(Activity context, String theme, int textToolbarDefault, DrawerLayout drawer, AppBarLayout appBar, Toolbar toolbar, TabLayout tabLayout) {

        if (!theme.equals(PREF_BLACK_THEME)) {
            ATH.setStatusbarColor(context, context.getResources().getInteger(R.integer.statusbar_tint));

            if (drawer != null) drawer.setStatusBarBackgroundColor(ThemeStore.statusBarColor(context));
            else ((CoordinatorLayout)context.findViewById(R.id.coordinator)).setStatusBarBackgroundColor(ThemeStore.statusBarColor(context));

            if (appBar != null) appBar.setBackgroundColor(ThemeStore.primaryColor(context));
            else toolbar.setBackgroundColor(ThemeStore.primaryColor(context));

            ToolbarContentTintHelper.setToolbarContentColor(context, toolbar, null, textToolbarDefault, textToolbarDefault, textToolbarDefault, 0xff424242);

            int tabTextColor = PreferenceManager.getDefaultSharedPreferences(context).getInt("tab_text_selected", textToolbarDefault);

            if (tabLayout != null)
                tabLayout.setTabTextColors(
                        //ColorUtil.isColorLight(tabTextColor) ? ColorUtil.shiftColor(tabTextColor, 0.9f) : ColorUtil.shiftColor(tabTextColor, 1.5f),
                        Color.argb(180, Color.red(tabTextColor), Color.green(tabTextColor), Color.blue(tabTextColor)),
                        tabTextColor
            );


        } else {
            ToolbarContentTintHelper.setToolbarContentColor(context, toolbar, null, 0xffeaeaea, 0xffeaeaea, 0xffeaeaea, 0xff424242);

            if (tabLayout != null) tabLayout.setTabTextColors(ColorUtil.shiftColor(0xffeaeaea, 1.5f), 0xffeaeaea);
        }
    }


    public static String getThemeCurrent(SharedPreferences pref) {
        return pref.getString(PREF_THEME, PREF_LIGHT_THEME);
    }


    public static int getTextColorToolbar(SharedPreferences pref) {
        return pref.getInt(TEXT_COLOR_TOOLBAR, 0x404040);
    }

    public static SharedPreferences.Editor setTextColorToolbar(SharedPreferences.Editor prefEdit, int value) {
        return prefEdit.putInt(TEXT_COLOR_TOOLBAR, value);
    }

    public static int getTabIndicatorColor(Context context, SharedPreferences pref) {
        return pref.getInt("tab_indicator", ATHUtil.resolveColor(context, R.attr.tabIndicatorAccent));
    }

    public static SharedPreferences.Editor setTabIndicatorColor(SharedPreferences.Editor prefEdit, int value) {
        return prefEdit.putInt("tab_indicator", value);
    }

    public static int getCounterBackColor(SharedPreferences pref, int defValue) {
        return pref.getInt("counter_back", defValue);
    }

    public static SharedPreferences.Editor setCounterBackColor(SharedPreferences.Editor prefEdit, int value) {
        return prefEdit.putInt("counter_back", value);
    }

    public static int getCounterTextColor(SharedPreferences pref) {
        return pref.getInt("counter_text", 0xfffafafa);
    }

    public static SharedPreferences.Editor setCounterTextColor(SharedPreferences.Editor prefEdit, int value) {
        return prefEdit.putInt("counter_text", value);
    }

}
