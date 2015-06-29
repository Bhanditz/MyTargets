/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.dreier.mytargets.R;
import de.dreier.mytargets.adapters.MainTabsFragmentPagerAdapter;
import de.dreier.mytargets.fragments.NowListFragment;
import de.dreier.mytargets.fragments.NowListFragmentBase;
import de.dreier.mytargets.shared.models.Arrow;
import de.dreier.mytargets.shared.models.IdProvider;

/**
 * Shows an overview over all trying days
 */
public class MainActivity extends AppCompatActivity
        implements NowListFragment.OnItemSelectedListener, NowListFragmentBase.ContentListener,
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private static boolean shownThisTime = false;
    protected FloatingActionButton mFab;
    protected View mNewLayout;
    protected TextView mNewText;
    private ViewPager viewPager;
    private MainTabsFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new MainTabsFragmentPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setTabTextColors(0xCCFFFFFF, Color.WHITE);
        tabLayout.setupWithViewPager(viewPager);

        askForHelpTranslating();
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);
        mNewLayout = findViewById(R.id.new_layout);
        mNewText = (TextView) findViewById(R.id.new_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_preferences) {
            startActivity(new Intent(this, SimpleFragmentActivity.SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void askForHelpTranslating() {
        ArrayList<String> supportedLanguages = new ArrayList<>();
        supportedLanguages.add("de");
        supportedLanguages.add("en");
        supportedLanguages.add("fr");
        supportedLanguages.add("es");
        supportedLanguages.add("ru");
        supportedLanguages.add("nl");
        supportedLanguages.add("it");
        supportedLanguages.add("sl");
        supportedLanguages.add("ca");
        supportedLanguages.add("zh");
        supportedLanguages.add("tr");
        supportedLanguages.add("hu");

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        boolean shown = prefs.getBoolean("translation_dialog_shown", false);

        String longLang = Locale.getDefault().getDisplayLanguage();
        String shortLocale = Locale.getDefault().getLanguage();
        if (!supportedLanguages.contains(shortLocale) && !shown && !shownThisTime) {
            // Link the e-mail address in the message
            final SpannableString s = new SpannableString(Html.fromHtml("If you would like " +
                    "to help make MyTargets even better by translating the app to " +
                    longLang +
                    ", please send me an E-Mail (dreier.florian@gmail.com) " +
                    "so I can give you access to the translation file!<br /><br />" +
                    "Thanks in advance :)"));
            Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);
            AlertDialog d = new AlertDialog.Builder(this).setTitle("App translation")
                    .setMessage(s)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putBoolean("translation_dialog_shown", true).apply();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Remind me later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shownThisTime = true;
                            dialog.dismiss();
                        }
                    }).create();
            d.show();
            ((TextView) d.findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        if (viewPager.getCurrentItem() == 0) {
            i.setClass(this, SimpleFragmentActivity.EditTrainingActivity.class);
        } else if (viewPager.getCurrentItem() == 1) {
            i.setClass(this, EditBowActivity.class);
        } else if (viewPager.getCurrentItem() == 2) {
            i.setClass(this, EditArrowActivity.class);
        }
        startActivity(i);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onItemSelected(long itemId, Class<? extends IdProvider> aClass) {
        Intent i;
        if (aClass.equals(Arrow.class)) {
            i = new Intent(this, EditArrowActivity.class);
            i.putExtra(EditArrowActivity.ARROW_ID, itemId);
        } else {
            i = new Intent(this, EditBowActivity.class);
            i.putExtra(EditBowActivity.BOW_ID, itemId);
        }
        startActivity(i);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onItemSelected(IdProvider e) {

    }

    boolean empty[] = new boolean[3];
    int stringRes[] = new int[3];

    {
        stringRes[0] = R.string.new_training;
        stringRes[1] = R.string.new_bow;
        stringRes[2] = R.string.new_arrow;
    }

    @Override
    public void onContentChanged(boolean empty, int stringRes) {
        for (int i = 0; i < this.stringRes.length; i++) {
            if (stringRes == this.stringRes[i]) {
                this.empty[i] = empty;
            }
        }
        onPageSelected(viewPager.getCurrentItem());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mNewLayout.setVisibility(empty[position] ? View.VISIBLE : View.GONE);
        mNewText.setText(stringRes[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
