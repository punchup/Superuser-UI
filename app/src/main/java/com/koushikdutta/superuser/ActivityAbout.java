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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.koushikdutta.superuser.helper.Theme;

import static com.koushikdutta.superuser.MainActivity.PREF_LIGHT_THEME;

public class ActivityAbout extends ATHToolbarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = Theme.setTheme(this, pref);

        int textToolbarDefault = bundle.getInt(Theme.TEXT_COLOR_TOOLBAR);
        String theme = bundle.getString(Theme.THEME_CURRENT);

        setContentView(R.layout.activity_about);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Theme.handleTheme(this, theme, textToolbarDefault, null, null, toolbar, null);


        final LinearLayout uiCard, aospCard, contCard;

        uiCard = (LinearLayout) findViewById(R.id.card_su_ui);
        aospCard = (LinearLayout) findViewById(R.id.card_aosp);
        contCard = (LinearLayout) findViewById(R.id.card_cont);

        final LinearLayout uiParent, aospParent, contParent;

        uiParent = (LinearLayout) findViewById(R.id.su_ui_info_parent);
        aospParent = (LinearLayout) findViewById(R.id.aosp_info_parent);
        contParent = (LinearLayout) findViewById(R.id.cont_info_parent);

        setClick(uiCard, uiParent);
        setClick(aospCard, aospParent);
        setClick(contCard, contParent);


        TextView su = (TextView) findViewById(R.id.su_info);

        su.setText(Html.fromHtml(
                "Licensed under " + addLink("GPLv3", "https://github.com/seSuperuser/super-bootimg/blob/master/LICENSE") + "<br>" +
                "Hosted on " + addLink("Github", "https://github.com/seSuperuser/super-bootimg") + "<br>" +
                "Maintained by " + addLink("phhusson", "https://github.com/phhusson") + " - " + addLink("XDA", "http://forum.xda-developers.com/member.php?u=1915408") + "<br><br>" +
                "Need help? Visit the " + addLink("XDA thread", "http://forum.xda-developers.com/android/software-hacking/wip-selinux-capable-superuser-t3216394")));


        TextView ui = (TextView) findViewById(R.id.su_ui_info);

        ui.setText(Html.fromHtml(
                "Licensed under " + addLink("GPLv3", "https://github.com/seSuperuser/Superuser-UI/blob/master/LICENSE") + "<br>" +
                "Hosted on " + addLink("Github", "https://github.com/seSuperuser/Superuser-UI") + "<br>" +
                "Maintained by " + addLink("PunchUp", "https://github.com/punchup") + " - " + addLink("XDA", "http://forum.xda-developers.com/member.php?u=5444464")));

        TextView library = (TextView) findViewById(R.id.su_ui_library_info);

        library.setText(Html.fromHtml(
                "AppCompat by Google<br>" +
                "Design Support by Google<br>" +
                addLink("App Intro", "https://github.com/PaoloRotolo/AppIntro") + " by PaoloRotolo<br>" +
                addLink("App Theme Helper", "https://github.com/kabouzeid/app-theme-helper") + " by afollestad/kabouzeid<br>" +
                addLink("Simple Item Decoration", "https://github.com/bignerdranch/simple-item-decoration") + " by bignerdranch"
        ));

        TextView resources = (TextView) findViewById(R.id.su_ui_res_info);

        resources.setText(Html.fromHtml(
                addLink("Pointing hand", "https://www.iconfinder.com/icons/111069/finger_point_up_icon") + " image by WPZOOM<br>"
        ));


        TextView aosp = (TextView) findViewById(R.id.aosp_info);

        aosp.setText(Html.fromHtml(
                "Hosted on " + addLink("Github", "https://github.com/seSuperuser/AOSP-SU-PATCH") + "<br>" +
                "Maintained by " + addLink("lbdroid", "https://github.com/lbdroid")
        ));
    }


    private String addLink(String s, String link) {
        return "<a href = \"" + link + "\">" + s + "</a>";
    }


    private void setClick(LinearLayout card, final LinearLayout parent) {
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent.getVisibility() == View.GONE) parent.setVisibility(View.VISIBLE);
                else parent.setVisibility(View.GONE);
            }
        });
    }
}