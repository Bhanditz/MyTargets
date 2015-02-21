package de.dreier.mytargets.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.dreier.mytargets.R;
import de.dreier.mytargets.adapters.PasseAdapter;
import de.dreier.mytargets.adapters.TargetItemAdapter;
import de.dreier.mytargets.fragments.ShareDialogFragment;
import de.dreier.mytargets.models.Arrow;
import de.dreier.mytargets.models.Bow;
import de.dreier.mytargets.models.Round;
import de.dreier.mytargets.models.Target;
import de.dreier.mytargets.utils.ScoreboardImage;
import de.dreier.mytargets.utils.TargetImage;

/**
 * Shows all passes of one round
 */
public class RoundActivity extends NowListActivity implements ShareDialogFragment.ShareDialogListener {

    private long mTraining;
    private long mRound;

    private Round mRoundInfo;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_round;
    }

    @Override
    protected void init(Intent intent, Bundle savedInstanceState) {
        itemSingular = getString(R.string.passe_singular);
        itemPlural = getString(R.string.passe_plural);
        if (intent != null && intent.hasExtra(ROUND_ID)) {
            mTraining = intent.getLongExtra(TRAINING_ID, -1);
            mRound = intent.getLongExtra(ROUND_ID, -1);
        }
        if (savedInstanceState != null) {
            mTraining = savedInstanceState.getLong(TRAINING_ID, -1);
            mRound = savedInstanceState.getLong(ROUND_ID, -1);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.round) + " " + db.getRoundInd(mTraining, mRound));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRoundInfo = db.getRound(mRound);
        setRoundInfo();
        adapter = new PasseAdapter(this, mRound, mRoundInfo);
        setListAdapter(adapter);
        supportInvalidateOptionsMenu();
    }

    public void setRoundInfo() {
        int maxPoints = Target.getMaxPoints(mRoundInfo.target);
        int reached = db.getRoundPoints(mRound);
        int maxP = mRoundInfo.ppp * maxPoints * db.getPasses(mRound).getCount();

        TextView info = (TextView) findViewById(R.id.detail_round_info);
        TextView score = (TextView) findViewById(R.id.detail_score);

        // Set round info
        String percent = maxP == 0 ? "" : " (" + (reached * 100 / maxP) + "%)";
        String infoText = "<font color=#ffffff>"+getString(R.string.distance) + "</font>: <font color=#ff9100><b>" +
                mRoundInfo.distance + " - " +
                getString(mRoundInfo.indoor ? R.string.indoor : R.string.outdoor) + "</b></font><br>" +
                "<font color=#ffffff>"+getString(R.string.points) + "</font>: <font color=#ff9100><b>" + reached + "/" + maxP + percent + "</b></font><br>" +
                "<font color=#ffffff>"+getString(R.string.target_round) + "</font>: <font color=#ff9100><b>" + TargetItemAdapter.targets[mRoundInfo.target] + "</b></font>";
        Bow bow = db.getBow(mRoundInfo.bow, true);
        if (bow != null) {
            infoText += "<br><font color=#ffffff>"+getString(R.string.bow) +
                    "</font>: <font color=#ff9100><b>" + TextUtils.htmlEncode(bow.name) + "</b></font>";
        }
        Arrow arrow = db.getArrow(mRoundInfo.arrow, true);
        if (arrow != null) {
            infoText += "<br><font color=#ffffff>"+getString(R.string.arrow) +
                    "</font>: <font color=#ff9100><b>" + TextUtils.htmlEncode(arrow.name) + "</b></font>";
        }
        if (!mRoundInfo.comment.isEmpty()) {
            infoText += "<br><font color=#ffffff>"+getString(R.string.comment) +
                    "</font>: <font color=#ff9100><b>" + TextUtils.htmlEncode(mRoundInfo.comment) + "</b></font>";
        }
        info.setText(Html.fromHtml(infoText));

        // Set number of X, 10, 9 shoots
        infoText = "<font color=#ffffff>X</font>: <font color=#ff9100><b>" + mRoundInfo.scoreCount[0] + "</b></font><br>" +
                "<font color=#ffffff>"+getString(R.string.ten_x) + "</font>: <font color=#ff9100><b>" + (mRoundInfo.scoreCount[0] + mRoundInfo.scoreCount[1]) + "</b></font><br>" +
                "<font color=#ffffff>"+getString(R.string.nine) + "</font>: <font color=#ff9100><b>" + mRoundInfo.scoreCount[2] + "</b></font>";
        score.setText(Html.fromHtml(infoText));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.round, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean hasPasses = adapter.getCount() > 2;
        menu.findItem(R.id.action_scoreboard).setVisible(hasPasses);
        menu.findItem(R.id.action_share).setVisible(hasPasses);
        menu.findItem(R.id.action_statistics).setVisible(hasPasses);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scoreboard:
                Intent intent = new Intent(this, ScoreboardActivity.class);
                intent.putExtra(ScoreboardActivity.ROUND_ID, mRound);
                startActivity(intent);
                return true;
            case R.id.action_statistics:
                Intent i = new Intent(this, StatisticsActivity.class);
                i.putExtra(StatisticsActivity.ROUND_ID, mRound);
                startActivity(i);
                return true;
            case R.id.action_share:
                showShareDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void showShareDialog() {
        // Create an instance of the dialog fragment and show it
        ShareDialogFragment dialog = new ShareDialogFragment();
        dialog.show(getSupportFragmentManager(), "share_dialog");
    }

    /* Called after the user selected with items he wants to share */
    @Override
    public void onShareDialogConfirmed(final boolean include_text, final boolean dispersion_pattern, final boolean scoreboard, final boolean comments) {
        // Construct share intent
        mRoundInfo = db.getRound(mRound);
        int max = Target.getMaxPoints(mRoundInfo.target);
        int reached = db.getRoundPoints(mRound);
        int maxP = mRoundInfo.ppp * max * db.getPasses(mRound).getCount();
        final String text = getString(R.string.my_share_text,
                mRoundInfo.scoreCount[0], mRoundInfo.scoreCount[1], mRoundInfo.scoreCount[2], reached, maxP);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final File f = File.createTempFile("target", ".png", getExternalCacheDir());
                    if (dispersion_pattern && !scoreboard && !comments) {
                        new TargetImage().generateBitmap(RoundActivity.this, 800, mRoundInfo, mRound, f);
                    } else {
                        new ScoreboardImage().generateBitmap(RoundActivity.this, mRound, scoreboard, dispersion_pattern, comments, f);
                    }

                    // Build and fire intent to ask for share provider
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    if (include_text)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    if (dispersion_pattern || scoreboard || comments)
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                    shareIntent.setType("*/*");
                    startActivity(shareIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(RoundActivity.this, getString(R.string.sharing_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    @Override
    protected void onDelete(long[] ids) {
        db.deletePasses(ids);
    }

    @Override
    public boolean onItemClick(Intent i, int pos, long id) {
        if (pos == 0) {
            i.setClass(this, PasseActivity.class);
            i.putExtra(PasseActivity.ROUND_ID, mRound);
        } else {
            i.setClass(this, PasseActivity.class);
            i.putExtra(PasseActivity.ROUND_ID, mRound);
            i.putExtra(PasseActivity.PASSE_IND, pos);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TRAINING_ID, mTraining);
        outState.putLong(ROUND_ID, mRound);
    }
}
