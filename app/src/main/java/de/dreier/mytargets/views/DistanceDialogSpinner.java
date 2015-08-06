/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import de.dreier.mytargets.activities.ItemSelectActivity;
import de.dreier.mytargets.adapters.DistanceItemAdapter;
import de.dreier.mytargets.fragments.DistanceFragment;


public class DistanceDialogSpinner extends DialogSpinner {

    public DistanceDialogSpinner(Context context) {
        super(context);
        init();
    }

    public DistanceDialogSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setAdapter(new DistanceItemAdapter(getContext()));
        setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ItemSelectActivity.Distance.class);
            i.putExtra(DistanceFragment.CUR_DISTANCE, getSelectedItemId());
            startIntent(i);
        });
        setOnResultListener(data -> setItemId(data.getLongExtra("id", -1)));
    }

    @Override
    public void setItemId(long id) {
        setAdapter(new DistanceItemAdapter(getContext(), (int) id));
        super.setItemId(id);
    }
}
