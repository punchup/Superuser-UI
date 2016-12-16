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
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.widget.Toast;

import com.koushikdutta.superuser.db.LogEntry;
import com.koushikdutta.superuser.db.SuperuserDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;
import com.koushikdutta.superuser.helper.Settings;

import static android.content.Context.POWER_SERVICE;

public class SuReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 4545;


    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null)
            return;

        String command = intent.getStringExtra("command");
        if (command == null)
            return;

        int uid = intent.getIntExtra("uid", -1);
        if (uid == -1) return;

        int desiredUid = intent.getIntExtra("desired_uid", -1);
        if (desiredUid == -1) return;

        String action = intent.getStringExtra("action");
        if (action == null) return;

        String fromName = intent.getStringExtra("from_name");
        String desiredName = intent.getStringExtra("desired_name");

		String bindFrom = intent.getStringExtra("bind_from");
		String bindTo = intent.getStringExtra("bind_to");

        final LogEntry logEntry = new LogEntry();
        logEntry.uid = uid;
        logEntry.command = command;
        logEntry.action = action;
        logEntry.desiredUid = desiredUid;
        logEntry.desiredName = desiredName;
        logEntry.username = fromName;
        logEntry.date = (int)(System.currentTimeMillis() / 1000);
        logEntry.getPackageInfo(context);

        UidPolicy policy = SuperuserDatabaseHelper.addLog(context, logEntry);


        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("deny_mode", false)
                && !UidPolicy.DENY.equals(action)) return;


        String toast;
        if (UidPolicy.ALLOW.equals(action))
            toast = context.getString(R.string.superuser_granted, logEntry.getName());

        else
            toast = context.getString(R.string.superuser_denied, logEntry.getName());


        if (policy != null && !policy.notification)
            return;

        switch (Settings.getNotificationType(context)) {

            case Settings.NOTIFICATION_TYPE_NOTIFICATION:
                notification(context, toast); break;

            case Settings.NOTIFICATION_TYPE_TOAST:
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show(); break;

            case Settings.NOTIFICATION_TYPE_SMART:

                if (isScreenOn(context)) Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                else notification(context, toast);
                break;
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Common.INTENT_FILTER_LOG));
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(Common.INTENT_FILTER_MAIN).putExtra("pkg", logEntry.packageName).putExtra("policy", action));
    }


    private void notification(Context context, String toast) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setTicker(toast)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .setContentTitle(context.getString(R.string.superuser))
                .setContentText(toast)
                .setSmallIcon(Build.VERSION.SDK_INT >= 21 ? R.drawable.ic_superuser : R.drawable.ic_superuser_compat);

        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification;

        if (Build.VERSION.SDK_INT < 16) notification = builder.getNotification();
        else notification = builder.build();

        nm.notify(NOTIFICATION_ID, notification);
    }


    //http://stackoverflow.com/a/17348755
    private boolean isScreenOn(Context context) {

        if (Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF)
                    return true;
            }

            return false;

        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            return (powerManager.isScreenOn());
        }
    }
}
