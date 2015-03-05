package de.dreier.mytargets.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;

import de.dreier.mytargets.R;
import de.dreier.mytargets.fragments.ArrowFragment;
import de.dreier.mytargets.fragments.BowFragment;
import de.dreier.mytargets.fragments.TrainingsFragment;

/**
* Created by Florian on 05.03.2015.
*/
public class MainTabsFragmentPagerAdapter extends FragmentPagerAdapter {
    private final Context context;

    public MainTabsFragmentPagerAdapter(ActionBarActivity context) {
        super(context.getSupportFragmentManager());
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TrainingsFragment();
        } else if (position == 1) {
            return new BowFragment();
        } else {
            return new ArrowFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
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
}
