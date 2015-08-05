/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.dreier.mytargets.R;
import de.dreier.mytargets.managers.DatabaseManager;
import de.dreier.mytargets.shared.models.RoundTemplate;
import de.dreier.mytargets.shared.models.StandardRound;

public class StandardRoundsItemAdapter extends ArrayAdapter<StandardRound> {
    private final Context mContext;
    private StandardRound standardRound;

    public StandardRoundsItemAdapter(Context context) {
        super(context, 0, DatabaseManager.getInstance(context).getStandardRounds());
        mContext = context;
    }

    @Override
    public StandardRound getItem(int position) {
        return standardRound != null ? standardRound : super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_standard_round, parent, false);
        }

        TextView name = (TextView) v.findViewById(android.R.id.text1);
        TextView desc = (TextView) v.findViewById(android.R.id.text2);
        ImageView image = (ImageView) v.findViewById(R.id.image);

        StandardRound item = getItem(position);
        name.setText(item.getName());
        desc.setText(item.getDescription(mContext));
        RoundTemplate firstRound = item.getRounds().get(0);
        image.setImageDrawable(firstRound.target);
        return v;
    }

    public void setStandardRound(StandardRound sr) {
        standardRound = sr;
    }

    public StandardRound getStandardRound() {
        return standardRound;
    }
}