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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.koushikdutta.superuser.MainActivity.PREF_BLACK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_DARK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_LIGHT_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_THEME;
import static com.koushikdutta.superuser.MultitaskSuRequestActivity.height;
import static com.koushikdutta.superuser.MultitaskSuRequestActivity.width;


public class NotifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        switch (pref.getString(PREF_THEME, PREF_DARK_THEME)) {
            case PREF_BLACK_THEME:
            case PREF_DARK_THEME:
                setTheme(R.style.PopupTheme);
                break;

            case PREF_LIGHT_THEME:
                setTheme(R.style.PopupTheme_Light);
                break;
        }

        //Settings.applyDarkThemeSetting(this, R.style.RequestThemeDark);
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.abc_slide_in_bottom, 0);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.background_light);
        getWindow().setGravity(Gravity.BOTTOM);

        View root = LayoutInflater.from(this).inflate(R.layout.activity_notify, null);

        setContentView(root, new LinearLayout.LayoutParams(width(this, 375), height(this, 180, 180)));

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        int callerUid = intent.getIntExtra("caller_uid", -1);
        if (callerUid == -1) {
            finish();
            return;
        }

        final View packageInfo = findViewById(R.id.packageinfo);

        final PackageManager pm = getPackageManager();

        String[] pkgs = pm.getPackagesForUid(callerUid);

        TextView unknown = (TextView)findViewById(R.id.unknown);
        unknown.setText(getString(R.string.unknown_uid, String.valueOf(callerUid)));

        if (pkgs != null && pkgs.length > 0) {
            for (String pkg: pkgs) {
                try {
                    PackageInfo pi = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
                    ImageView icon = (ImageView)packageInfo.findViewById(R.id.image);
                    icon.setImageDrawable(pi.applicationInfo.loadIcon(pm));
                    ((TextView)packageInfo.findViewById(R.id.title)).setText(pi.applicationInfo.loadLabel(pm));
                    ((TextView)findViewById(R.id.summary)).setText(pi.packageName);

                    break;

                } catch (Exception ex) {

                }
            }

            findViewById(R.id.unknown).setVisibility(View.GONE);
        }

        findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, R.anim.abc_slide_out_bottom);
    }
}