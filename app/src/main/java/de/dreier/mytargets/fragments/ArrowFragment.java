/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.recyclerviewchoicemode.SelectableViewHolder;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.SimpleFragmentActivity;
import de.dreier.mytargets.adapters.NowListAdapter;
import de.dreier.mytargets.shared.models.Arrow;
import de.dreier.mytargets.utils.RoundedAvatarDrawable;

public class ArrowFragment extends NowListFragment<Arrow> implements View.OnClickListener{

    @Override
    protected void init(Bundle intent, Bundle savedInstanceState) {
        itemTypeRes = R.plurals.arrow_selected;
        itemTypeDelRes = R.plurals.arrow_deleted;
        newStringRes = R.string.new_arrow;
        mEditable = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setList(db.getArrows(), new ArrowAdapter());
    }

    @Override
    protected void onEdit(Arrow item) {
        Intent i = new Intent(getActivity(), SimpleFragmentActivity.EditArrowActivity.class);
        i.putExtra(EditArrowFragment.ARROW_ID, item.getId());
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onClick(View v) {
        startActivity(SimpleFragmentActivity.EditArrowActivity.class);
    }

    protected class ArrowAdapter extends NowListAdapter<Arrow> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_image_details, parent, false);
            return new ViewHolder(itemView);
        }
    }

    public class ViewHolder extends SelectableViewHolder<Arrow> {
        private final TextView mName;
        private final ImageView mImg;

        public ViewHolder(View itemView) {
            super(itemView, mMultiSelector, ArrowFragment.this);
            mName = (TextView) itemView.findViewById(R.id.name);
            mImg = (ImageView) itemView.findViewById(R.id.image);
        }

        @Override
        public void bindCursor() {
            mName.setText(mItem.name);
            mImg.setImageDrawable(new RoundedAvatarDrawable(mItem.getThumbnail()));
        }
    }
}

