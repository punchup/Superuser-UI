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
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.koushikdutta.superuser.helper.Settings;

import java.util.Arrays;

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean startup = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("intro", true);

        if (startup) {
            startActivity(new Intent(this, ActivityIntro.class));

            if (Build.VERSION.SDK_INT >= 25) {
                switch (Settings.getSuperuserAccess()) {
                    case Settings.SUPERUSER_ACCESS_ADB_ONLY:
                    case Settings.SUPERUSER_ACCESS_APPS_ONLY:
                    case Settings.SUPERUSER_ACCESS_APPS_AND_ADB:
                        SuSwitch.setShortcut(this, 1);
                        break;

                    case Settings.SUPERUSER_ACCESS_DISABLED:
                        SuSwitch.setShortcut(this, 0);
                        break;
                }
            }

        } else
            startActivity(new Intent(this, MainActivity.class));

        finish();
    }
}
