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
import android.widget.TextView;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.ItemSelectActivity;
import de.dreier.mytargets.models.WindDirection;

import static de.dreier.mytargets.activities.ItemSelectActivity.ITEM;

public class WindDirectionDialogSpinner extends DialogSpinner<WindDirection> {

    public WindDirectionDialogSpinner(Context context) {
        this(context, null);
    }

    public WindDirectionDialogSpinner(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.item_simple_text);
        setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ItemSelectActivity.WindDirection.class);
            i.putExtra(ITEM, item);
            startIntent(i, data -> setItem((WindDirection) data.getSerializableExtra(ITEM)));
        });
    }

    @Override
    protected void bindView() {
        TextView name = (TextView) mView.findViewById(android.R.id.text1);
        name.setText(item.name);
    }

    public void setItemId(long direction) {
        setItem(WindDirection.getList(getContext()).get((int) direction));
    }
}
