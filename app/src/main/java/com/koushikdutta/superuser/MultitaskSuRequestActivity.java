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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.DataInputStream;
import java.io.File;
import java.util.HashMap;

import com.kabouzeid.appthemehelper.core.ThemeStore;
import com.koushikdutta.superuser.db.SuDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;
import com.koushikdutta.superuser.util.Util;
import com.koushikdutta.superuser.view.PinView;
import com.koushikdutta.superuser.helper.Settings;
import com.koushikdutta.superuser.helper.SuHelper;

import static com.koushikdutta.superuser.MainActivity.PREF_BLACK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_DARK_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_LIGHT_THEME;
import static com.koushikdutta.superuser.MainActivity.PREF_THEME;

@SuppressLint("ValidFragment")
public class MultitaskSuRequestActivity extends AppCompatActivity {

    private Spinner spinner;
    private Button allow, deny;


    private static final String LOGTAG = "Superuser";

    private final static int SU_PROTOCOL_PARAM_MAX = 20;
    private final static int SU_PROTOCOL_NAME_MAX = 20;
    private final static int SU_PROTOCOL_VALUE_MAX_DEFAULT = 256;

    private final static HashMap<String, Integer> SU_PROTOCOL_VALUE_MAX = new HashMap<String, Integer>() {

        private static final long serialVersionUID = 5649873127008413475L; {
            put("command", 2048);
        }
    };


    private SharedPreferences pref;

    private ArrayAdapter<String> spinnerAdapter;

    private Handler handler = new Handler();

    private LocalSocket socket;

    private boolean handled, requestReady;

    private String socketPath;
    private String desiredCmd;
    private String bindFrom, bindTo;

    private int callerUid, desiredUid;

    private int pid, timeLeft = 3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        switch (pref.getString(PREF_THEME, PREF_LIGHT_THEME)) {
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

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        socketPath = intent.getStringExtra("socket");
        if (socketPath == null) {
            finish();
            return;
        }
        //Toast.makeText(this, socketPath, Toast.LENGTH_SHORT).show();
        overridePendingTransition(R.anim.abc_slide_in_bottom, 0);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.background_light);
        getWindow().setGravity(Gravity.BOTTOM);

        setContentView();

        manageSocket();


        // watch for the socket disappearing. that means su died.
        new Runnable() {
            public void run() {
                if (isFinishing()) return;

                if (!new File(socketPath).exists()) {
                    finish();
                    return;
                }

                handler.postDelayed(this, 1000);
            };
        }.run();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;

                if (!handled)
                    handleAction(false, -1);
            }

        }, Settings.getRequestTimeout(this) * 1000);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView();
    }


    void setContentView() {
        View root = LayoutInflater.from(this).inflate(R.layout.activity_request, null);

        setContentView(root, new LinearLayout.LayoutParams(width(this, 375), height(this, 250, 250)));

        ((TextView) root.findViewById(R.id.request)).setTextColor(ThemeStore.primaryColor(this));

        root.findViewById(R.id.spinner_arrow).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.performClick();
            }
        });

        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item));
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);


        allow = (Button)findViewById(R.id.allow);
        deny = (Button)findViewById(R.id.deny);

        allow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.isPinProtected(MultitaskSuRequestActivity.this)) {

                    if (pref.getBoolean("confirmation", false)) snack(null);
                    else approve(null);

                    if (getUntil() == 0)
                        LocalBroadcastManager.getInstance(MultitaskSuRequestActivity.this).sendBroadcast(new Intent("POLICY_UPDATE"));

                    return;
                }

                ViewGroup ready = (ViewGroup)findViewById(R.id.root);
                final int until = getUntil();
                ready.removeAllViews();

                PinView pin = new PinView(getLayoutInflater(), R.layout.view_pin_request, (ViewGroup)findViewById(android.R.id.content), null) {
                    @Override
                    public void onEnter(String password) {
                        super.onEnter(password);
                        if (Settings.checkPin(MultitaskSuRequestActivity.this, password)) {

                            if (pref.getBoolean("confirmation", false)) snack(until);
                            else approve(until);


                            if (until == 0)
                                LocalBroadcastManager.getInstance(MultitaskSuRequestActivity.this).sendBroadcast(new Intent("POLICY_ADD"));

                        } else {
                            Toast.makeText(MultitaskSuRequestActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                ready.addView(pin.getView());
            }
        });

        deny.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deny();
            }
        });

        if (requestReady)
            requestReady();
    }


    @Override
    protected void onPause() {
        super.onPause();
        hideOverlays(true);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideOverlays(false);
    }


    void hideOverlays(boolean v) {
        //TODO: Have a proper intent naming ?
        Intent i = new Intent("eu.chainfire.supersu.action.HIDE_OVERLAYS");
        i.putExtra("eu.chainfire.supersu.extra.HIDE", v);
        i.addCategory("android.intent.category.INFO");
        sendBroadcast(i);
    }


    void manageSocket() {
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new LocalSocket();
                    socket.connect(new LocalSocketAddress(socketPath, Namespace.FILESYSTEM));

                    DataInputStream is = new DataInputStream(socket.getInputStream());

                    ContentValues payload = new ContentValues();


                    for (int i = 0; i < SU_PROTOCOL_PARAM_MAX; i++) {

                        int nameLen = is.readInt();
                        if (nameLen > SU_PROTOCOL_NAME_MAX)
                            throw new IllegalArgumentException("name length too long: " + nameLen);

                        byte[] nameBytes = new byte[nameLen];
                        is.readFully(nameBytes);

                        String name = new String(nameBytes);

                        int dataLen = is.readInt();
                        if (dataLen > getValueMax(name))
                            throw new IllegalArgumentException(name + " data length too long: " + dataLen);

                        byte[] dataBytes = new byte[dataLen];
                        is.readFully(dataBytes);

                        String data = new String(dataBytes);

                        payload.put(name, data);
                        //Log.i(LOGTAG, name);
                        //Log.i(LOGTAG, data);

                        if ("eof".equals(name))
                            break;
                    }

                    //int protocolVersion = payload.getAsInteger("version");
                    callerUid  = payload.getAsInteger("from.uid");
                    desiredUid = payload.getAsInteger("to.uid");
                    desiredCmd = payload.getAsString("command");
                    bindFrom   = payload.getAsString("bind.from");
                    bindTo     = payload.getAsString("bind.to");

                    pid = payload.getAsInteger("pid");
                    //String calledBin = payload.getAsString("from.bin");

                    Log.i(LOGTAG, "Got bind from " + payload.getAsString("bind.from") + " to " + payload.getAsString("bind.to"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            requestReady = true;
                            requestReady();
                        }
                    });

                    if ("com.koushikdutta.superuser".equals(getPackageName())) {
                        if (!SuHelper.CURRENT_VERSION.equals(payload.getAsString("binary.version")))
                            SuCheckerReceiver.doNotification(MultitaskSuRequestActivity.this);
                    }

                } catch (Exception ex) {
                    Log.i(LOGTAG, ex.getMessage(), ex);

                    try {
                        socket.close();
                    } catch (Exception e) {
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }.start();
    }


    void requestReady() {
        //findViewById(R.id.incoming).setVisibility(View.GONE);
        findViewById(R.id.ready).setVisibility(View.VISIBLE);

        final View packageInfo = findViewById(R.id.packageinfo);

        final PackageManager pm = getPackageManager();
        String[] pkgs = pm.getPackagesForUid(callerUid);

        TextView unknown = (TextView)findViewById(R.id.unknown);
        unknown.setText(getString(R.string.unknown_uid, String.valueOf(callerUid)));

        ((TextView)findViewById(R.id.uid_header)).setText(Integer.toString(desiredUid));
        ((TextView)findViewById(R.id.command_header)).setText(desiredCmd);

        if(!"".equals(bindFrom) && !"".equals(bindTo)) {
            findViewById(R.id.bind).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.bind_to)).setText(bindTo);

            findViewById(R.id.uid).setVisibility(View.GONE);
            findViewById(R.id.command).setVisibility(View.GONE);
            //findViewById(R.id.remember).setVisibility(View.GONE);

            spinnerAdapter.add(getString(R.string.once));

        } else {
            spinnerAdapter.add(getString(R.string.once));
            spinnerAdapter.add(getString(R.string.forever));
            spinnerAdapter.add(getString(R.string.remember_minutes, "10"));
            spinnerAdapter.add(getString(R.string.remember_minutes, "20"));
            spinnerAdapter.add(getString(R.string.remember_minutes, "30"));
            spinnerAdapter.add(getString(R.string.remember_minutes, "60"));
        }


        if (pkgs != null && pkgs.length > 0) {
            for (String pkg: pkgs) {
                try {
                    PackageInfo pi = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);

                    //((TextView)findViewById(R.id.request)).setText(getString(R.string.application_request, pi.applicationInfo.loadLabel(pm)));

                    ImageView icon = (ImageView)packageInfo.findViewById(R.id.image);
                    icon.setImageDrawable(pi.applicationInfo.loadIcon(pm));

                    ((TextView)packageInfo.findViewById(R.id.title)).setText(pi.applicationInfo.loadLabel(pm));

                    //((TextView)findViewById(R.id.app_header)).setText(pi.applicationInfo.loadLabel(pm));
                    ((TextView)findViewById(R.id.summary)).setText(pi.packageName);

                    // could display them all, but maybe a better ux for this later
                    break;

                } catch (Exception ex) {
                }
            }

            findViewById(R.id.unknown).setVisibility(View.GONE);
        }

        // Automatic response
        switch (Settings.getAutomaticResponse(MultitaskSuRequestActivity.this)) {
            case Settings.AUTOMATIC_RESPONSE_ALLOW:
                // automatic response and pin can not be used together
                // check if the permission must be granted
                Log.i(LOGTAG, "Automatically allowing due to user preference");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!handled)
                            handleAction(true, 0);
                    }
                });
                return;

            case Settings.AUTOMATIC_RESPONSE_DENY:
                Log.i(LOGTAG, "Automatically denying due to user preference");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!handled)
                            handleAction(false, 0);
                    }
                });
                return;
        }

        new Runnable() {
            public void run() {
                allow.setText(getString(R.string.allow) + " (" + timeLeft + ")");

                if (timeLeft-- <= 0) {
                    allow.setText(getString(R.string.allow));
                    if (!handled) allow.setEnabled(true);
                    return;
                }

                handler.postDelayed(this, 1000);
            }
        }.run();
    }


    private void snack(final Integer until) {
        Snackbar.make(findViewById(R.id.root), R.string.confirmation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.yes, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        approve(until);
                    }
                }).show();
    }


    void approve(Integer until) {
        allow.setEnabled(false);
        deny.setEnabled(false);

        handleAction(true, until);
    }


    void deny() {
        allow.setEnabled(false);
        deny.setEnabled(false);

        handleAction(false, null);
    }


    void handleAction(boolean action, Integer until) {
        Assert.assertTrue(!handled);
        handled = true;

        try {
            socket.getOutputStream().write((action ? "socket:ALLOW" : "socket:DENY").getBytes());

        } catch (Exception ex) {
        }

        try {
            if (until == null) {
                until = getUntil();
            }

            if (until != -1) {
                UidPolicy policy = new UidPolicy();
                policy.policy = action ? UidPolicy.ALLOW : UidPolicy.DENY;
                policy.uid = callerUid;
                // for now just approve all commands, since per command approval is stupid
                //policy.command = desiredCmd;
                policy.command = null;
                policy.until = until;
                policy.desiredUid = desiredUid;
                SuDatabaseHelper.setPolicy(this, policy);
            }
            // TODO: logging? or should su binary handle that via broadcast?
            // Probably the latter, so it is consolidated and from the system of record.

        } catch (Exception ex) {
        }

        finish();
    }


    private static int getValueMax(String name) {
        Integer max = SU_PROTOCOL_VALUE_MAX.get(name);

        if (max == null)
            return SU_PROTOCOL_VALUE_MAX_DEFAULT;
        return max;
    }


    int getUntil() {
        int until = -1;

        if (spinner.isShown()) {
            int pos = spinner.getSelectedItemPosition();
            //int id = spinnerIds[pos];

            if (pos == 1) until = 0;
            else if (pos == 5) until = (int)(System.currentTimeMillis() / 1000) +  3600;
            else until = (int)(System.currentTimeMillis() / 1000) + ((pos - 1) * 60);


            /*if (id == R.string.remember_for)
                until = (int)(System.currentTimeMillis() / 1000) + getGracePeriod() * 60;

            else if (id == R.string.remember_forever)
                until = 0;*/
        }
        /*else if (mRemember.isShown()) {
            if (mRemember.getCheckedRadioButtonId() == R.id.remember_for)
                until = (int)(System.currentTimeMillis() / 1000) + getGracePeriod() * 60;

            else if (mRemember.getCheckedRadioButtonId() == R.id.remember_forever)
                until = 0;

        }*/

        return until;
    }


    // Not really sure how these dimensions would behave on various screens and custom dpi values. Might need some changes
    public static int height(Context context, int h1, int h2) {

        int height;

        if (context.getResources().getConfiguration().screenWidthDp < 400)
            height = Util.toPx(context, h1);

        else
            height = Util.toPx(context, h2);

        return height;
    }


    public static int width(Context context, int w) {

        int width;

        if (context.getResources().getConfiguration().screenWidthDp < 400)
            width = context.getResources().getDisplayMetrics().widthPixels;

        else
            width = Util.toPx(context, w);

        return width;
    }


    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, R.anim.abc_slide_out_bottom);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!handled)
            handleAction(false, -1);

        try {
            if (socket != null)
                socket.close();

        } catch (Exception ex) {
        }

        new File(socketPath).delete();
    }
}