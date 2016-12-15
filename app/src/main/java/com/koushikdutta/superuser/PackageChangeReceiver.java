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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.ArrayList;

import com.koushikdutta.superuser.db.SuDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;


public class PackageChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            public void run() {
                ArrayList<UidPolicy> policies = SuDatabaseHelper.getPolicies(context);

                if (policies == null)
                    return;

                final PackageManager pm = context.getPackageManager();

                for (UidPolicy policy: policies) {
                    // if the uid did not have a package name at creation time,
                    // it may be a nameless or unresolveable uid...
                    // ie, I can do something like:
                    // su - 5050
                    // # 5050 has no name, so the following su will be an empty package name
                    // su
                    //
                    // ignore this null package name as valid.
                    if (TextUtils.isEmpty(policy.packageName))
                        continue;

                    try {
                        boolean found = false;
                        String[] names = pm.getPackagesForUid(policy.uid);

                        if (names == null)
                            throw new Exception("no packages for uid");

                        for (String name: names) {
                            if (name.equals(policy.packageName))
                                found = true;
                        }

                        if (!found)
                            throw new Exception("no package name match");

                    } catch (Exception e) {
                        SuDatabaseHelper.delete(context, policy);
                    }
                }
            }

        }.start();
    }
}
