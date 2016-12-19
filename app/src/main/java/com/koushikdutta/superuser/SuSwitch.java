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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.koushikdutta.superuser.helper.Settings;

import java.util.Arrays;


public class SuSwitch extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int which = -1;

        switch (Settings.getSuperuserAccess()) {
            case Settings.SUPERUSER_ACCESS_ADB_ONLY:
            case Settings.SUPERUSER_ACCESS_APPS_ONLY:
            case Settings.SUPERUSER_ACCESS_APPS_AND_ADB:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("last_state", Settings.getSuperuserAccess()).apply();
                Settings.setSuperuserAccess(Settings.SUPERUSER_ACCESS_DISABLED);
                which = 1;
                break;

            case Settings.SUPERUSER_ACCESS_DISABLED:
                Settings.setSuperuserAccess(PreferenceManager.getDefaultSharedPreferences(this).getInt("last_state", Settings.SUPERUSER_ACCESS_APPS_AND_ADB));
                which = 0;
                break;
        }
        Toast.makeText(this, String.valueOf(which), Toast.LENGTH_SHORT).show();
        setShortcut(this, which);
        notify(which);

        finish();
    }
    
    
    @TargetApi(Build.VERSION_CODES.N_MR1)
    static void setShortcut(Context context, int which) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "id1")
                .setShortLabel(which == 0 ? context.getString(R.string.enable_superuser) : context.getString(R.string.disable_superuser))
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_shortcut))
                .setIntent(new Intent(Intent.ACTION_MAIN).setClass(context, SuSwitch.class))
                .build();

        shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
    }

    private void notify(int which) {
        String s = "Error";

        switch (which) {
            case 0:
                switch (Settings.getSuperuserAccess()) {
                    case Settings.SUPERUSER_ACCESS_ADB_ONLY:     s = getString(R.string.adb_only); break;
                    case Settings.SUPERUSER_ACCESS_APPS_ONLY:    s = getString(R.string.apps_only); break;
                    case Settings.SUPERUSER_ACCESS_APPS_AND_ADB: s = getString(R.string.apps_and_adb); break;
                }
                break;

            case 1:
                switch (Settings.getSuperuserAccess()) {
                    case Settings.SUPERUSER_ACCESS_DISABLED: s = getString(R.string.access_disabled); break;
                }
                break;
        }

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
