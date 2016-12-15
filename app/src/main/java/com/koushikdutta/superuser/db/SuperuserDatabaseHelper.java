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

package com.koushikdutta.superuser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;


import java.util.ArrayList;

import com.koushikdutta.superuser.helper.Settings;

public class SuperuserDatabaseHelper extends SQLiteOpenHelper {
    private static final int CURRENT_VERSION = 1;
    public SuperuserDatabaseHelper(Context context) {
        super(context, "superuser.sqlite", null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, CURRENT_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 0) {
            db.execSQL("create table if not exists log (id integer primary key autoincrement, desired_name text, username text, uid integer, desired_uid integer, command text not null, date integer, action text, package_name text, name text)");
            db.execSQL("create index if not exists log_uid_index on log(uid)");
            db.execSQL("create index if not exists log_desired_uid_index on log(desired_uid)");
            db.execSQL("create index if not exists log_command_index on log(command)");
            db.execSQL("create index if not exists log_date_index on log(date)");
            db.execSQL("create table if not exists settings (key text primary key not null, value text)");
            oldVersion = 1;
        }
    }

    public static ArrayList<LogEntry> getLogs(Context context, UidPolicy policy, int limit) {
        SQLiteDatabase db = new SuperuserDatabaseHelper(context).getReadableDatabase();
        try {
            return getLogs(db, policy, limit);
        }
        finally {
            db.close();
        }
    }
    public static ArrayList<LogEntry> getLogs(SQLiteDatabase db, UidPolicy policy, int limit) {
        ArrayList<LogEntry> ret = new ArrayList<LogEntry>();
        Cursor c;
        if (!TextUtils.isEmpty(policy.command))
            c = db.query("log", null, "uid = ? and desired_uid = ? and command = ?", new String[] { String.valueOf(policy.uid), String.valueOf(policy.desiredUid), policy.command }, null, null, "date DESC", limit == -1 ? null : String.valueOf(limit));
        else
            c = db.query("log", null, "uid = ? and desired_uid = ?", new String[] { String.valueOf(policy.uid), String.valueOf(policy.desiredUid) }, null, null, "date DESC", limit == -1 ? null : String.valueOf(limit));
        try {
            while (c.moveToNext()) {
                LogEntry l = new LogEntry();
                ret.add(l);
                l.getUidCommand(c);
                l.id = c.getLong(c.getColumnIndex("id"));
                l.date = c.getInt(c.getColumnIndex("date"));
                l.action = c.getString(c.getColumnIndex("action"));
            }
        }
        catch (Exception ex) {
        }
        finally {
            c.close();
        }
        return ret;
    }

    public static ArrayList<LogEntry> getLogs(Context context) {
        SQLiteDatabase db = new SuperuserDatabaseHelper(context).getReadableDatabase();
        try {
            return getLogs(context, db);
        }
        finally {
            db.close();
        }
    }
    public static ArrayList<LogEntry> getLogs(Context context, SQLiteDatabase db) {
        ArrayList<LogEntry> ret = new ArrayList<LogEntry>();
        Cursor c = db.query("log", null, null, null, null, null, "date DESC");
        try {
            while (c.moveToNext()) {
                LogEntry l = new LogEntry();
                ret.add(l);
                l.getUidCommand(c);
                l.id = c.getLong(c.getColumnIndex("id"));
                l.date = c.getInt(c.getColumnIndex("date"));
                l.action = c.getString(c.getColumnIndex("action"));
            }
        }
        catch (Exception ex) {
        }
        finally {
            c.close();
        }
        return ret;
    }

    public static void deleteLogs(Context context) {
        SQLiteDatabase db = new SuperuserDatabaseHelper(context).getWritableDatabase();
        db.delete("log", null, null);
        db.close();
    }

    static void addLog(SQLiteDatabase db, LogEntry log) {
        ContentValues values = new ContentValues();
        values.put("uid", log.uid);
        // nulls are considered unique, even from other nulls. blerg.
        // http://stackoverflow.com/questions/3906811/null-permitted-in-primary-key-why-and-in-which-dbms
        if (log.command == null)
            log.command = "";
        values.put("command", log.command);
        values.put("action", log.action);
        values.put("date", log.date);
        values.put("name", log.name);
        values.put("desired_uid", log.desiredUid);
        values.put("package_name", log.packageName);
        values.put("desired_name", log.desiredName);
        values.put("username", log.username);
        db.insert("log", null, values);
    }

    public static UidPolicy addLog(Context context, LogEntry log) {
        // nulls are considered unique, even from other nulls. blerg.
        // http://stackoverflow.com/questions/3906811/null-permitted-in-primary-key-why-and-in-which-dbms
        if (log.command == null)
            log.command = "";

        // grab the policy and add a log
        UidPolicy u = null;
        SQLiteDatabase su = new SuDatabaseHelper(context).getReadableDatabase();
        Cursor c = su.query("uid_policy", null, "uid = ? and (command = ? or command = ?) and desired_uid = ?", new String[] { String.valueOf(log.uid), log.command, "", String.valueOf(log.desiredUid) }, null, null, null, null);
        try {
            if (c.moveToNext()) {
                u = SuDatabaseHelper.getPolicy(c);
            }
        }
        finally {
            c.close();
            su.close();
        }

        if (u != null && !u.logging)
            return u;

        if (!Settings.getLogging(context))
            return u;

        SQLiteDatabase superuser = new SuperuserDatabaseHelper(context).getWritableDatabase();
        try {
            // delete logs over 2 weeks
            superuser.delete("log", "date < ?", new String[] { String.valueOf((System.currentTimeMillis() - 14L * 24L * 60L * 60L * 1000L) / 1000L) });
            addLog(superuser, log);
        }
        finally {
            superuser.close();
        }

        return u;
    }
}
