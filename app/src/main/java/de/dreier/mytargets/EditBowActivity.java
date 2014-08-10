package de.dreier.mytargets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.iangclifton.android.floatlabel.FloatLabel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditBowActivity extends Activity implements View.OnClickListener {

    public static final String BOW_ID = "bow_id";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PICTURE = 2;
    private ImageView mImageView;
    private FloatLabel name, brand, size, height, tiller, desc;
    private RadioButton recurvebow, compoundbow, longbow, blank;
    private long mBowId = -1, mImageId = -1;
    private Uri fileUri;
    private Bitmap imageBitmap = null;
    private Drawable mActionBarBackgroundDrawable;
    private LinearLayout sight_settings;
    private ArrayList<SightSetting> sightSettingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bow);

        Intent intent = getIntent();
        if(intent!=null && intent.hasExtra(BOW_ID)) {
            mBowId = intent.getLongExtra(BOW_ID, -1);
        }

        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_solid_example);
        mActionBarBackgroundDrawable.setAlpha(0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mActionBarBackgroundDrawable.setCallback(mDrawableCallback);
        }
        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);

        ((NotifyingScrollView) findViewById(R.id.scrollView)).setOnScrollChangedListener(mOnScrollChangedListener);

        mImageView = (ImageView)findViewById(R.id.imageView);
        registerForContextMenu(mImageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageView.showContextMenu();
            }
        });
        name = (FloatLabel)findViewById(R.id.bow_name);
        recurvebow = (RadioButton)findViewById(R.id.recurve);
        compoundbow = (RadioButton)findViewById(R.id.compound);
        longbow = (RadioButton)findViewById(R.id.longbow);
        blank = (RadioButton)findViewById(R.id.blank);
        brand = (FloatLabel) findViewById(R.id.marke);
        size = (FloatLabel) findViewById(R.id.size);
        height = (FloatLabel) findViewById(R.id.height);
        tiller = (FloatLabel) findViewById(R.id.tiller);
        desc = (FloatLabel) findViewById(R.id.desc);
        sight_settings = (LinearLayout)findViewById(R.id.sight_settings);
        Button add_button = (Button) findViewById(R.id.add_visier_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSightSetting(new SightSetting(),-1);
            }
        });
        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Button newBow = (Button) findViewById(R.id.new_bow_button);
        newBow.setOnClickListener(this);

        recurvebow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurvebow.setChecked(true);
                compoundbow.setChecked(false);
                longbow.setChecked(false);
                blank.setChecked(false);
            }
        });

        compoundbow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurvebow.setChecked(false);
                compoundbow.setChecked(true);
                longbow.setChecked(false);
                blank.setChecked(false);
            }
        });

        longbow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurvebow.setChecked(false);
                compoundbow.setChecked(false);
                longbow.setChecked(true);
                blank.setChecked(false);
            }
        });

        blank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recurvebow.setChecked(false);
                compoundbow.setChecked(false);
                longbow.setChecked(false);
                blank.setChecked(true);
            }
        });

        if(savedInstanceState==null && mBowId!=-1) {
            TargetOpenHelper db = new TargetOpenHelper(this);
            TargetOpenHelper.Bow bow = db.getBow(mBowId,false);
            name.setText(bow.name);
            brand.setText(bow.brand);
            size.setText(bow.size);
            height.setText(bow.height);
            tiller.setText(bow.tiller);
            desc.setText(bow.description);
            imageBitmap = bow.image;
            mImageView.setImageBitmap(imageBitmap);
            mImageId = bow.imageId;
            switch (bow.type) {
                case 0:
                    recurvebow.setChecked(true); break;
                case 1:
                    compoundbow.setChecked(true); break;
                case 2:
                    longbow.setChecked(true); break;
                case 3:
                    blank.setChecked(true); break;
            }
            sightSettingsList = db.getSettings(mBowId);
        } else if(savedInstanceState==null) {
            recurvebow.setChecked(true);
            sightSettingsList = new ArrayList<SightSetting>();
        }

        if(savedInstanceState!=null) {
            name.setText(savedInstanceState.getString("name"));
            brand.setText(savedInstanceState.getString("brand"));
            size.setText(savedInstanceState.getString("size"));
            height.setText(savedInstanceState.getString("height"));
            tiller.setText(savedInstanceState.getString("tiller"));
            desc.setText(savedInstanceState.getString("desc"));
            imageBitmap = savedInstanceState.getParcelable("img");
            mImageId = savedInstanceState.getLong("image_id");
            mImageView.setImageBitmap(imageBitmap);
            sightSettingsList = savedInstanceState.getParcelableArrayList("settings");
        } else if(imageBitmap==null) {
            ViewTreeObserver vtobs = mImageView.getViewTreeObserver();
            vtobs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(imageBitmap==null) {
                        int width = mImageView.getMeasuredWidth();
                        int height = mImageView.getMeasuredHeight();
                        try {
                            imageBitmap = decodeSampledBitmapFromRes(R.drawable.recurvebogen, width, height);
                        } catch (IOException e) {
                            e.printStackTrace();
                            imageBitmap = null;
                        }
                    }
                }
            });

            if(sightSettingsList.size()==0) {
                addSightSetting(new SightSetting(), -1);
            } else {
                for (int i = 0; i < sightSettingsList.size(); i++) {
                    addSightSetting(sightSettingsList.get(i), i);
                }
            }
        }
    }

    public static class SightSetting implements Parcelable {
        Spinner distance;
        EditText setting;
        int distanceInd = 0;
        String value="/";

        public SightSetting() {}

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(distanceInd);
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
            distanceInd = in.readInt();
            value = in.readString();
        }

        public void update() {
            distanceInd = distance.getSelectedItemPosition();
            value = setting.getText().toString();
        }
    }

    private void addSightSetting(SightSetting setting, int i) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View rel = inflater.inflate(R.layout.sight_settings_item, sight_settings, false);
        setting.distance = (Spinner) rel.findViewById(R.id.distanceSpinner);
        setting.setting = (EditText) rel.findViewById(R.id.sight_setting);
        ImageButton remove = (ImageButton) rel.findViewById(R.id.remove_sight_setting);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sight_settings.removeView(rel);
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, NewRoundActivity.distances);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setting.distance.setAdapter(adapter);
        setting.distance.setSelection(setting.distanceInd);
        setting.setting.setText(setting.value);
        if(i==-1) {
            i = sightSettingsList.size();
            sightSettingsList.add(setting);
        }
        setting.distance.setId(548969458+i*2);
        setting.setting.setId(548969458+i*2+1);
        sight_settings.addView(rel);
    }

    @Override
    public void onClick(View view) {
        TargetOpenHelper db = new TargetOpenHelper(this);

        int bowType=0;
        if(recurvebow.isChecked())
            bowType = 0;
        else if(compoundbow.isChecked())
            bowType = 1;
        else if(longbow.isChecked())
            bowType = 2;
        else if(blank.isChecked())
            bowType = 3;

        Bitmap thumb = ThumbnailUtils.extractThumbnail(imageBitmap, 100, 100);

        mBowId = db.updateBow(mBowId, mImageId, name.getTextString(), bowType,
                brand.getTextString(), size.getTextString(),
                height.getTextString(), tiller.getTextString(),
                desc.getTextString(), thumb, imageBitmap);

        for(SightSetting set:sightSettingsList)
            set.update();

        db.updateSightSettings(mBowId, sightSettingsList);
        finish();
    }

    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }
    };

    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            final int headerHeight = findViewById(R.id.imageView).getHeight() - getActionBar().getHeight();
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            mActionBarBackgroundDrawable.setAlpha(newAlpha);
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_image, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_from_gallery:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.select_picture)), SELECT_PICTURE);
                return true;
            case R.id.action_take_picture:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        fileUri = getOutputMediaFileUri();
                    } catch(NullPointerException e) {
                        return true;
                    }
                    takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, fileUri );
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = fileUri;
        } else if(requestCode==SELECT_PICTURE  && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
        } else {
            return;
        }

        try {
            imageBitmap = decodeSampledBitmapFromStream(selectedImageUri,mImageView.getWidth(),mImageView.getHeight());
            mImageView.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("OutputMediaFile","SD card is not mounted!");
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyTargets");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyTargets", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    public Bitmap decodeSampledBitmapFromStream(Uri uri, int reqWidth, int reqHeight) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream stream = getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        stream = getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        return bmp;
    }

    public Bitmap decodeSampledBitmapFromRes(int id, int reqWidth, int reqHeight) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), id, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(getResources(), id, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        int minSize = Math.max(reqWidth,reqHeight);

        if (height > minSize || width > minSize) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > minSize
                    && (halfWidth / inSampleSize) > minSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
        outState.putParcelable("img",imageBitmap);
        outState.putLong("image_id",mImageId);
        for(SightSetting set:sightSettingsList)
            set.update();
        outState.putParcelableArrayList("settings",sightSettingsList);
    }
}