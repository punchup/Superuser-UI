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

package com.koushikdutta.superuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.core.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.koushikdutta.superuser.helper.Theme;
import com.koushikdutta.superuser.util.Util;

import static com.koushikdutta.superuser.FragmentMain.SHOULD_RELOAD;

public class MainActivity extends ATHToolbarActivity
        implements FragmentLog.LogCallback, FragmentMain.MainCallback, ColorChooserDialog.ColorCallback {

    public static final String PREF_THEME = "theme";
    public static final String PREF_BLACK_THEME = "black_theme";
    public static final String PREF_DARK_THEME = "dark_theme";
    public static final String PREF_LIGHT_THEME = "light_theme";


    DrawerLayout drawer;

    AppBarLayout appBar;
    Toolbar toolbar;
    TabLayout tabLayout;

    private PagerAdapter pagerAdapter;
    //FloatingActionButton fab;


    SharedPreferences pref;
    SharedPreferences.Editor prefEdit;

    public String theme;
    public int textToolbarDefault;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.colorPrimary_Light)
                    .accentColorRes(R.color.colorAccent_Light)
                    //.coloredNavigationBar(false)
                    .commit();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        prefEdit = pref.edit();

        Bundle bundle = Theme.setTheme(this, pref);

        theme = bundle.getString(Theme.THEME_CURRENT);
        textToolbarDefault = bundle.getInt(Theme.TEXT_COLOR_TOOLBAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        appBar = (AppBarLayout) findViewById(R.id.appbar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu);

        setupDrawer(toolbar);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(2);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setSelectedTabIndicatorColor(pref.getInt("tab_indicator", ATHUtil.resolveColor(this, R.attr.tabIndicatorAccent)));

        Theme.handleTheme(this, theme, textToolbarDefault, drawer, appBar, toolbar, tabLayout);
    }


    private void setupDrawer(final Toolbar toolbar) {
        //todo: back button close?
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        TextView title = (TextView) drawer.findViewById(R.id.title_theme);
        title.setTextColor(ThemeStore.accentColor(this));
        title.setPadding(0, Build.VERSION.SDK_INT >= 21 ? Util.toPx(this, 40) : Util.toPx(this, 20), 0, Util.toPx(this, 10));

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.addDrawerListener(actionBarDrawerToggle);
        //actionBarDrawerToggle.syncState();

        drawer.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                drawer.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, ActivitySettings.class));
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    }
                }, 200);
            }
        });

        drawer.findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                drawer.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, ActivityAbout.class));
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    }
                }, 200);
            }
        });


        LinearLayout themeBaseParent = (LinearLayout) drawer.findViewById(R.id.theme_base);
        TextView current = (TextView) drawer.findViewById(R.id.theme_current);

        final LinearLayout listTheme = (LinearLayout) drawer.findViewById(R.id.list_theme);

        themeBaseParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listTheme.setVisibility(listTheme.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        switch (theme) {
            case PREF_BLACK_THEME: current.setText(R.string.black); drawer.findViewById(R.id.parent).setBackgroundColor(0xff161616); break;
            case PREF_DARK_THEME:  current.setText(R.string.dark);  break;
            case PREF_LIGHT_THEME: current.setText(R.string.light); break;
        }

        setNavThemeBaseItem(listTheme, R.id.theme_black, PREF_BLACK_THEME);
        setNavThemeBaseItem(listTheme, R.id.theme_dark, PREF_DARK_THEME);
        setNavThemeBaseItem(listTheme, R.id.theme_light, PREF_LIGHT_THEME);


        final LinearLayout toolbarThemeParent = (LinearLayout) drawer.findViewById(R.id.theme_parent);

        LinearLayout toolbarBackground = (LinearLayout) toolbarThemeParent.findViewById(R.id.primary_color);
        LinearLayout toolbarText = (LinearLayout) toolbarThemeParent.findViewById(R.id.toolbar_text);
        LinearLayout tabIndicator = (LinearLayout) toolbarThemeParent.findViewById(R.id.tab_indicator);
        LinearLayout accent = (LinearLayout) toolbarThemeParent.findViewById(R.id.accent);
        LinearLayout counterBack = (LinearLayout) toolbarThemeParent.findViewById(R.id.counter_back);
        LinearLayout counterText = (LinearLayout) toolbarThemeParent.findViewById(R.id.counter_text);

        TextView toolbarBackgroundDisplay = (TextView) toolbarBackground.findViewById(R.id.title);
        toolbarBackgroundDisplay.setText(R.string.primary_color);

        TextView toolbarTextDisplay = (TextView) toolbarText.findViewById(R.id.title);
        toolbarTextDisplay.setText(R.string.toolbar_text);

        TextView tabIndicatorDisplay = (TextView) tabIndicator.findViewById(R.id.title);
        tabIndicatorDisplay.setText(R.string.tab_indicator);

        TextView accentDisplay = (TextView) accent.findViewById(R.id.title);
        accentDisplay.setText(R.string.accent_color);

        TextView counterBackDisplay = (TextView) counterBack.findViewById(R.id.title);
        counterBackDisplay.setText(R.string.counter_back);

        TextView counterTextDisplay = (TextView) counterText.findViewById(R.id.title);
        counterTextDisplay.setText(R.string.counter_text);

        addNavThemeItem(toolbarBackground, ThemeStore.primaryColor(this), R.string.primary_color);
        addNavThemeItem(toolbarText,       Theme.getTextColorToolbar(pref), R.string.toolbar_text);
        addNavThemeItem(tabIndicator,      Theme.getTabIndicatorColor(this, pref), R.string.tab_indicator);
        addNavThemeItem(accent,            ThemeStore.accentColor(this), R.string.accent_color);
        addNavThemeItem(counterBack,       Theme.getCounterBackColor(pref, 0xff656565), R.string.counter_back);
        addNavThemeItem(counterText,       Theme.getCounterTextColor(pref), R.string.counter_text);
    }


    private void setNavThemeBaseItem(LinearLayout parent, int resId, final String theme) {

        RelativeLayout child = (RelativeLayout) parent.findViewById(resId);

        int background = 0xffeaeaea;
        int text = 0xff424242;

        switch (resId) {
            case R.id.theme_black: background = 0xff101010; text = 0xffdfdfdf; break;
            case R.id.theme_dark:  background = 0xff404040; text = 0xffeaeaea; break;
        }

        ((ImageView)child.findViewById(R.id.theme_background)).setColorFilter(background);
        ((TextView) child.findViewById(R.id.theme_text)).setTextColor(text);

        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefEdit.putString(PREF_THEME, theme).commit()) recreate();
            }
        });
    }


    private void addNavThemeItem(LinearLayout child, final int color, final int title) {

        final ImageView preview = (ImageView) child.findViewById(R.id.preview);
        preview.setColorFilter(color);

        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ColorChooserDialog.Builder(MainActivity.this, title)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(true)
                        .dynamicButtonColor(false)
                        .preselect(color)
                        .show();
            }
        });
    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i) {

        ThemeStore themeStore = ThemeStore.editTheme(this);

        switch (colorChooserDialog.getTitle()) {

            case R.string.primary_color: themeStore.primaryColor(i); break;
            case R.string.toolbar_text : Theme.setTextColorToolbar(prefEdit, i); break;
            case R.string.tab_indicator: Theme.setTabIndicatorColor(prefEdit, i); break;
            case R.string.accent_color : themeStore.accentColor(i); break;
            case R.string.counter_back: Theme.setCounterBackColor(prefEdit, i); break;
            case R.string.counter_text: Theme.setCounterTextColor(prefEdit, i); break;
        }

        prefEdit.commit();
        themeStore.commit();
        recreate();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (SHOULD_RELOAD) {
            onChanged();
            SHOULD_RELOAD = false;
        }
    }


    @Override
    public void onChanged() {
        ((FragmentMain)pagerAdapter.getRegisteredFragment(1)).load();
        ((FragmentMain)pagerAdapter.getRegisteredFragment(2)).load();
    }


    @Override
    public void onListChanged(int which) {
        switch (which) {

            case FragmentMain.FRAGMENT_ALLOWED:
                ((FragmentMain)pagerAdapter.getRegisteredFragment(2)).load();
                break;

            case FragmentMain.FRAGMENT_DENIED:
                ((FragmentMain)pagerAdapter.getRegisteredFragment(1)).load();
                break;
        }
    }


    @Override
    public void onGridSpanChanged(int which, int val) {
        switch (which) {

            case FragmentMain.FRAGMENT_ALLOWED:
                ((FragmentMain) pagerAdapter.getRegisteredFragment(2)).setSpan(val);
                break;

            case FragmentMain.FRAGMENT_DENIED:
                ((FragmentMain) pagerAdapter.getRegisteredFragment(1)).setSpan(val);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        FragmentMain.data = null;
    }




    public class PagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: return new FragmentLog();

                case 1: return FragmentMain.newInstance(FragmentMain.FRAGMENT_ALLOWED);

                case 2: return FragmentMain.newInstance(FragmentMain.FRAGMENT_DENIED);
            }

            return null;
        }


        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.logs);
                case 1: return getString(R.string.allowed);
                case 2: return getString(R.string.denied);
            }
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
