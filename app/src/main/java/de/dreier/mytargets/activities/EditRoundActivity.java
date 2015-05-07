/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.iangclifton.android.floatlabel.FloatLabel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import de.dreier.mytargets.R;
import de.dreier.mytargets.adapters.ArrowItemAdapter;
import de.dreier.mytargets.adapters.BowItemAdapter;
import de.dreier.mytargets.adapters.TargetItemAdapter;
import de.dreier.mytargets.fragments.DatePickerFragment;
import de.dreier.mytargets.fragments.DistanceFragment;
import de.dreier.mytargets.fragments.PasseFragment;
import de.dreier.mytargets.managers.DatabaseManager;
import de.dreier.mytargets.models.Round;
import de.dreier.mytargets.models.Training;
import de.dreier.mytargets.utils.MyBackupAgent;
import de.dreier.mytargets.views.DialogSpinner;
import de.dreier.mytargets.views.DistanceDialogSpinner;
import de.dreier.mytargets.views.NumberPicker;

public class EditRoundActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final String TRAINING_ID = "training_id";
    public static final String ROUND_ID = "round_id";
    public static final String EDIT_TRAINING = "edit_training";
    private static final int REQ_SELECTED_ARROW = 1;
    private static final int REQ_SELECTED_BOW = 2;
    private static final int REQ_SELECTED_TARGET = 3;
    private static final int REQ_SELECTED_DISTANCE = 4;

    private long mTraining = -1, mRound = -1;

    private DistanceDialogSpinner distance;
    private RadioButton indoor;
    private DialogSpinner bow;
    private DialogSpinner arrow;
    private DialogSpinner target;
    private int mBowId = 0;
    private EditText training;
    private FloatLabel comment;
    private NumberPicker rounds, arrows;
    private Button training_date;
    private Date date = new Date();
    private boolean editTraining;
    private View scrollView;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_round);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        Intent i = getIntent();
        if (i != null) {
            if (i.hasExtra(TRAINING_ID)) {
                mTraining = i.getLongExtra(TRAINING_ID, -1);
            }
            if (i.hasExtra(ROUND_ID)) {
                mRound = i.getLongExtra(ROUND_ID, -1);
            }
            if (i.hasExtra(EDIT_TRAINING)) {
                editTraining = i.getBooleanExtra(EDIT_TRAINING, false);
            }
        }
        SharedPreferences prefs = getSharedPreferences(MyBackupAgent.PREFS, 0);

        training = (EditText) findViewById(R.id.training);
        training_date = (Button) findViewById(R.id.training_date);
        training_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Package bundle with fragment arguments
                Bundle bundle = new Bundle();
                bundle.putSerializable(DatePickerFragment.ARG_CURRENT_DATE, date);

                // Create and show date picker
                DatePickerFragment datePickerDialog = new DatePickerFragment();
                datePickerDialog.setArguments(bundle);
                datePickerDialog.show(getSupportFragmentManager(), "datepicker");
            }
        });

        scrollView = findViewById(R.id.scrollView);

        // Distance
        distance = (DistanceDialogSpinner) findViewById(R.id.distance_spinner);
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditRoundActivity.this,
                        SimpleFragmentActivity.DistanceItemSelectActivity.class);
                i.putExtra("title", R.string.distance);
                i.putExtra(DistanceFragment.CUR_DISTANCE, (int) distance.getSelectedItemId());
                startActivityForResult(i, REQ_SELECTED_DISTANCE);
            }
        });

        // Indoor / outdoor
        RadioButton outdoor = (RadioButton) findViewById(R.id.outdoor);
        indoor = (RadioButton) findViewById(R.id.indoor);

        // Show scoreboard
        rounds = (NumberPicker) findViewById(R.id.rounds);
        rounds.setTextPattern(R.plurals.passe);

        // Points per passe
        arrows = (NumberPicker) findViewById(R.id.ppp);
        arrows.setTextPattern(R.plurals.arrow);
        arrows.setMinimum(1);
        arrows.setMaximum(10);

        // Bow
        bow = (DialogSpinner) findViewById(R.id.bow);
        bow.setAdapter(new BowItemAdapter(this));
        Button addBow = (Button) findViewById(R.id.add_bow);
        bow.setAddButton(addBow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditRoundActivity.this, EditBowActivity.class));
            }
        });
        bow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditRoundActivity.this,
                        SimpleFragmentActivity.BowItemSelectActivity.class);
                i.putExtra("title", R.string.bow);
                startActivityForResult(i, REQ_SELECTED_BOW);

            }
        });

        // Arrow
        arrow = (DialogSpinner) findViewById(R.id.arrow);
        arrow.setAdapter(new ArrowItemAdapter(this));
        Button addArrow = (Button) findViewById(R.id.add_arrow);
        arrow.setAddButton(addArrow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditRoundActivity.this, EditArrowActivity.class));
            }
        });
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditRoundActivity.this,
                        SimpleFragmentActivity.ArrowItemSelectActivity.class);
                i.putExtra("title", R.string.bow);
                startActivityForResult(i, REQ_SELECTED_ARROW);
            }
        });

        // Target round
        target = (DialogSpinner) findViewById(R.id.target_spinner);
        target.setAdapter(new TargetItemAdapter(this));
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditRoundActivity.this,
                        SimpleFragmentActivity.TargetItemSelectActivity.class);
                i.putExtra("title", R.string.target_round);
                startActivityForResult(i, REQ_SELECTED_TARGET);
            }
        });

        // Comment
        comment = (FloatLabel) findViewById(R.id.comment);

        if (mRound == -1) {
            // Initialise with default values
            distance.setItemId(prefs.getInt("distance", 10));
            indoor.setChecked(prefs.getBoolean("indoor", false));
            outdoor.setChecked(!prefs.getBoolean("indoor", false));
            arrows.setValue(prefs.getInt("ppp", 3));
            rounds.setValue(prefs.getInt("rounds", 10));
            bow.setItemId(prefs.getInt("bow", 0));
            arrow.setItemId(prefs.getInt("arrow", 0));
            target.setItemId(prefs.getInt("target", 2));
            comment.setText("");
        } else {
            // Load saved values
            DatabaseManager db = DatabaseManager.getInstance(this);
            Round r = db.getRound(mRound);
            distance.setItemId(r.distanceVal);
            indoor.setChecked(r.indoor);
            outdoor.setChecked(!r.indoor);
            arrows.setValue(r.ppp);
            bow.setItemId(r.bow);
            arrow.setItemId(r.arrow);
            target.setItemId(r.target);
            comment.setText(r.comment);

            View not_editable = findViewById(R.id.not_editable);
            not_editable.setVisibility(View.GONE);
        }

        if (mTraining == -1) {
            training.setText(getString(R.string.training));
            setTrainingDate();
            getSupportActionBar().setTitle(R.string.new_training);
        } else if(editTraining) {
            DatabaseManager db = DatabaseManager.getInstance(this);
            Training train = db.getTraining(mTraining);
            training.setText(train.title);
            date = train.date;
            setTrainingDate();
            getSupportActionBar().setTitle(R.string.new_training);
            scrollView.setVisibility(View.GONE);
        } else {
            View training_container = findViewById(R.id.training_container);
            training_container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        date = new Date(new GregorianCalendar(year, monthOfYear, dayOfMonth).getTimeInMillis());
        setTrainingDate();
    }

    private void setTrainingDate() {
        training_date.setText(SimpleDateFormat.getDateInstance().format(date));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            long id = data.getLongExtra("id", 0);
            if (requestCode == REQ_SELECTED_ARROW) {
                arrow.setItemId(id);
                return;
            } else if (requestCode == REQ_SELECTED_BOW) {
                bow.setItemId(id);
                return;
            } else if (requestCode == REQ_SELECTED_TARGET) {
                target.setItemId(id);
                return;
            } else if (requestCode == REQ_SELECTED_DISTANCE) {
                distance.setItemId(id);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bow.setAdapter(new BowItemAdapter(this));
        arrow.setAdapter(new ArrowItemAdapter(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            finish();
            if(!editTraining) {
                if(mTraining==-1){
                    onSaveTraining();
                }
                onSaveRound();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else {
                onSaveTraining();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onSaveTraining() {
        DatabaseManager db = DatabaseManager.getInstance(this);
        String title = training.getText().toString();
        Training training = new Training();
        training.id = mTraining;
        training.title = title;
        training.date = date;
        db.updateTraining(training);
        mTraining = training.id;
    }

    void onSaveRound() {
        Round round = new Round();
        round.target = (int) target.getSelectedItemId();

        if (bow.getAdapter().getCount() == 0 && mBowId == 0 && round.target == 3) {
            new AlertDialog.Builder(this).setTitle(R.string.title_compound)
                    .setMessage(R.string.msg_compound_type)
                    .setPositiveButton(R.string.compound_bow,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBowId = -2;
                                    onSaveRound();
                                }
                            })
                    .setNegativeButton(R.string.other_bow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBowId = -1;
                            onSaveRound();
                        }
                    }).setCancelable(false)
                    .show();
            return;
        }

        DatabaseManager db = DatabaseManager.getInstance(this);

        round.id = mRound;
        round.training = mTraining;
        round.bow = bow.getSelectedItemId();
        round.arrow = arrow.getSelectedItemId();
        if (round.bow == 0) {
            round.bow = mBowId;
        }

        round.distanceVal = (int) distance.getSelectedItemId();
        round.unit = "m";

        int after_rounds = rounds.getValue();
        round.ppp = arrows.getValue();
        round.indoor = indoor.isChecked();
        round.comment = comment.getTextString();
        db.updateRound(round);

        SharedPreferences prefs = getSharedPreferences(MyBackupAgent.PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("bow", (int) bow.getSelectedItemId());
        editor.putInt("arrow", (int) arrow.getSelectedItemId());
        editor.putInt("distance", round.distanceVal);
        editor.putInt("ppp", round.ppp);
        editor.putInt("rounds", after_rounds);
        editor.putInt("target", round.target);
        editor.putBoolean("indoor", round.indoor);
        editor.apply();

        if (mRound == -1) {
            Intent i = new Intent(this, SimpleFragmentActivity.TrainingActivity.class);
            i.putExtra(PasseFragment.TRAINING_ID, mTraining);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);

            i = new Intent(this, InputActivity.class);
            i.putExtra(InputActivity.ROUND_ID, round.id);
            i.putExtra(InputActivity.STOP_AFTER, after_rounds);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
