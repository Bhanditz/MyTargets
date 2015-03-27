/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.SimpleFragmentActivity;

/**
 * Shows all passes of one round
 */
public class TimerFragment extends Fragment implements View.OnClickListener {
    private static final int WAIT_FOR_START = 0;
    private static final int PREPARATION = 1;
    private static final int SHOOTING = 2;
    private static final int FINISHED = 3;


    private int mWaitingTime;
    private int mShootingTime;
    private int mWarnTime;
    private View root;
    private TextView mStatusField, mTimeField;
    private int mCurStatus = WAIT_FOR_START;
    private CountDownTimer countdown;
    private MediaPlayer horn;
    private boolean mWithSound;
    private PowerManager.WakeLock wakeLock;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_timer, container, false);
        root.setOnClickListener(this);
        mStatusField = (TextView) root.findViewById(R.id.timer_status);
        mTimeField = (TextView) root.findViewById(R.id.timer_time);
        horn = MediaPlayer.create(getActivity(), R.raw.horn);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mWaitingTime = Integer.parseInt(prefs.getString("timer_wait_time", "20"));
        mShootingTime = Integer.parseInt(prefs.getString("timer_shoot_time", "120"));
        mWarnTime = Integer.parseInt(prefs.getString("timer_warn_time", "30"));
        mWithSound = prefs.getBoolean("timer_with_sound", true);
        mStatusField.setText(R.string.touch_to_start);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");
        wakeLock.acquire();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    public void onClick(View v) {
        changeStatus(mCurStatus + 1);
    }

    private void changeStatus(int status) {
        if (countdown != null) {
            countdown.cancel();
        }
        mCurStatus = status;
        switch (status) {
            case PREPARATION:
                playHorn(2);
                mStatusField.setText(R.string.preparation);
                countdown = new CountDownTimer(mWaitingTime * 1000, 100) {
                    public void onTick(long millisUntilFinished) {
                        mTimeField.setText("" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        changeStatus(SHOOTING);
                    }
                }.start();
                break;
            case SHOOTING:
                playHorn(1);
                root.setBackgroundResource(R.color.timer_green);
                mStatusField.setText(R.string.shooting);
                countdown = new CountDownTimer(mShootingTime * 1000, 100) {
                    public void onTick(long millisUntilFinished) {
                        mTimeField.setText("" + millisUntilFinished / 1000);
                        if (millisUntilFinished <= mWarnTime * 1000) {
                            root.setBackgroundResource(R.color.timer_orange);
                        }
                    }

                    public void onFinish() {
                        changeStatus(FINISHED);
                    }
                }.start();
                break;
            case FINISHED:
                playHorn(3);
                root.setBackgroundResource(R.color.timer_red);
                mTimeField.setText(R.string.stop);
                countdown = new CountDownTimer(6000, 100) {
                    public void onTick(long millisUntilFinished) {
                        mStatusField.setText("" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        getActivity().finish();
                        getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    }
                }.start();
        }
    }

    private void playHorn(final int n) {
        if (!mWithSound) {
            return;
        }
        horn.start();
        horn.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (n > 1) {
                    playHorn(n - 1);
                }
            }

        });
    }

    @Override
    public void onStop() {
        super.onStop();
        horn.release();
        horn = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_preferences) {
            startActivity(
                    new Intent(getActivity(), SimpleFragmentActivity.SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
