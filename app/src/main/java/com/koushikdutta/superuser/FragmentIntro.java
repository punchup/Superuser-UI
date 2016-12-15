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

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentIntro extends Fragment {

    public static final String ARG_LAYOUT_RES_ID = "layoutResId";

    private int layoutResId;


    public static FragmentIntro newInstance(int layoutResId) {
        FragmentIntro startupFragment = new FragmentIntro();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        startupFragment.setArguments(args);

        return startupFragment;
    }


    public FragmentIntro() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View parent = inflater.inflate(layoutResId, container, false);

        if (layoutResId != R.layout.fragment_intro_0) return parent;

        final ImageView superuser = (ImageView) parent.findViewById(R.id.superuser);
        final ImageView background = (ImageView) parent.findViewById(R.id.superuser_back);

        final TextView title = (TextView) parent.findViewById(R.id.title);
        final TextView desc = (TextView) parent.findViewById(R.id.tour);

        final Animation fadeIn, zoomIn;

        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        zoomIn = AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                background.startAnimation(zoomIn);
                background.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Animation fadeIn2 = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                fadeIn2.setDuration(1000);

                fadeIn2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation fadeIn3 = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                        fadeIn3.setDuration(400);

                        fadeIn3.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ((ActivityIntro)getActivity()).setProgressButtonEnabled(true);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        desc.startAnimation(fadeIn3);
                        desc.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                title.startAnimation(fadeIn2);
                title.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superuser.startAnimation(fadeIn);

                superuser.setVisibility(View.VISIBLE);
            }
        }, 600);

        return parent;
    }
}
