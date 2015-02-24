package de.dreier.mytargets.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import de.dreier.mytargets.R;
import de.dreier.mytargets.fragments.StatisticsFragment;

public class StatisticsActivity extends ActionBarActivity {
    public static final String ROUND_ID = "round_id";
    public static final String TRAINING_ID = "training_id";
    private long mTraining;
    private long mRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mTraining = getIntent().getLongExtra(TRAINING_ID, -1);
        mRound = getIntent().getLongExtra(ROUND_ID, -1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new StatisticsPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(mRound == -1 ? 1 : 2, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class StatisticsPagerAdapter extends FragmentStatePagerAdapter {
        public StatisticsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            StatisticsFragment fragment = new StatisticsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(StatisticsFragment.ARG_POSITION, position);
            bundle.putLong(StatisticsFragment.ARG_TRAINING_ID, mTraining);
            bundle.putLong(StatisticsFragment.ARG_ROUND_ID, mRound);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mRound == -1 ? (mTraining == -1 ? 1 : 2) : 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.all_trainings);
                case 1:
                    return getString(R.string.entire_training);
                default:
                    return getString(R.string.current_round);
            }
        }
    }
}
