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

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.core.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.koushikdutta.superuser.db.LogEntry;
import com.koushikdutta.superuser.db.SuDatabaseHelper;
import com.koushikdutta.superuser.db.SuperuserDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;
import com.koushikdutta.superuser.util.Util;
import com.koushikdutta.superuser.helper.Theme;
import com.koushikdutta.superuser.helper.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FragmentLog extends Fragment  {

    public interface LogCallback {
        void onChanged();
    }


    LinearLayout header;

    ImageView icon;

    SwitchCompat log, notification;

    ExpandableListView listView;


    LogCallback callback;

    List<String> listParent;
    HashMap<String, List<ListItem>> listChild;

    LogAdapter adapter;

    UidPolicy up;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_log, container, false);

        log = (SwitchCompat) view.findViewById(R.id.log_switch);
        notification = (SwitchCompat) view.findViewById(R.id.noti_switch);

        listView = (ExpandableListView) view.findViewById(R.id.list);

        ViewCompat.setNestedScrollingEnabled(listView, true);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        header = (LinearLayout) getActivity().findViewById(R.id.header);

        icon = (ImageView)getActivity().findViewById(R.id.icon);

        TextView title, subtitle, request, command;

        title = (TextView) getActivity().findViewById(R.id.title);
        subtitle = (TextView) getActivity().findViewById(R.id.subtitle);
        request = (TextView) getActivity().findViewById(R.id.request);
        command = (TextView) getActivity().findViewById(R.id.command);


        Intent intent = getActivity().getIntent();

        if (intent != null) {

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle != null) {
                String cmd = bundle.getString("command");

                int uid = bundle.getInt("uid", -1);

                int desiredUid = bundle.getInt("desiredUid", -1);

                if (uid != -1 && desiredUid != -1)
                    up = SuDatabaseHelper.get(getContext(), uid, desiredUid, cmd);
            }
        }


        if (up != null) {

            String app = up.username;
            if (app == null || app.length() == 0)
                app = String.valueOf(up.uid);

            icon.setImageDrawable(Util.loadPackageIcon(getActivity(), up.packageName));

            title.setTextColor(((ActivityLog)getActivity()).textToolbar);
            subtitle.setTextColor(((ActivityLog)getActivity()).textToolbar);
            request.setTextColor(((ActivityLog)getActivity()).textToolbar);
            command.setTextColor(((ActivityLog)getActivity()).textToolbar);

            title.setText(up.getName());

            subtitle.setText(up.packageName + ", " + app);

            request.setText("Requested UID: " + up.desiredUid);

            command.setText("Command: " + (TextUtils.isEmpty(up.command) ? getString(R.string.all_commands) : up.command));

            //getListView().setSelector(android.R.color.transparent);

        } else {
            callback = (LogCallback) getActivity();
        }


        LinearLayout logParent = (LinearLayout) getActivity().findViewById(R.id.log);
        LinearLayout notiParent = (LinearLayout) getActivity().findViewById(R.id.noti);

        int accent = ThemeStore.accentColor(getContext());

        TintHelper.setTint(log, accent, false);
        TintHelper.setTint(notification, accent, false);

        if (up == null) {
            log.setChecked(Settings.getLogging(getActivity()));

            notiParent.setVisibility(View.GONE);
            notification.setChecked(false);

        } else {
            log.setChecked(up.logging);

            notification.setChecked(up.notification);
        }


        logParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.setChecked(!log.isChecked());
            }
        });


        log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (up == null) {
                    Settings.setLogging(getActivity(), b);

                } else {
                    up.logging = b;
                    SuDatabaseHelper.setPolicy(getActivity(), up);
                }
            }
        });


        notiParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notification.setChecked(!notification.isChecked());
            }
        });

        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (up == null) {

                } else {
                    up.notification = notification.isChecked();
                    SuDatabaseHelper.setPolicy(getActivity(), up);
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        listParent = new ArrayList<>();
        listChild = new HashMap<>();

        ArrayList<LogEntry> logs;

        if (up != null) logs = SuperuserDatabaseHelper.getLogs(getActivity(), up, -1);
        else logs = SuperuserDatabaseHelper.getLogs(getActivity());

        //java.text.DateFormat formatDay = DateFormat.getDateFormat(getActivity());
        java.text.DateFormat formatTime = DateFormat.getTimeFormat(getActivity());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

        Calendar calendar = GregorianCalendar.getInstance();

        Calendar logCalender = GregorianCalendar.getInstance();

        for (LogEntry log : logs) {

            String time = formatTime.format(log.getDate());

            String title = time;
            if (up == null) title = log.getName();

            String summary = getString(log.getActionResource());

            String date = sdf.format(log.getDate());

            logCalender.setTime(log.getDate());

            if (logCalender.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {

                if (logCalender.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) date = getString(R.string.today);

                calendar.add(Calendar.DAY_OF_YEAR, -1);
                if (logCalender.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) date = getString(R.string.yesterday);

                calendar.add(Calendar.DAY_OF_YEAR, -1);
                if (logCalender.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) date = getString(R.string.day_before);

                calendar.add(Calendar.DAY_OF_YEAR, 2);
            }

            if (!listParent.contains(date)) listParent.add(date);

            List<ListItem> list = listChild.get(date);

            if (list == null) {
                list = new ArrayList<>();
                listChild.put(date, list);
            }

            list.add(new ListItem(null, title, time, summary, null));
        }

        adapter = new LogAdapter(getActivity());
        listView.setAdapter(adapter);

        if (listParent.size() > 0) listView.expandGroup(0);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        VectorDrawableCompat icon = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_delete, getContext().getTheme());

        if (!Theme.getThemeCurrent(PreferenceManager.getDefaultSharedPreferences(getContext())).equals(MainActivity.PREF_BLACK_THEME)) {
            icon.setColorFilter(
                    getActivity() instanceof ActivityLog ? ((ActivityLog) getActivity()).textToolbar : ((MainActivity)getActivity()).textToolbarDefault,
                    PorterDuff.Mode.SRC_ATOP);

        } else {
            icon.setColorFilter(0xffeaeaea, PorterDuff.Mode.SRC_ATOP);
        }


        if (up == null) {
            MenuItem delete = menu.add(R.string.delete).setIcon(icon);
            delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (up != null)
                        SuDatabaseHelper.delete(getActivity(), up);

                    else
                        SuperuserDatabaseHelper.deleteLogs(getActivity());

                    adapter.clear();
                    callback.onChanged();
                    //if (up != null) getActivity().finish();
                    return true;
                }
            });
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        callback = null;
    }


    private class LogAdapter extends BaseExpandableListAdapter {


        private class ViewHolderParent {
            TextView parent;
        }

        private class ViewHolderChild {
            TextView text1, text2, text3;
        }


        private Context context;


        LogAdapter(Context context) {
            this.context = context;
        }

        void clear() {
            listParent.clear();
            listChild.clear();

            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return listParent.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return listChild.get(listParent.get(i)).size();
        }

        @Override
        public Object getGroup(int i) {
            return listParent.get(i);
        }

        @Override
        public Object getChild(int i, int i1) {
            return listChild.get(listParent.get(i)).get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

            ViewHolderParent holder;

            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_item_log_parent, null);

                holder = new ViewHolderParent();

                holder.parent = (TextView) view.findViewById(R.id.parent);

                view.setTag(holder);

            } else {
                holder = (ViewHolderParent) view.getTag();
            }

            String s = listParent.get(i);

            if (s.equals(getString(R.string.today)) ||
                    s.equals(getString(R.string.yesterday)) ||
                    s.equals(getString(R.string.day_before))) {

                holder.parent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f);

            } else {
                holder.parent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            }

            holder.parent.setText(s);

            return view;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

            ViewHolderChild holder;

            if (view == null) {

                view = LayoutInflater.from(context).inflate(R.layout.list_item_log_child, null);

                holder = new ViewHolderChild();

                holder.text1 = (TextView) view.findViewById(R.id.text1);
                holder.text2 = (TextView) view.findViewById(R.id.text2);
                holder.text3 = (TextView) view.findViewById(R.id.text3);

                view.setTag(holder);


            } else {
                holder = (ViewHolderChild) view.getTag();
            }

            ListItem item = ((ListItem) getChild(i, i1));

            if (up == null) {
                holder.text1.setVisibility(View.VISIBLE);
                holder.text1.setText(item.getItem1());
                holder.text2.setText(item.getItem2());

            } else {
                holder.text1.setVisibility(View.GONE);
                holder.text2.setText(item.getItem1());
            }

            holder.text3.setText(item.getItem3());

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }
}
