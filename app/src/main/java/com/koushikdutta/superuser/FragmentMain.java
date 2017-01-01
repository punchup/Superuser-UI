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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.koushikdutta.superuser.db.SuDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;
import com.koushikdutta.superuser.helper.Settings;
import com.koushikdutta.superuser.helper.Theme;
import com.koushikdutta.superuser.helper.recycler.GridDividerItemDecoration;
import com.koushikdutta.superuser.helper.recycler.GridTopOffsetItemDecoration;
import com.koushikdutta.superuser.helper.recycler.StartOffsetItemDecoration;
import com.koushikdutta.superuser.util.Util;
import com.koushikdutta.superuser.view.PinView;
import com.koushikdutta.superuser.view.RecyclerViewSwipeable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.koushikdutta.superuser.MainActivity.data;
import static com.koushikdutta.superuser.MainActivity.logCount;


public class FragmentMain extends Fragment {

    public interface MainCallback {
        void onListChanged(int which);
        void onGridSpanChanged(int which, int val);
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //load(); This creates an unpleasant flicker. Better to change a single item

            if (intent == null || intent.getExtras() == null || adapter == null) return;

            String pkg = intent.getStringExtra("pkg");

            int pos = adapter.getPositionByPackage(pkg);

            if (pos == -1) return;

            String log = logCount.get(pkg);

            if (log == null) {
                if (intent.getStringExtra("policy").equals(UidPolicy.ALLOW))
                    log = "1+0";

                else if (intent.getStringExtra("policy").equals(UidPolicy.DENY))
                    log = "0+1";

                logCount.put(pkg, log);

                ListItem item = adapter.getVisible().get(pos);

                int i = data.indexOf(item);

                item.setItem3(getString(R.string.today));

                data.set(i, item);

            } else {
                if (intent.getStringExtra("policy").equals(UidPolicy.ALLOW))
                    log = log.replace(log.substring(0, log.indexOf("+")), String.valueOf(Integer.parseInt(log.split("\\+")[0]) + 1));

                else if (intent.getStringExtra("policy").equals(UidPolicy.DENY))
                    log = log.replace(log.substring(log.indexOf("+") + 1), String.valueOf(Integer.parseInt(log.split("\\+")[1]) + 1));

                for (Map.Entry<String, String> entry : logCount.entrySet()) {
                    if (entry.getKey().equals(pkg))
                        entry.setValue(log);
                }
            }

            adapter.notifyItemChanged(pos);
        }
    };


    public static final int FRAGMENT_ALLOWED = 0;
    public static final int FRAGMENT_DENIED = 1;
    private static final String DATA_BUNDLE_KEY = "deleted";


    private CoordinatorLayout coordinatorLayout;

    //TabLayout tabLayout;

    //private ViewPager viewPager;

    private TextView empty;
    private RecyclerViewSwipeable recycler;


    private Context context;
    private SharedPreferences pref;

    private MainCallback callback;

    private RecyclerViewSwipeable.LayoutManagerSwipeable layoutManager;
    private AppAdapter adapter;


    static boolean SHOULD_RELOAD = false;
    private boolean gridMode;

    private int type = FRAGMENT_ALLOWED;

    private String policy;



    public static FragmentMain newInstance(int which) {

        FragmentMain fragment = new FragmentMain();

        Bundle bundle = new Bundle();

        bundle.putInt("which", which);

        fragment.setArguments(bundle);

        return fragment;
    }


    public FragmentMain() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        type = bundle.getInt("which");

        switch (type) {
            case FRAGMENT_ALLOWED:
                policy = UidPolicy.ALLOW; break;

            case FRAGMENT_DENIED:
                policy = UidPolicy.DENY; break;
        }

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        empty = (TextView) view.findViewById(R.id.empty);
        if (policy.equalsIgnoreCase(UidPolicy.ALLOW)) empty.setText(R.string.empty_allowed);
        else empty.setText(R.string.empty_denied);

        recycler = (RecyclerViewSwipeable) view.findViewById(R.id.recycler);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        callback = (MainCallback) getActivity();

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(Common.INTENT_FILTER_MAIN));


        gridMode = pref.getBoolean("grid_mode", true);


        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.main_content);

        //tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);

        //viewPager = (ViewPager) getActivity().findViewById(R.id.container);

        int span = 0;

        if (gridMode) {
            layoutManager = new RecyclerViewSwipeable.LayoutManagerSwipeable(context, 1);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                span = pref.getInt("grid_size_port", 3);
                layoutManager.setSpanCount(span);

            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                span = pref.getInt("grid_size_land", 4);
                layoutManager.setSpanCount(span);
            }

            Drawable divider = ContextCompat.getDrawable(context, R.drawable.divider_grid);
            divider.setColorFilter(new PorterDuffColorFilter(ATHUtil.resolveColor(context, R.attr.dividerGrid), PorterDuff.Mode.SRC_ATOP));

            recycler.setLayoutManager(layoutManager);

            recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.grid_layout_animation));

            recycler.addItemDecoration(new GridDividerItemDecoration(divider, divider, span));

            recycler.addItemDecoration(new GridTopOffsetItemDecoration(Util.toPx(context, 5), span));

        } else {
            recycler.setLayoutManager(new LinearLayoutManager(context));

            recycler.addItemDecoration(new StartOffsetItemDecoration(Util.toPx(context, 10)));
        }

        //recycler.setListener(clickListener);
        //recycler.setViewPager(viewPager);
        //recycler.setFragment(this);

        setData();
    }


    /*public Date getLastDate(int last) {
        return new Date((long) last * 1000);
    }*/


    public void setData() {
        if (adapter == null) {
            adapter = new AppAdapter(context);
            recycler.setAdapter(adapter);
        }

        adapter.filter();

        setEmpty();
    }


    private void setEmpty() {
        if (adapter.getVisible().isEmpty()) empty.setVisibility(View.VISIBLE);
        else empty.setVisibility(View.GONE);
    }


    public void setSpan(int val) {
        layoutManager.setSpanCount(val);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem mode = menu.add(Menu.NONE, Menu.NONE, 100, R.string.list_mode);
        mode.setTitle(pref.getBoolean("grid_mode", true) ? R.string.list_mode : R.string.grid_mode);
        mode.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        mode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (pref.edit().putBoolean("grid_mode", !pref.getBoolean("grid_mode", true)).commit())
                    getActivity().recreate();
                return true;
            }
        });

        if (!gridMode) return;

        MenuItem gridSize = menu.add(Menu.NONE, Menu.NONE, 101, R.string.grid_size);
        gridSize.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        gridSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int sizePort = pref.getInt("grid_size_port", 3);
                int sizeLand = pref.getInt("grid_size_land", 4);

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                View parent = LayoutInflater.from(context).inflate(R.layout.dialog_settings_grid_size, null);

                final AppCompatSeekBar seekPort = (AppCompatSeekBar) parent.findViewById(R.id.seek_port);
                seekPort.setProgress(sizePort - 3);

                final AppCompatSeekBar seekLand = (AppCompatSeekBar) parent.findViewById(R.id.seek_land);
                seekLand.setProgress(sizeLand - 3);

                builder.setView(parent);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putInt("grid_size_port", seekPort.getProgress() + 3);
                        editor.putInt("grid_size_land", seekLand.getProgress() + 3).apply();

                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            int val = seekPort.getProgress() + 3;
                            layoutManager.setSpanCount(val);
                            callback.onGridSpanChanged(type, val);

                        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            int val = seekLand.getProgress() + 3;
                            layoutManager.setSpanCount(val);
                            callback.onGridSpanChanged(type, val);
                        }
                    }
                });

                builder.show();
                return true;
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        callback = null;

        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }


    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            LinearLayout parent, overlayParent, titleParent;
            RelativeLayout iconParent;

            ImageView icon, left, right, menu;
            TextView title, subtitle, counter, counterIndicator;

            ViewHolder(View itemView) {
                super(itemView);

                parent = (LinearLayout) itemView.findViewById(R.id.parent);
                iconParent = (RelativeLayout) itemView.findViewById(R.id.icon_parent);
                overlayParent = (LinearLayout) itemView.findViewById(R.id.overlay_parent);
                titleParent = (LinearLayout) itemView.findViewById(R.id.title_parent);

                icon = (ImageView) itemView.findViewById(R.id.icon);
                left = (ImageView) itemView.findViewById(R.id.permission_left);
                right = (ImageView) itemView.findViewById(R.id.permission_right);
                menu = (ImageView) itemView.findViewById(R.id.menu);

                title = (TextView) itemView.findViewById(R.id.title);
                subtitle = (TextView) itemView.findViewById(R.id.subtitle);

                counter = (TextView) itemView.findViewById(R.id.counter);
                counterIndicator = (TextView) itemView.findViewById(R.id.counter_indicator);
            }
        }


        Context context;

        List<ListItem> visible;

        int counterBack, counterText;


        AppAdapter(Context context) {
            this.context = context;

            this.visible = new ArrayList<>();
            //filter();

            int defBack = 0xff555555;

            switch (((MainActivity)getActivity()).theme) {
                case MainActivity.PREF_LIGHT_THEME:
                    defBack = 0xff656565;
            }

            counterBack = Theme.getCounterBackColor(pref, defBack);
            counterText = Theme.getCounterTextColor(pref);
        }


        List<ListItem> getVisible() {
            return visible;
        }


        @Override
        public int getItemCount() {
            return visible.size();
        }


        int getPositionByPackage(String pkg) {
            for (ListItem item : visible) {
                if (item.getPolicy().packageName.equals(pkg))
                    return visible.indexOf(item);
            }

            return -1;
        }


        void remove(int pos) {
            visible.remove(pos);
            notifyItemRemoved(pos);
        }


        /*void setPolicy(String policy) {
            filter(policy);
        }*/


        private void filter () {
            int size = visible.size();

            if (size > 0) {
                /*for (int i = 0; i < size; i++) Not needed anymore
                    visible.remove(0);*/

                visible.clear();

                notifyItemRangeRemoved(0, size);
            }

            for (ListItem item : data) {
                if (item.getItem1().equals(policy)) visible.add(item);
            }

            notifyItemRangeInserted(0, visible.size());
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new ViewHolder(
                    LayoutInflater.from(context)
                            .inflate(gridMode ? R.layout.grid_item : R.layout.list_item,
                                    parent,
                                    false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            final ListItem item = visible.get(position);

            holder.icon.setImageDrawable(item.getIcon());

            holder.title.setText(item.getItem2());

            if (!pref.getBoolean("ellipsize", false))
                holder.title.setMaxLines(2);

            holder.title.post(new Runnable() {
                @Override
                public void run() {
                    if (holder.title.getLineCount() > 1) {
                        holder.titleParent.setPadding(Util.toPx(context, 10), Util.toPx(context, 2), Util.toPx(context, 10), Util.toPx(context, 3));

                    } else {
                        holder.titleParent.setPadding(Util.toPx(context, 10), Util.toPx(context, 10), Util.toPx(context, 10), Util.toPx(context, 10));
                    }
                }
            });

            String count = logCount.get(item.getPolicy().packageName);

            if (count != null) {
                if (policy.equalsIgnoreCase(UidPolicy.ALLOW)) {
                    count = count.split("\\+")[0];
                    if (!gridMode) holder.counterIndicator.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                } else {
                    count = count.split("\\+")[1];
                    if (!gridMode) holder.counterIndicator.setTextColor(0xffc62828);
                }

                if (count.length() == 1) count = "0" + count;
                holder.counter.setText(count);
            }

            if (item.getItem3() != null && Integer.parseInt(count) > 0) {
                holder.subtitle.setVisibility(View.VISIBLE);
                holder.counter.setVisibility(View.VISIBLE);
                if (!gridMode) holder.counterIndicator.setVisibility(View.VISIBLE);

                holder.subtitle.setText(item.getItem3());

            } else {
                holder.subtitle.setVisibility(View.GONE);
                holder.counter.setVisibility(View.GONE);
                if (!gridMode) holder.counterIndicator.setVisibility(View.GONE);
            }

            if (gridMode) handleGridItem(holder, item);
            else handleListItem(holder, item);
        }


        private void handleGridItem(final ViewHolder holder, final ListItem item) {

            holder.overlayParent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });

            setClickListener(holder.icon, holder.icon, item);

            holder.icon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(item.getPolicy().packageName);
                    if (intent != null) startActivity(intent);
                    return false;
                }
            });

            holder.counter.setTextColor(counterText);

            GradientDrawable counterBackDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.counter_back);
            counterBackDrawable.setColor(counterBack);

            holder.counter.setBackground(counterBackDrawable);

            holder.titleParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.overlayParent.getVisibility() == View.GONE)
                        showOverlay(holder);

                    else
                        hideOverlay(holder);
                }
            });

            if (policy.equalsIgnoreCase(UidPolicy.ALLOW)) {
                holder.left.setImageResource(R.drawable.ic_deny);
                holder.left.setColorFilter(0xfff44336);

            } else {
                holder.left.setImageResource(R.drawable.ic_allow);
                holder.left.setColorFilter(0xff4caf50);
            }

            holder.left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideOverlay(holder);

                    if (Settings.isPinProtected(context))
                        handlePin(0, holder, item);
                    else
                        showSnack(0, holder, item);
                }
            });

            holder.right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideOverlay(holder);

                    if (Settings.isPinProtected(context))
                        handlePin(1, holder, item);
                    else
                        showSnack(1, holder, item);
                }
            });
        }


        private void showOverlay(final ViewHolder holder) {
            final Animation up = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            final Animation down = AnimationUtils.loadAnimation(context, R.anim.abc_slide_out_bottom);

            down.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.iconParent.setVisibility(View.GONE);
                    holder.overlayParent.startAnimation(up);
                    holder.overlayParent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            holder.iconParent.startAnimation(down);

        }


        private void hideOverlay(final ViewHolder holder) {
            final Animation up = AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
            final Animation down = AnimationUtils.loadAnimation(context, R.anim.abc_slide_out_bottom);

            down.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.overlayParent.setVisibility(View.GONE);
                    holder.iconParent.startAnimation(up);
                    holder.iconParent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            holder.overlayParent.startAnimation(down);
        }


        private void handleListItem(final ViewHolder holder, final ListItem item) {

            setClickListener(holder.titleParent, holder.icon, item);

            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.icon.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(item.getPolicy().packageName);
                            if (intent != null) startActivity(intent);
                        }
                    }, 200);
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(context, holder.menu);

                    Menu menu = popupMenu.getMenu();

                    if (policy.equalsIgnoreCase(UidPolicy.ALLOW)) {
                        setMenu(0, menu.add(getString(R.string.deny)), holder, item);

                    } else {
                        setMenu(0, menu.add(getString(R.string.allow)), holder, item);
                    }

                    setMenu(1, menu.add(getString(R.string.revoke)), holder, item);

                    popupMenu.show();
                }
            });
        }


        private void setMenu(final int which, MenuItem menuItem, final ViewHolder holder, final ListItem item) {

            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    switch (which) {
                        case 0:
                            if (Settings.isPinProtected(context))
                                handlePin(0, holder, item);
                            else
                                showSnack(0, holder, item);
                            break;

                        case 1:
                            if (Settings.isPinProtected(context))
                                handlePin(1, holder, item);
                            else
                                showSnack(1, holder, item);

                            break;
                    }

                    return false;
                }
            });
        }


        private void handlePin(final int which, final ViewHolder holder, final ListItem item) {
            final Dialog d = new Dialog(getContext());

            d.setContentView(new PinView((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.view_pin, null, null) {
                public void onEnter(String password) {
                    super.onEnter(password);

                    if (Settings.checkPin(getActivity(), password)) {
                        d.dismiss();
                        handleRequest(which, holder, item);
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
        }


        private void showSnack(final int which, final ViewHolder holder, final ListItem item) {
            Snackbar.make(coordinatorLayout, R.string.confirmation, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handleRequest(which, holder, item);
                        }

                    }).show();

        }


        private void handleRequest(int which, ViewHolder holder, ListItem item) {
            final UidPolicy up = item.getPolicy();

            switch (which) {
                case 0: changePolicy(holder, up, item); break;
                case 1: revoke(holder, up, item); break;
            }
        }


        private void changePolicy(ViewHolder holder, UidPolicy up, ListItem item) {
            int i = data.indexOf(item);

            item.setItem1(policy.equals(UidPolicy.ALLOW) ? UidPolicy.DENY : UidPolicy.ALLOW);

            up.setPolicy(policy.equals(UidPolicy.ALLOW) ? UidPolicy.DENY : UidPolicy.ALLOW);

            SuDatabaseHelper.setPolicy(context, up);

            data.set(i, item);

            remove(holder.getAdapterPosition());
            callback.onListChanged(type);

            setEmpty();
        }


        private void revoke(final ViewHolder holder, final UidPolicy up, final ListItem item) {
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.getData().getBoolean(DATA_BUNDLE_KEY)) {

                        data.remove(data.indexOf(item));

                        remove(holder.getAdapterPosition());

                        setEmpty();

                    } else
                        showErrorDialog(up, R.string.db_delete_error);
                }
            };

            new Thread() {
                public void run() {
                    final boolean done = SuDatabaseHelper.delete(getActivity(), up);
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(DATA_BUNDLE_KEY, done);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }.start();
        }


        private void showErrorDialog(UidPolicy policy, int resource) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(policy.name)
                    .setIcon(Util.loadPackageIcon(getActivity(), policy.packageName))
                    .setMessage(getResources().getText(resource))
                    .setCancelable(true)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


        private void setClickListener(View view, final View icon, final ListItem item) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    recycler.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent intent = new Intent(context, ActivityLog.class);

                            UidPolicy up = item.getPolicy();

                            if (up != null) {
                                Bundle args = new Bundle();
                                args.putString("name", up.getName());
                                args.putString("command", up.command);
                                args.putInt("uid", up.uid);
                                args.putInt("desiredUid", up.desiredUid);

                                intent.putExtra("bundle", args);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                    Pair<View, String> pair2 = Pair.create(icon, "icon");
                                    //Pair<View, String> pair1 = Pair.create(parent.findViewById(R.id.title), "title");

                                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()/*, pair1*/, pair2);

                                    startActivity(intent, options.toBundle());

                                } else {
                                    startActivity(intent);
                                }
                            }
                        }
                    }, 200);
                }
            });
        }
    }
}