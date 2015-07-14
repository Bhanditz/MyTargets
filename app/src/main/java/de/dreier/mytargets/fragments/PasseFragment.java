/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.recyclerviewchoicemode.SelectableViewHolder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.InputActivity;
import de.dreier.mytargets.activities.ScoreboardActivity;
import de.dreier.mytargets.activities.StatisticsActivity;
import de.dreier.mytargets.adapters.ExpandableNowListAdapter;
import de.dreier.mytargets.shared.models.Arrow;
import de.dreier.mytargets.shared.models.Bow;
import de.dreier.mytargets.shared.models.Passe;
import de.dreier.mytargets.shared.models.Round;
import de.dreier.mytargets.shared.models.StandardRound;
import de.dreier.mytargets.shared.models.target.Target;
import de.dreier.mytargets.shared.models.Training;
import de.dreier.mytargets.utils.ScoreboardImage;
import de.dreier.mytargets.utils.TargetImage;
import de.dreier.mytargets.views.PasseView;
import de.dreier.mytargets.views.TargetPasseView;

/**
 * Shows all passes of one round
 */
public class PasseFragment extends ExpandableNowListFragment<Round, Passe>
        implements ShareDialogFragment.ShareDialogListener/*, ObservableScrollViewCallbacks*/,
        View.OnClickListener {

    private long mTraining;

    private ArrayList<Round> mRounds;

    boolean target_equals = true;
    boolean distance_equals = true;
    private FloatingActionButton mFab;
    private boolean mTargetViewMode = false;
    private View mNewLayout;
    private TextView mNewText;

    public PasseFragment() {
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_training;
    }

    @Override
    protected void init(Bundle intent, Bundle savedInstanceState) {
        itemTypeRes = R.plurals.passe_selected;
        itemTypeDelRes = R.plurals.passe_deleted;
        newStringRes = R.string.new_round;

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(this);
        mNewLayout = rootView.findViewById(R.id.new_layout);
        mNewText = (TextView) rootView.findViewById(R.id.new_text);

        if (intent != null) {
            mTraining = intent.getLong(TRAINING_ID, -1);
        }
        if (savedInstanceState != null) {
            mTraining = savedInstanceState.getLong(TRAINING_ID, -1);
        }

        // Get UI elements
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        // Set up toolbar
        activity.setSupportActionBar(toolbar);
        Training tr = db.getTraining(mTraining);
        ActionBar actionBar = activity.getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(tr.title);
        actionBar.setSubtitle(DateFormat.getDateInstance().format(tr.date));
        actionBar.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set round info
        mRounds = db.getRounds(mTraining);
        setRoundInfo();

        setList(mRounds, db.getPasses(mTraining), true,
                new PasseAdapter());

        activity.supportInvalidateOptionsMenu();
    }

    @Override
    protected void updateFabButton(List list) {
        if (newStringRes != 0) {
            mNewLayout.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            mNewText.setText(newStringRes);
            mFab.setVisibility(View.VISIBLE);
        } else {
            mFab.setVisibility(View.GONE);
        }
    }

    void setRoundInfo() {
        TextView info = (TextView) activity.findViewById(R.id.detail_round_info);
        TextView score = (TextView) activity.findViewById(R.id.detail_score);

        Training training = db.getTraining(mTraining);
        StandardRound standardRound = db.getStandardRound(mTraining);
        boolean indoor = standardRound.indoor;
        long bowId = training.bow;
        long arrowId = training.arrow;

        // Set number of X, 10, 9 shoots
        String infoText = "<font color=#ffffff>X: <b>" + training.scoreCount[0] + "</b><br>" +
                getString(R.string.ten_x) + ": <b>" +
                (training.scoreCount[0] + training.scoreCount[1]) + "</b><br>" +
                getString(R.string.nine) + ": <b>" + training.scoreCount[2] + "</b></font>";
        score.setText(Html.fromHtml(infoText));

        String percent = training.maxPoints == 0 ? "" : " (" + (training.reachedPoints * 100 / training.maxPoints) + "%)";
        infoText = "<font color=#ffffff>" + getString(R.string.points) + ": <b>" + training.reachedPoints + "/" +
                        training.maxPoints + percent + "</b>";

        // Set round info
        Bow bow = db.getBow(bowId, true);
        if (bow != null) {
            infoText += "<br>" + getString(R.string.bow) +
                    ": <b>" + TextUtils.htmlEncode(bow.name) + "</b>";
        }

        Arrow arrow = db.getArrow(arrowId, true);
        if (arrow != null) {
            infoText += "<br>" + getString(R.string.arrow) +
                    ": <b>" + TextUtils.htmlEncode(arrow.name) + "</b>";
        }

        if (mRounds.size() == 0) {
            infoText += "</font>";
            info.setText(Html.fromHtml(infoText));
            return;
        }
        // Aggregate round information
        Round round = mRounds.get(0);
        String distance = round.info.distance.toString();
        Target target = round.info.target;
        target_equals = true;
        distance_equals = true;
        for (Round r : mRounds) {
            distance_equals = r.info.distance.toString().equals(distance) && distance_equals;
            target_equals = r.info.target.equals(target) && target_equals;
        }


        if (distance_equals) {
            infoText += "<br>" + getString(R.string.distance) + ": <b>" +
                    distance + " - " + getString(indoor ? R.string.indoor : R.string.outdoor) + "</b>";
        }
        if (target_equals) {
            infoText += "<br>" + getString(R.string.target_face) + ": <b>" +
                    target + "</b>";
        }

        infoText += "</font>";
        info.setText(Html.fromHtml(infoText));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.round, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean hasPasses = mAdapter.getItemCount() > 1;
        menu.findItem(R.id.action_scoreboard).setVisible(hasPasses);
        menu.findItem(R.id.action_share).setVisible(hasPasses);
        menu.findItem(R.id.action_statistics).setVisible(hasPasses);
        menu.findItem(R.id.action_view_mode).setVisible(hasPasses);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scoreboard:
                Intent intent = new Intent(activity, ScoreboardActivity.class);
                intent.putExtra(ScoreboardActivity.TRAINING_ID, mTraining);
                startActivity(intent);
                return true;
            case R.id.action_statistics:
                Intent i = new Intent(activity, StatisticsActivity.class);
                i.putExtra(StatisticsActivity.TRAINING_ID, mTraining);
                startActivity(i);
                return true;
            case R.id.action_share:
                showShareDialog();
                return true;
            case R.id.action_view_mode:
                mTargetViewMode = !mTargetViewMode;
                setList(mRounds, db.getPasses(mTraining), true,
                        new PasseAdapter());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void showShareDialog() {
        // Create an instance of the dialog fragment and show it
        ShareDialogFragment dialog = new ShareDialogFragment();
        dialog.setTargetFragment(this, 0);
        dialog.show(activity.getSupportFragmentManager(), "share_dialog");
    }

    /* Called after the user selected with items he wants to share */
    @Override
    public void onShareDialogConfirmed(final boolean include_text, final boolean dispersion_pattern, final boolean scoreboard, final boolean comments) {
        // Construct share intent
        mRounds = db.getRounds(mTraining);
        Training training = db.getTraining(mTraining);

        final String text = getString(R.string.my_share_text,
                training.scoreCount[0], training.scoreCount[1],
                training.scoreCount[2], training.reachedPoints, training.maxPoints);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final File f = File
                            .createTempFile("target", ".png", activity.getExternalCacheDir());
                    if (dispersion_pattern && !scoreboard && !comments) {
                        new TargetImage().generateBitmap(activity, 800, mTraining, f);
                    } else {
                        new ScoreboardImage()
                                .generateBitmap(activity, mTraining, scoreboard, dispersion_pattern,
                                        comments, f);
                    }

                    // Build and fire intent to ask for share provider
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    if (include_text) {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    }
                    if (dispersion_pattern || scoreboard || comments) {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                    }
                    shareIntent.setType("*/*");
                    startActivity(shareIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, getString(R.string.sharing_failed), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }).start();
    }

    /*private final View.OnClickListener headerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(activity, SimpleFragmentActivity.EditRoundActivity.class);
            i.putExtra(SimpleFragmentActivity.EditRoundActivity.TRAINING_ID, mTraining);
            i.putExtra(SimpleFragmentActivity.EditRoundActivity.ROUND_ID, mRound);
            startActivity(i);
        }
    };*/

    @Override
    public void onSelected(Passe item) {
        Intent i = new Intent(activity, InputActivity.class);
        i.putExtra(InputActivity.ROUND_ID, item.roundId);
        i.putExtra(InputActivity.PASSE_IND, item.index);
        startActivity(i);
        activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onEdit(Passe item) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TRAINING_ID, mTraining);
    }
/*
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int translationShadow = Math.max(mActionBarSize, mActionBarSize + mHeaderHeight - scrollY);
        ViewHelper.setTranslationY(mShadow, translationShadow);
        ViewHelper.setTranslationY(mHeader, mActionBarSize - scrollY);
        ViewHelper.setAlpha(mDetails, ScrollUtils
                .getFloat((float) (mHeaderHeight - scrollY * 2) / mHeaderHeight, 0, 1));
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }*/

    @Override
    public void onClick(View v) {
        Intent i = new Intent(activity, InputActivity.class);
        i.putExtra(InputActivity.TRAINING_ID, mTraining);
        startActivity(i);
    }

    public class PasseAdapter extends ExpandableNowListAdapter<Round, Passe> {

        @Override
        protected HeaderViewHolder getTopLevelViewHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_round, parent, false);
            return new HeaderViewHolder(itemView);
        }

        @Override
        protected SelectableViewHolder<Passe> getSecondLevelViewHolder(ViewGroup parent) {
            if (mTargetViewMode) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.target_passe_card, parent, false);
                return new TargetViewHolder(itemView);
            } else {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.passe_card, parent, false);
                return new PasseViewHolder(itemView);
            }
        }

        @Override
        public int getMaxSpan() {
            return mTargetViewMode ? 2 : 1;
        }

        @Override
        public int getItemViewType(int position) {
            int type = super.getItemViewType(position);
            if (type == ITEM_TYPE && mTargetViewMode) {
                type = ITEM_TYPE_2;
            }
            return type;
        }
    }

    public class TargetViewHolder extends SelectableViewHolder<Passe> {
        public final TargetPasseView mShots;
        public final TextView mSubtitle;

        public TargetViewHolder(View itemView) {
            super(itemView, mMultiSelector, PasseFragment.this);
            mShots = (TargetPasseView) itemView.findViewById(R.id.shoots);
            mSubtitle = (TextView) itemView.findViewById(R.id.passe);
        }

        @Override
        public void bindCursor() {
            Context context = mSubtitle.getContext();
            Round r = db.getRound(mItem.roundId);
            mShots.setPasse(mItem, r.info.target);
            mSubtitle.setText(context.getString(R.string.passe_n, (mItem.index + 1)));
        }
    }

    public class PasseViewHolder extends SelectableViewHolder<Passe> {
        public final PasseView mShots;
        public final TextView mSubtitle;

        public PasseViewHolder(View itemView) {
            super(itemView, mMultiSelector, PasseFragment.this);
            mShots = (PasseView) itemView.findViewById(R.id.shoots);
            mSubtitle = (TextView) itemView.findViewById(R.id.passe);
        }

        @Override
        public void bindCursor() {
            Context context = mSubtitle.getContext();
            Round r = db.getRound(mItem.roundId);
            mShots.setPoints(mItem, r.info.target);
            mSubtitle.setText(context.getString(R.string.passe_n, (mItem.index + 1)));
        }
    }


    public class HeaderViewHolder extends SelectableViewHolder<Round> {
        public final TextView mTitle;
        public final TextView mSubtitle;
       // private final TextView mPoints;
       // private final TextView mPercentage;

        public HeaderViewHolder(View itemView) {
            super(itemView, R.id.expand_collapse);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
            mTitle = (TextView) itemView.findViewById(R.id.round);
            mSubtitle = (TextView) itemView.findViewById(R.id.dist);
           // mPoints = (TextView) itemView.findViewById(R.id.totalPoints);
          //  mPercentage = (TextView) itemView.findViewById(R.id.totalPercentage);
        }

        @Override
        public void bindCursor() {
            Context context = mTitle.getContext();
            mTitle.setText(context.getString(R.string.round) + " " + (mRounds.indexOf(mItem) + 1));
            //mPoints.setText(mItem.reachedPoints + "/" + mItem.maxPoints);
            //String percent = mItem.getMaxPoints() == 0 ? "" : (mItem.getReachedPoints() * 100 /
            //        mItem.getMaxPoints()) + "%";
            //mPercentage.setText(percent);

            String infoText = "";
            if (!distance_equals) {
                infoText += "<br>" + getString(R.string.distance) + ": <b>" +
                        mItem.info.distance/* + " - " +
                        getString(mItem.indoor ? R.string.indoor : R.string.outdoor) + "</b>"*/;
            }
            if (!target_equals) {
                infoText += "<br>" + getString(R.string.target_face) + ": <b>" +
                        mItem.info.target + "</b>";
            }
            if (!mItem.comment.isEmpty()) {
                infoText += "<br>" + getString(R.string.comment) +
                        ": <b>" + TextUtils.htmlEncode(mItem.comment) + "</b>";
            }

            if (infoText.startsWith("<br>")) {
                infoText = infoText.substring(4);
            }

            mSubtitle.setText(Html.fromHtml(infoText));
        }

        @Override
        public boolean onLongClick(View v) {
            //TODO show dialog (with round info and ) allow to add a comment to the round
            /*Intent i = new Intent(getActivity(), SimpleFragmentActivity.EditRoundActivity.class);
            i.putExtra(EditStandardRoundFragment.TRAINING_ID, mTraining);
            i.putExtra(EditStandardRoundFragment.ROUND_ID, mItem.id);
            startActivity(i);*/
            return true;
        }
    }
}
