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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.common.preference.ATESwitchPreference;
import com.koushikdutta.superuser.view.PinView;
import com.koushikdutta.superuser.helper.Settings;

public class FragmentSettings extends PreferenceFragmentCompat {

    SharedPreferences pref;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        pref = PreferenceManager.getDefaultSharedPreferences(getContext());

        addPreferencesFromResource(R.xml.fragment_settings);

        init();
    }


    public void init() {
        initPrefAccess();
        initPrefMultiuser();
        initPrefResponse();
        initPrefTimeout();
        //initPrefLogging();
        initPrefNotification();
        initPrefEllipsize();
        initPrefPin();
    }


    /*private void setIcon(Preference preference, int id) {

        VectorDrawableCompat drawable = VectorDrawableCompat.create(getResources(), id, getActivity().getTheme());

        int tint = 0xffcfcfcf;

        switch (pref.getString(PREF_THEME, PREF_LIGHT_THEME)) {

            case PREF_BLACK_THEME:
            case PREF_DARK_THEME:
                tint = 0xffcccccc;
                break;

            case PREF_LIGHT_THEME:
                tint = 0xff919191;
                break;
        }

        if (drawable != null) {
            drawable.setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
            preference.setIcon(drawable);
        }
    }*/


    @Override
    public void onResume() {
        super.onResume();

        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
    }


    private void initPrefAccess() {

        Preference preference = findPreference("access");

        //setIcon(preference, R.drawable.ic_superuser);

        updateAccess(preference);

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                String[] items = new String[] { getString(R.string.access_disabled), getString(R.string.apps_only), getString(R.string.adb_only), getString(R.string.apps_and_adb) };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle(R.string.superuser_access);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switch (i) {
                            case 0:
                                Settings.setSuperuserAccess(Settings.SUPERUSER_ACCESS_DISABLED);
                                break;
                            case 1:
                                Settings.setSuperuserAccess(Settings.SUPERUSER_ACCESS_APPS_ONLY);
                                break;
                            case 2:
                                Settings.setSuperuserAccess(Settings.SUPERUSER_ACCESS_ADB_ONLY);
                                break;
                            case 3:
                                Settings.setSuperuserAccess(Settings.SUPERUSER_ACCESS_APPS_AND_ADB);
                                break;
                        }

                        updateAccess(preference);
                        dialogInterface.dismiss();
                    }
                });

                builder.show();

                return true;
            }
        });
    }


    private void updateAccess(Preference preference) {

        switch (Settings.getSuperuserAccess()) {
            case Settings.SUPERUSER_ACCESS_ADB_ONLY:
                preference.setSummary(R.string.adb_only);
                break;

            case Settings.SUPERUSER_ACCESS_APPS_ONLY:
                preference.setSummary(R.string.apps_only);
                break;

            case Settings.SUPERUSER_ACCESS_APPS_AND_ADB:
                preference.setSummary(R.string.apps_and_adb);
                break;

            case Settings.SUPERUSER_ACCESS_DISABLED:
                preference.setSummary(R.string.access_disabled);
                break;

            default:
                preference.setSummary(R.string.apps_and_adb);
                break;
        }
    }


    private void initPrefMultiuser() {

        Preference preference = findPreference("multiuser");

        //setIcon(preference, R.drawable.ic_multi);

        if (Settings.getMultiuserMode(getActivity()) == Settings.MULTIUSER_MODE_NONE) {
            preference.setEnabled(false);

        } else {

            int res = updateMultiuser();

            if (!Settings.isAdminUser(getActivity())) {
                preference.setEnabled(false);

                String s = "";
                if (res != -1) s = getString(res) + "\n";

                preference.setSummary(s + getString(R.string.multiuser_require_owner));

            } else {
                if (res != -1) preference.setSummary(res);

                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(final Preference preference) {

                        String[] items = new String[] { getString(R.string.multiuser_owner_only), getString(R.string.multiuser_owner_managed), getString(R.string.multiuser_user) };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle(R.string.multiuser_policy);

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:
                                        Settings.setMultiuserMode(getActivity(), Settings.MULTIUSER_MODE_OWNER_ONLY);
                                        break;

                                    case 1:
                                        Settings.setMultiuserMode(getActivity(), Settings.MULTIUSER_MODE_OWNER_MANAGED);
                                        break;

                                    case 2:
                                        Settings.setMultiuserMode(getActivity(), Settings.MULTIUSER_MODE_USER);
                                        break;
                                }

                                preference.setSummary(updateMultiuser());
                            }
                        });

                        builder.show();

                        return true;
                    }
                });
            }
        }
    }


    private int updateMultiuser() {
        int res = -1;

        switch (Settings.getMultiuserMode(getActivity())) {

            case Settings.MULTIUSER_MODE_OWNER_MANAGED:
                res = R.string.multiuser_owner_managed_summary;
                break;

            case Settings.MULTIUSER_MODE_OWNER_ONLY:
                res = R.string.multiuser_owner_only_summary;
                break;

            case Settings.MULTIUSER_MODE_USER:
                res = R.string.multiuser_user_summary;
                break;
        }

        return res;
    }


    private void initPrefResponse() {

        Preference preference = findPreference("response");

        //setIcon(preference, R.drawable.ic_auto);

        updateResponse(preference);

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                String[] items = new String[] { getString(R.string.prompt), getString(R.string.deny), getString(R.string.allow) };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.automatic_response);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Settings.setAutomaticResponse(getActivity(), Settings.AUTOMATIC_RESPONSE_PROMPT);
                                break;
                            case 1:
                                Settings.setAutomaticResponse(getActivity(), Settings.AUTOMATIC_RESPONSE_DENY);
                                break;
                            case 2:
                                Settings.setAutomaticResponse(getActivity(), Settings.AUTOMATIC_RESPONSE_ALLOW);
                                break;
                        }

                        updateResponse(preference);
                    }
                });

                builder.show();

                return true;
            }
        });
    }


    private void updateResponse(Preference preference) {

        switch (Settings.getAutomaticResponse(getActivity())) {

            case Settings.AUTOMATIC_RESPONSE_ALLOW:
                preference.setSummary(R.string.automatic_response_allow_summary);
                break;

            case Settings.AUTOMATIC_RESPONSE_DENY:
                preference.setSummary(R.string.automatic_response_deny_summary);
                break;

            case Settings.AUTOMATIC_RESPONSE_PROMPT:
                preference.setSummary(R.string.automatic_response_prompt_summary);
                break;
        }
    }


    private void initPrefTimeout() {

        Preference preference = findPreference("timeout");

        //setIcon(preference, R.drawable.ic_timeout);

        preference.setSummary(getString(R.string.request_timeout_summary, String.valueOf(Settings.getRequestTimeout(getActivity()))));

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                String[] seconds = new String[6];

                for (int i = 0; i < seconds.length; i++) {
                    seconds[i] = getString(R.string.number_seconds, String.valueOf((i + 1) * 10));
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.request_timeout);

                builder.setItems(seconds, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.setTimeout(getActivity(), (which + 1) * 10);
                        preference.setSummary(getString(R.string.request_timeout_summary, String.valueOf(Settings.getRequestTimeout(getActivity()))));
                    }
                });

                builder.show();
                return true;
            }
        });
    }


    private void initPrefEllipsize() {
        ATESwitchPreference switchPreference = (ATESwitchPreference) findPreference("ellipsize");

        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                FragmentMain.SHOULD_RELOAD = true;
                return true;
            }
        });
    }


    private void initPrefNotification() {

        Preference preference = findPreference("notification");

        //setIcon(preference, R.drawable.ic_noti);

        updateNotification(preference);

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                String[] items = new String[] { getString(R.string.none), getString(R.string.toast), getString(R.string.notification), getString(R.string.smart_mode) };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.notifications);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_NONE);
                                break;

                            case 1:
                                Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_TOAST);
                                break;

                            case 2:
                                Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_NOTIFICATION);
                                break;

                            case 3:
                                Settings.setNotificationType(getActivity(), Settings.NOTIFICATION_TYPE_SMART);
                                break;
                        }

                        updateNotification(preference);
                    }
                });

                builder.show();
                return true;
            }
        });
    }


    private void updateNotification(Preference preference) {

        switch (Settings.getNotificationType(getActivity())) {

            case Settings.NOTIFICATION_TYPE_NONE:
                preference.setSummary(getString(R.string.no_notification));
                break;

            case Settings.NOTIFICATION_TYPE_NOTIFICATION:
                preference.setSummary(getString(R.string.notifications_summary, getString(R.string.notification)));
                break;

            case Settings.NOTIFICATION_TYPE_TOAST:
                preference.setSummary(getString(R.string.notifications_summary, getString(R.string.toast)));
                break;

            case Settings.NOTIFICATION_TYPE_SMART:
                preference.setSummary(getString(R.string.smart_mode_summary));
                break;
        }
    }


    private void initPrefPin() {

        final Preference preference = findPreference("pin");

        /*switch (pref.getString(PREF_THEME, PREF_DARK_THEME)) {

            case PREF_BLACK_THEME:
            case PREF_DARK_THEME:
                setIcon(preference, R.drawable.ic_lock);
                break;

            case PREF_LIGHT_THEME:
                setIcon(preference, R.drawable.ic_lock_light);
                break;
        }*/


        preference.setSummary(Settings.isPinProtected(getActivity()) ? R.string.pin_set : R.string.pin_protection_summary);

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {

                if (pref.getBoolean("pin_disclaimer", true)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle(R.string.note)
                            .setMessage(R.string.pin_warning)
                            .setPositiveButton(R.string.understand, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    pref.edit().putBoolean("pin_disclaimer", false).apply();
                                    checkPin(preference);
                                }
                            })
                            .show();

                } else {
                    checkPin(preference);
                }

                return true;
            }
        });
    }


    void checkPin(final Preference preference) {
        if (Settings.isPinProtected(getActivity())) {

            final Dialog d = new Dialog(getContext());
            //d.setTitle(R.string.enter_pin);

            d.setContentView(new PinView((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.view_pin, null, null) {
                public void onEnter(String password) {
                    super.onEnter(password);

                    if (Settings.checkPin(getActivity(), password)) {
                        super.onEnter(password);
                        Settings.setPin(getActivity(), null);

                        preference.setSummary(R.string.pin_protection_summary);

                        Toast.makeText(getActivity(), getString(R.string.pin_disabled), Toast.LENGTH_SHORT).show();

                        d.dismiss();
                        return;
                    }

                    Toast.makeText(getActivity(), getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                }

                public void onCancel() {
                    super.onCancel();
                    d.dismiss();
                }

            }.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            d.show();

        } else {

            setPin(preference);
        }
    }


    void setPin(final Preference preference) {

        final Dialog d = new Dialog(getContext());
        //d.setTitle(R.string.enter_new_pin);

        d.setContentView(new PinView((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.view_pin, null, null) {

            public void onEnter(String password) {
                super.onEnter(password);
                confirmPin(password, preference);
                d.dismiss();
            }

            public void onCancel() {
                super.onCancel();
                d.dismiss();
            }

        }.getView());

        d.show();
    }


    void confirmPin(final String pin, final Preference preference) {

        final Dialog d = new Dialog(getContext());
        //d.setTitle(R.string.confirm_pin);

        d.setContentView(new PinView((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.view_pin, null, null) {
            public void onEnter(String password) {
                super.onEnter(password);

                if (pin.equals(password)) {
                    Settings.setPin(getActivity(), password);

                    preference.setSummary(Settings.isPinProtected(getActivity()) ? R.string.pin_set : R.string.pin_protection_summary);

                    if (password != null && password.length() > 0)
                        Toast.makeText(getActivity(), getString(R.string.pin_set), Toast.LENGTH_SHORT).show();
                    d.dismiss();
                    return;
                }

                Toast.makeText(getActivity(), getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show();
            }

            public void onCancel() {
                super.onCancel();
                d.dismiss();
            }

        }.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        d.show();
    }
}
