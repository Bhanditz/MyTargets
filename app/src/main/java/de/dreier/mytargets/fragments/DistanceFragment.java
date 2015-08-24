/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.Assert;

import de.dreier.mytargets.R;
import de.dreier.mytargets.adapters.DistanceTabsFragmentPagerAdapter;
import de.dreier.mytargets.shared.models.Dimension;
import de.dreier.mytargets.shared.models.Distance;
import de.dreier.mytargets.utils.TextInputDialog;

public class DistanceFragment extends Fragment implements View.OnClickListener,
        TextInputDialog.OnClickListener {

    public static final String CUR_DISTANCE = "distance";
    private NowListFragment.OnItemSelectedListener listener;
    private long distance;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        distance = getArguments().getLong(CUR_DISTANCE);

        View rootView = inflater.inflate(R.layout.fragment_distance, container, false);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        DistanceTabsFragmentPagerAdapter adapter =
                new DistanceTabsFragmentPagerAdapter(getActivity(), distance);
        viewPager.setAdapter(adapter);
        int item = Distance.fromId(distance).unit.equals(Distance.METER) ? 0 : 1;
        viewPager.setCurrentItem(item, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.sliding_tabs);
        tabLayout.setTabTextColors(0xCCFFFFFF, Color.WHITE);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof NowListFragment.OnItemSelectedListener) {
            this.listener = (NowListFragment.OnItemSelectedListener) activity;
        }
        Assert.assertNotNull(listener);
    }

    @Override
    public void onClick(View v) {
        new TextInputDialog.Builder(getActivity())
                .setTitle(R.string.distance)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setSpinnerItems(new String[]{Dimension.METER, Dimension.YARDS})
                .setOnClickListener(this)
                .show();
    }

    @Override
    public void onCancelClickListener() {

    }

    @Override
    public void onOkClickListener(String input) {
        long distance = this.distance;
        try {
            int distanceVal = Integer.parseInt(input.replaceAll("[^0-9]", ""));
            String unit;
            if (input.endsWith(Dimension.METER)) {
                unit = Dimension.METER;
            } else {
                unit = Dimension.YARDS;
            }
            distance = new Distance(distanceVal, unit).getId();
        } catch (NumberFormatException e) {
            // leave distance as it is
        }
        listener.onItemSelected(distance, Distance.class);
    }

}
