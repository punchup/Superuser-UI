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
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;


public class ActivityIntro extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFadeAnimation();
        skipButtonEnabled = false;

        setProgressButtonEnabled(false);

        addSlide(FragmentIntro.newInstance(R.layout.fragment_intro_0));
        addSlide(AppIntro2Fragment.newInstance("App Settings", "Click on the icon for per app settings\nLong click will open the app itself", R.drawable.intro_1, 0xff43a047));
        addSlide(AppIntro2Fragment.newInstance("Changing Permission", "To allow/deny or revoke, click on the app name", R.drawable.intro_2, 0xff0288d1));
        //addSlide(AppIntro2Fragment.newInstance("Switching", "Use the floating action button to switch between allowed and denied apps", R.drawable.intro_3, 0xff6a1b9a));
    }


    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

        if (newFragment != null && newFragment.getView() != null) {
            View parent = newFragment.getView();

            TextView title = (TextView) parent.findViewById(R.id.title);

            if (title != null) {
                if (!(newFragment instanceof FragmentIntro)) title.setTextColor(0xffeaeaea);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

                if (Build.VERSION.SDK_INT > 15) title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            }


            TextView desc = (TextView) parent.findViewById(R.id.description);

            if (desc != null) {
                desc.setTextColor(0xffe9e9e9);
                desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);
                desc.setLineSpacing(1f, 1);

                //if (Build.VERSION.SDK_INT > 15) desc.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

            }
        }
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("intro", false).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    @Override
    public void onBackPressed() {
    }
}