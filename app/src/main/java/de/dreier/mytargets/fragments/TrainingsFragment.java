package de.dreier.mytargets.fragments;

import android.content.Intent;
import android.os.Bundle;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.EditRoundActivity;
import de.dreier.mytargets.activities.TrainingActivity;
import de.dreier.mytargets.adapters.TrainingAdapter;

/**
 * Shows an overview over all trying days
 */
public class TrainingsFragment extends NowListFragment {

    @Override
    protected void init(Bundle intent, Bundle savedInstanceState) {
        itemTypeRes = R.plurals.training;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter = new TrainingAdapter(getActivity());
        setListAdapter(adapter);
    }

    @Override
    protected void onDelete(long[] ids) {
        db.deleteTrainings(ids);
    }

    @Override
    public boolean onItemClick(Intent i, int pos, long id) {
        if (pos == 0) {
            i.setClass(getActivity(), EditRoundActivity.class);
        } else {
            i.setClass(getActivity(), TrainingActivity.class);
            i.putExtra(TrainingActivity.TRAINING_ID, getListAdapter().getItemId(pos));
        }
        return true;
    }
}
