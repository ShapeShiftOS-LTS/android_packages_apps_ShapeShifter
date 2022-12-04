/*
 * Copyright (C) 2020 ShapeShiftOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssos.shapeshifter;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.viewpager.widget.ViewPager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.ssos.shapeshifter.tabs.ActionsTab;
import com.ssos.shapeshifter.tabs.InterfaceTab;
import com.ssos.shapeshifter.tabs.StatusBarTab;
import com.ssos.shapeshifter.tabs.LockScreenTab;
import com.ssos.shapeshifter.tabs.SystemMiscTab;

import com.ssos.shapeshifter.navigation.BubbleNavigationConstraintView;
import com.ssos.shapeshifter.navigation.BubbleNavigationChangeListener;

public class ShapeShifterSettings extends SettingsPreferenceFragment {

    private static final String TAG = "ShapeShifterSettings";

    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Resources res = getResources();
        Window win = getActivity().getWindow();
        mContext = getActivity();
        View view = inflater.inflate(R.layout.shapeshifter, container, false);

        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        win.setNavigationBarColor(res.getColor(R.color.shapeshifter_navbar_color));
        win.setNavigationBarDividerColor(res.getColor(R.color.shapeshifter_navbar_color));

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.shapeshifter_title);
        }

        BubbleNavigationConstraintView bubbleNavigationConstraintView =  (BubbleNavigationConstraintView) view.findViewById(R.id.bottom_navigation_view_constraint);
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        PagerAdapter mPagerAdapter = new PagerAdapter(getFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        bubbleNavigationConstraintView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                if (view.getId() == R.id.tab_actions) {
                    viewPager.setCurrentItem(position, true);
                } else if (view.getId() == R.id.tab_interface) {
                    viewPager.setCurrentItem(position, true);
                } else if (view.getId() == R.id.tab_statusbar) {
                    viewPager.setCurrentItem(position, true);
                } else if (view.getId() == R.id.tab_lockscreen) {
                    viewPager.setCurrentItem(position, true);
                } else if (view.getId() == R.id.tab_system_misc) {
                    viewPager.setCurrentItem(position, true);
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                bubbleNavigationConstraintView.setCurrentActiveItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    class PagerAdapter extends FragmentPagerAdapter {

        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        PagerAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new ActionsTab();
            frags[1] = new InterfaceTab();
            frags[2] = new StatusBarTab();
            frags[3] = new LockScreenTab();
            frags[4] = new SystemMiscTab();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
            getString(R.string.navigation_actions_title),
            getString(R.string.navigation_interface_title),
            getString(R.string.navigation_statusbar_title),
            getString(R.string.navigation_lockscreen_title),
            getString(R.string.navigation_system_title)};

        return titleString;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 0, 0, R.string.shapeshiftos_about_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

            AboutShapeShiftOS newFragment = AboutShapeShiftOS .newInstance();
            newFragment.show(ft, "AboutShapeShiftOS");
            return true;
        }
        return false;
    }
}
