package de.dreier.mytargets.activities;

import android.content.Intent;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.iangclifton.android.floatlabel.FloatLabel;

import java.util.ArrayList;

import de.dreier.mytargets.R;
import de.dreier.mytargets.managers.DatabaseManager;
import de.dreier.mytargets.models.Bow;

public class EditBowActivity extends EditWithImageActivity {

    public static final String BOW_ID = "bow_id";
    private FloatLabel name;
    private FloatLabel brand;
    private FloatLabel size;
    private FloatLabel height;
    private FloatLabel tiller;
    private FloatLabel desc;
    private RadioButton recurveBow, compoundBow, longBow, blank, horse, yumi;
    private long mBowId = -1;
    private LinearLayout sight_settings;
    private ArrayList<SightSetting> sightSettingsList;

    public EditBowActivity() {
        super(R.layout.activity_edit_bow, R.drawable.recurve_bow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(BOW_ID)) {
            mBowId = intent.getLongExtra(BOW_ID, -1);
        }

        name = (FloatLabel) findViewById(R.id.name);
        recurveBow = (RadioButton) findViewById(R.id.recurve);
        compoundBow = (RadioButton) findViewById(R.id.compound);
        longBow = (RadioButton) findViewById(R.id.longbow);
        blank = (RadioButton) findViewById(R.id.blank);
        horse = (RadioButton) findViewById(R.id.horse);
        yumi = (RadioButton) findViewById(R.id.yumi);
        brand = (FloatLabel) findViewById(R.id.brand);
        size = (FloatLabel) findViewById(R.id.size);
        height = (FloatLabel) findViewById(R.id.height);
        tiller = (FloatLabel) findViewById(R.id.tiller);
        desc = (FloatLabel) findViewById(R.id.desc);
        sight_settings = (LinearLayout) findViewById(R.id.sight_settings);
        Button add_button = (Button) findViewById(R.id.add_sight_setting_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSightSetting(new SightSetting(), -1);
            }
        });

        recurveBow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(true);
                compoundBow.setChecked(false);
                longBow.setChecked(false);
                blank.setChecked(false);
                horse.setChecked(false);
                yumi.setChecked(false);
            }
        });

        compoundBow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(false);
                compoundBow.setChecked(true);
                longBow.setChecked(false);
                blank.setChecked(false);
                horse.setChecked(false);
                yumi.setChecked(false);
            }
        });

        longBow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(false);
                compoundBow.setChecked(false);
                longBow.setChecked(true);
                blank.setChecked(false);
                horse.setChecked(false);
                yumi.setChecked(false);
            }
        });

        blank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(false);
                compoundBow.setChecked(false);
                longBow.setChecked(false);
                blank.setChecked(true);
                horse.setChecked(false);
                yumi.setChecked(false);
            }
        });

        horse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(false);
                compoundBow.setChecked(false);
                longBow.setChecked(false);
                blank.setChecked(false);
                horse.setChecked(true);
                yumi.setChecked(false);
            }
        });

        yumi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurveBow.setChecked(false);
                compoundBow.setChecked(false);
                longBow.setChecked(false);
                blank.setChecked(false);
                horse.setChecked(false);
                yumi.setChecked(true);
            }
        });

        if (savedInstanceState == null && mBowId != -1) {
            DatabaseManager db = DatabaseManager.getInstance(this);
            Bow bow = db.getBow(mBowId, false);
            name.setText(bow.name);
            brand.setText(bow.brand);
            size.setText(bow.size);
            height.setText(bow.height);
            tiller.setText(bow.tiller);
            desc.setText(bow.description);
            imageBitmap = bow.image;
            if (imageBitmap != null)
                mImageView.setImageBitmap(imageBitmap);
            mImageFile = bow.imageFile;
            switch (bow.type) {
                case 0:
                    recurveBow.setChecked(true);
                    break;
                case 1:
                    compoundBow.setChecked(true);
                    break;
                case 2:
                    longBow.setChecked(true);
                    break;
                case 3:
                    blank.setChecked(true);
                    break;
                case 4:
                    horse.setChecked(true);
                    break;
                case 5:
                    yumi.setChecked(true);
                    break;
            }
            sightSettingsList = db.getSettings(mBowId);
        } else if (savedInstanceState == null) {
            recurveBow.setChecked(true);
            sightSettingsList = new ArrayList<>();
        }

        if (savedInstanceState != null) {
            name.setText(savedInstanceState.getString("name"));
            brand.setText(savedInstanceState.getString("brand"));
            size.setText(savedInstanceState.getString("size"));
            height.setText(savedInstanceState.getString("height"));
            tiller.setText(savedInstanceState.getString("tiller"));
            desc.setText(savedInstanceState.getString("desc"));
            sightSettingsList = savedInstanceState.getParcelableArrayList("settings");
        } else {
            if (sightSettingsList.size() == 0 && mBowId == -1) {
                addSightSetting(new SightSetting(), -1);
            } else if (sightSettingsList.size() > 0) {
                for (int i = 0; i < sightSettingsList.size(); i++) {
                    addSightSetting(sightSettingsList.get(i), i);
                }
            }
        }
    }

    public static class SightSetting implements Parcelable {
        Spinner distance;
        View customDist;
        EditText distanceText;
        boolean custom;
        EditText setting;
        public int distanceVal;
        public String value = "/";
        public int distanceInd;

        public SightSetting() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(distanceVal);
            parcel.writeString(value);
        }

        public static final Parcelable.Creator<SightSetting> CREATOR
                = new Parcelable.Creator<SightSetting>() {
            public SightSetting createFromParcel(Parcel in) {
                return new SightSetting(in);
            }

            public SightSetting[] newArray(int size) {
                return new SightSetting[size];
            }
        };

        private SightSetting(Parcel in) {
            distanceVal = in.readInt();
            value = in.readString();
        }

        public void update() {
            if (custom) {
                distanceVal = Integer.parseInt(distanceText.getText().toString());
            } else {
                distanceVal = EditRoundActivity.distanceValues[distance.getSelectedItemPosition()];
            }
            value = setting.getText().toString();
        }
    }

    private void addSightSetting(final SightSetting setting, int i) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View rel = inflater.inflate(R.layout.sight_settings_item, sight_settings, false);
        setting.customDist = rel.findViewById(R.id.customDist);
        setting.distanceText = (EditText) rel.findViewById(R.id.distanceVal);
        setting.distance = (Spinner) rel.findViewById(R.id.distance);
        setting.distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == EditRoundActivity.distanceValues.length) {
                    setting.distance.setVisibility(View.GONE);
                    setting.customDist.setVisibility(View.VISIBLE);
                    setting.custom = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setting.setting = (EditText) rel.findViewById(R.id.sight_setting);
        ImageButton remove = (ImageButton) rel.findViewById(R.id.remove_sight_setting);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sight_settings.removeView(rel);
                sightSettingsList.remove(setting);
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.distances));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setting.distance.setAdapter(adapter);
        if (setting.distanceInd == -1) {
            setting.distance.setVisibility(View.GONE);
            setting.customDist.setVisibility(View.VISIBLE);
            setting.distanceText.setText("" + setting.distanceVal);
            setting.custom = true;
        } else {
            setting.distance.setSelection(setting.distanceInd);
            setting.distance.setVisibility(View.VISIBLE);
            setting.customDist.setVisibility(View.GONE);
            setting.custom = false;
        }
        setting.setting.setText(setting.value);
        if (i == -1) {
            i = sightSettingsList.size();
            sightSettingsList.add(setting);
        }
        setting.distance.setId(548969458 + i * 2);
        setting.setting.setId(548969458 + i * 2 + 1);
        sight_settings.addView(rel);
    }

    @Override
    public void onSave() {
        DatabaseManager db = DatabaseManager.getInstance(this);

        Bow bow = new Bow();
        bow.id = mBowId;
        bow.name = name.getTextString();
        bow.brand = brand.getTextString();
        bow.size = size.getTextString();
        bow.height = height.getTextString();
        bow.tiller = tiller.getTextString();
        bow.description = desc.getTextString();

        if (recurveBow.isChecked())
            bow.type = 0;
        else if (compoundBow.isChecked())
            bow.type = 1;
        else if (longBow.isChecked())
            bow.type = 2;
        else if (blank.isChecked())
            bow.type = 3;
        else if (horse.isChecked())
            bow.type = 4;
        else if (yumi.isChecked())
            bow.type = 5;
        else
            bow.type = 0;

        bow.image = ThumbnailUtils.extractThumbnail(imageBitmap, 100, 100);
        bow.imageFile = mImageFile;

        mBowId = db.updateBow(bow);

        for (SightSetting set : sightSettingsList)
            set.update();

        db.updateSightSettings(mBowId, sightSettingsList);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name.getTextString());
        outState.putString("brand", brand.getTextString());
        outState.putString("size", size.getTextString());
        outState.putString("height", height.getTextString());
        outState.putString("tiller", tiller.getTextString());
        outState.putString("desc", desc.getTextString());
        for (SightSetting set : sightSettingsList)
            set.update();
        outState.putParcelableArrayList("settings", sightSettingsList);
    }
}