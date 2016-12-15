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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.koushikdutta.superuser.helper.Settings;
import com.koushikdutta.superuser.helper.SuHelper;


public class SuCheckerReceiver extends BroadcastReceiver {

    private static final String ACTION_DELETED = "internal.superuser.ACTION_CHECK_DELETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null)
            return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || "internal.superuser.BOOT_TEST".equals(intent.getAction())) {
            // if the user deleted the notification in the past, don't bother them again for a while
            int counter = Settings.getCheckSuQuietCounter(context);

            if (counter > 0) {
                Log.i("Superuser", "Not bothering user... su counter set.");
                counter--;
                Settings.setCheckSuQuietCounter(context, counter);
                return;
            }

            final Handler handler = new Handler();
            new Thread() {
                public void run() {
                    try {
                        SuHelper.checkSu(context);

                    } catch (Exception ex) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                doNotification(context);
                            }
                        });
                    }
                };
            }.start();

        } else if (ACTION_DELETED.equals(intent.getAction())) {
            // notification deleted? bother the user in 3 reboots.
            Log.i("Superuser", "Will not bother the user in the future... su counter set.");
            Settings.setCheckSuQuietCounter(context, 3);
        }
    }


    public static void doNotification(Context context) {

        Notification.Builder builder = new Notification.Builder(context);
        builder.setTicker(context.getString(R.string.install_superuser));
        builder.setContentTitle(context.getString(R.string.install_superuser));
        builder.setSmallIcon(Build.VERSION.SDK_INT >= 21 ? R.drawable.ic_superuser : R.drawable.ic_superuser_compat);
        builder.setWhen(0);
        builder.setContentText(context.getString(R.string.su_binary_outdated));
        builder.setAutoCancel(true);

        PendingIntent launch = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        Intent delIntent = new Intent(context, SuCheckerReceiver.class);
        delIntent.setAction(ACTION_DELETED);

        PendingIntent delete = PendingIntent.getBroadcast(context, 0, delIntent, 0);
        builder.setDeleteIntent(delete);
        builder.setContentIntent(launch);

        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification;

        if (Build.VERSION.SDK_INT < 16) notification = builder.getNotification();
        else notification = builder.build();

        nm.notify(10000, notification);
    }
}
