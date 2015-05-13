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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.dreier.mytargets.R;
import de.dreier.mytargets.shared.models.Target;

public class TargetItemAdapter extends BaseAdapter {
    private final Context mContext;

    public TargetItemAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return Target.list.size();
    }

    @Override
    public Object getItem(int i) {
        return Target.list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.image_item, parent, false);
        }

        ImageView img = (ImageView) v.findViewById(R.id.image);
        TextView desc = (TextView) v.findViewById(R.id.name);

        img.setImageResource(Target.list.get(position).drawableRes);
        desc.setText(Target.list.get(position).name);
        return v;
    }
}