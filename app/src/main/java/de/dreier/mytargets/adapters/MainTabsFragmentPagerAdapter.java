/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import de.dreier.mytargets.R;
import de.dreier.mytargets.fragments.ArrowFragment;
import de.dreier.mytargets.fragments.BowFragment;
import de.dreier.mytargets.fragments.NowListFragmentBase;
import de.dreier.mytargets.fragments.TrainingsFragment;

public class MainTabsFragmentPagerAdapter extends FragmentPagerAdapter {
    private final Context context;
    private NowListFragmentBase[] fragments = new NowListFragmentBase[3];
    {
        fragments[0] = new TrainingsFragment();
        fragments[1] = new BowFragment();
        fragments[2] = new ArrowFragment();
    }

    public MainTabsFragmentPagerAdapter(AppCompatActivity context) {
        super(context.getSupportFragmentManager());
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.training);
        } else if (position == 1) {
            return context.getString(R.string.bow);
        } else {
            return context.getString(R.string.arrow);
        }
    }

    public NowListFragmentBase getFragment(int i) {
        return fragments[i];
    }
}
