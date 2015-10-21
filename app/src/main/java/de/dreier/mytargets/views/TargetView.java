/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.views;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.dreier.mytargets.R;
import de.dreier.mytargets.shared.models.ArrowNumber;
import de.dreier.mytargets.shared.models.Coordinate;
import de.dreier.mytargets.shared.models.Passe;
import de.dreier.mytargets.shared.models.RoundTemplate;
import de.dreier.mytargets.shared.models.Shot;
import de.dreier.mytargets.shared.models.target.SpotBase;
import de.dreier.mytargets.shared.utils.PasseDrawer;
import de.dreier.mytargets.shared.views.TargetViewBase;
import de.dreier.mytargets.utils.TextInputDialog;

public class TargetView extends TargetViewBase {

    private static final float ZOOM_FACTOR = 2;
    private static final int SPOT_ZOOM_IN = -3;
    private static final int MODE_CHANGE = -2;
    private static final int NONE = -1;
    private float radius, midX, midY;
    private Paint drawColorP;
    private boolean showAll = false;
    private ArrayList<Passe> mOldShots;
    private Timer longPressTimer;
    private final Handler h = new Handler();
    private float oldRadius;
    private RectF[] spotRects;
    private float orgRadius, orgMidX, orgMidY;
    private boolean spotFocused = false;
    private RectF orgRect;
    private List<ArrowNumber> arrowNumbers = new ArrayList<>();
    private TextPaint mTextPaint;
    private List<Zone> selectableZones = new ArrayList<>();

    public TargetView(Context context) {
        super(context);
        init();
    }

    public TargetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TargetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setPasse(Passe passe) {
        currentArrow = passe.shot.length;
        lastSetArrow = passe.shot.length;
        mPasse = passe;
        mPasseDrawer.setSelection(currentArrow, null, PasseDrawer.MAX_CIRCLE_SIZE);
        mPasseDrawer.setPasse(passe);
        animateFromZoomSpot();
        invalidate();
    }

    public void setArrowNumbers(@NonNull List<ArrowNumber> arrowNumbers) {
        this.arrowNumbers = arrowNumbers;
    }

    public void showAll(boolean showAll) {
        this.showAll = showAll;
        invalidate();
    }

    public void setOldShoots(ArrayList<Passe> oldOnes) {
        mOldShots = oldOnes;
        invalidate();
    }

    @Override
    public void setRoundTemplate(RoundTemplate r) {
        super.setRoundTemplate(r);
        initKeyboard();
        initSpotBounds();
        selectableZones = getZoneList();
    }

    private void initSpotBounds() {
        Rect rect = new Rect(0, 0, 500, 500);
        if (round.target.getFaceCount() > 1) {
            SpotBase spotBase = (SpotBase) round.target;
            spotRects = new RectF[spotBase.getFaceCount()];
            for (int i = 0; i < spotBase.getFaceCount(); i++) {
                spotRects[i] = spotBase.getBoundsF(i, rect);
            }
        } else {
            spotRects = new RectF[1];
            spotRects[0] = new RectF(rect);
        }
    }

    private void init() {
        // Set up a default TextPaint object
        density = getResources().getDisplayMetrics().density;
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(22 * density);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        drawColorP = new Paint();
        drawColorP.setAntiAlias(true);

        if (isInEditMode()) {
            round = new RoundTemplate();
            round.arrowsPerPasse = 3;
            mPasse = new Passe(3);
            mPasseDrawer.setPasse(mPasse);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int curZone;
        if (currentArrow > -1 && currentArrow < round.arrowsPerPasse) {
            curZone = mPasse.shot[currentArrow].zone;
        } else {
            curZone = -2;
        }

        // Draw target
        if (mCurSelecting < -1) {
            drawTarget(canvas, mOutFromX + (midX - mOutFromX) * mCurAnimationProgress,
                    mOutFromY + (midY - mOutFromY) * mCurAnimationProgress,
                    oldRadius + (radius - oldRadius) * mCurAnimationProgress);
        } else {
            if (!mZoneSelectionMode && curZone >= -1) {
                drawZoomedInTarget(canvas);
            } else {
                drawTarget(canvas, midX, midY, radius);
            }
        }

        // Draw right indicator
        drawRightSelectorBar(canvas);

        // Draw all points of this passe at the bottom
        mPasseDrawer.draw(canvas);
    }

    private void drawZoomedInTarget(Canvas canvas) {
        float px = mPasse.shot[currentArrow].x;
        float py = mPasse.shot[currentArrow].y;
        int radius2 = (int) (radius * ZOOM_FACTOR);
        int x = (int) ((midX - orgMidX) * ZOOM_FACTOR + orgMidX - px * (orgRadius + 30 * density));
        int y = (int) ((midY - orgMidY) * ZOOM_FACTOR + orgMidY - py * (orgRadius + 30 * density) -
                60 * density);
        drawTarget(canvas, x, y, radius2);
    }

    private void drawTarget(Canvas canvas, float x, float y, float radius) {
        // Erase background
        drawColorP.setColor(0xfffafafa);
        canvas.drawRect(0, 0, contentWidth, contentHeight, drawColorP);

        // Draw actual target face
        round.target.setBounds((int) (x - radius), (int) (y - radius), (int) (x + radius),
                (int) (y + radius));
        round.target.draw(canvas);

        // Draw exact arrow position
        if (!mZoneSelectionMode) {
            drawArrows(canvas);
        }
    }

    private void drawArrows(Canvas canvas) {
        int spots = round.target.getFaceCount();
        Midpoint[] m = new Midpoint[spots];
        for (int i = 0; i < spots; i++) {
            m[i] = new Midpoint();
        }
        for (int i = 0; i < mPasse.shot.length && i <= lastSetArrow + 1; i++) {
            Shot shot = mPasse.shot[i];
            if (shot.zone == Shot.NOTHING_SELECTED) {
                continue;
            }
            if (i == currentArrow) {
                round.target.drawFocusedArrow(canvas, shot);
                continue;
            }
            round.target.drawArrow(canvas, shot);
            m[i % spots].sumX += shot.x;
            m[i % spots].sumY += shot.y;
            m[i % spots].count++;
        }

        if (showAll) {
            for (Passe p : mOldShots) {
                if (p.getId() != mPasse.getId()) {
                    round.target.drawArrows(canvas, p);
                    for (int i = 0; i < p.shot.length; i++) {
                        m[i % spots].sumX += p.shot[i].x;
                        m[i % spots].sumY += p.shot[i].y;
                        m[i % spots].count++;
                    }
                }
            }
        }

        for (int i = 0; i < spots; i++) {
            if (m[i].count >= 2) {
                round.target.drawArrowAvg(canvas, m[i].sumX / m[i].count,
                        m[i].sumY / m[i].count, i);
            }
        }
    }

    @Override
    protected Coordinate initAnimationPositions(int i) {
        Coordinate coordinate = new Coordinate();
        coordinate.x = orgMidX + orgRadius * mPasse.shot[i].x;
        coordinate.y = orgMidY + orgRadius * mPasse.shot[i].y;
        return coordinate;
    }

    @Override
    public void saveState(Bundle b) {
        super.saveState(b);
        b.putSerializable("oldShots", mOldShots);
    }

    @Override
    public void restoreState(Bundle b) {
        super.restoreState(b);
        mOldShots = (ArrayList<Passe>) b.getSerializable("oldShots");
    }

    @Override
    protected void calcSizes() {
        int availableWidth = mZoneSelectionMode ? (int) (contentWidth - 60 * density) : contentWidth;
        float radH = (contentHeight - 10 * density) / 2.45f;
        float radW = (availableWidth - (mZoneSelectionMode ? 70 : 20) * density) * 0.5f;
        orgRadius = (int) (Math.min(radW, radH));
        orgMidX = availableWidth / 2;
        orgMidY = orgRadius + (int) (10 * density);
        orgRect = new RectF(/**/
                orgMidX - orgRadius,
                orgMidY - orgRadius,
                orgMidX + orgRadius,
                orgMidY + orgRadius);
        RectF rect = new RectF();
        rect.left = 30 * density;
        rect.right = availableWidth - 30 * density;
        rect.top = midY + orgRadius;
        rect.bottom = contentHeight;
        mPasseDrawer.animateToRect(rect);
        animateToZoomSpot();
    }

    private void initKeyboard() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean mode = prefs.getBoolean("target_mode", false);
        switchMode(mode, false);
    }

    public void switchMode(boolean mode, boolean animate) {
        if (mode != mZoneSelectionMode) {
            mZoneSelectionMode = mode;
            if (animate) {
                animateMode();
            }
            if (mZoneSelectionMode) {
                animateFromZoomSpot();
            } else {
                animateToZoomSpot();
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putBoolean("target_mode", mZoneSelectionMode).apply();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Shot getShotFromPos(float x, float y) {
        // Create Shot object
        Shot s = new Shot(currentArrow);
        if (mZoneSelectionMode) {
            if (x > midX + radius + 30 * density) {
                int i = (int) (y * selectableZones.size() / (float) contentHeight);
                s.x = round.target.getXFromZone(s.zone);
                s.y = 0;
                s.zone = selectableZones.get(i).zone;
            } else {
                return null;
            }
        } else { // Handle via target
            s.x = (x - orgMidX) / (orgRadius - 30 * density);
            s.y = (y - orgMidY) / (orgRadius - 30 * density);
            s.zone = round.target.getZoneFromPoint(s.x, s.y);
        }
        return s;
    }

    @Override
    protected boolean selectPreviousShots(MotionEvent motionEvent, float x, float y) {
        // Handle selection of already saved shoots
        int arrow = mPasseDrawer.getPressedPosition(x, y);
        if (arrow != -1 && currentArrow != arrow) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (longPressTimer != null) {
                    mPasseDrawer.setPressed(-1);
                    longPressTimer.cancel();
                    longPressTimer = null;
                    super.onArrowChanged(arrow);
                }
            } else if (mPasseDrawer.getPressed() != arrow) {
                // If new item gets selected cancel old timer and start new one
                mPasseDrawer.setPressed(arrow);
                if (longPressTimer != null) {
                    longPressTimer.cancel();
                }
                longPressTimer = new Timer();
                longPressTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        h.post(TargetView.this::onLongPressArrow);
                    }
                }, 1500);
            }
            invalidate();
            return true;
        } else {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (longPressTimer != null) {
                    longPressTimer.cancel();
                    longPressTimer = null;
                }
            }
            mPasseDrawer.setPressed(-1);
        }
        return false;
    }

    private void animateMode() {
        mCurSelecting = MODE_CHANGE;
        initAnimation();
        calcSizes();

        final ValueAnimator moveAnimator = ValueAnimator.ofFloat(0, 1);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        moveAnimator.addUpdateListener(valueAnimator -> {
            mCurAnimationProgress = (Float) valueAnimator.getAnimatedValue();
            if (mCurAnimationProgress == 1.0f) {
                moveAnimator.cancel();
                mCurSelecting = NONE;
            }
            invalidate();
        });
        moveAnimator.setDuration(300);
        moveAnimator.start();
    }

    private void initAnimation() {
        mCurAnimationProgress = 0;
        mOutFromX = midX;
        mOutFromY = midY;
        oldRadius = radius;
    }

    @Override
    protected void animateFromZoomSpot() {
        if (round.target.dependsOnArrowIndex()) {
            selectableZones = getZoneList();
        }
        if (round.target.getFaceCount() > 1) {
            if (!spotFocused) {
                mCurSelecting = -1;
                animateToZoomSpot();
            } else {
                mCurSelecting = SPOT_ZOOM_IN;
                initAnimation();

                radius = orgRadius;
                midX = orgMidX;
                midY = orgMidY;

                final ValueAnimator moveAnimator = ValueAnimator.ofFloat(0, 1);
                moveAnimator.setInterpolator(
                        currentArrow < round.arrowsPerPasse ? new AccelerateInterpolator() :
                                new AccelerateDecelerateInterpolator());
                moveAnimator.addUpdateListener(valueAnimator -> {
                    mCurAnimationProgress = (Float) valueAnimator.getAnimatedValue();
                    if (mCurAnimationProgress == 1.0f) {
                        moveAnimator.cancel();
                        spotFocused = false;
                        mCurSelecting = -1;
                        animateToZoomSpot();
                    }
                    invalidate();
                });
                moveAnimator.setDuration(200);
                moveAnimator.start();
            }
        }
    }

    @Override
    protected void animateToZoomSpot() {
        if (!spotFocused && mCurSelecting != SPOT_ZOOM_IN) {
            radius = orgRadius;
            midX = orgMidX;
            midY = orgMidY;
        }
        if (round.target.getFaceCount() > 1 && currentArrow < round.arrowsPerPasse && radius > 0 &&
                !spotFocused && !mZoneSelectionMode && mCurSelecting != SPOT_ZOOM_IN) {
            mCurSelecting = SPOT_ZOOM_IN;
            initAnimation();

            RectF spotRect = new RectF(spotRects[currentArrow % spotRects.length]);
            float scale = orgRadius / 250;
            spotRect.left = orgRect.left + spotRect.left * scale;
            spotRect.top = orgRect.top + spotRect.top * scale;
            spotRect.right = orgRect.left + spotRect.right * scale;
            spotRect.bottom = orgRect.top + spotRect.bottom * scale;

            float zoomFactor = orgRadius * 2.0f / spotRect.width();
            radius = (int) (orgRadius * zoomFactor);
            midX = (int) (radius + orgMidX + (orgRect.left - spotRect.centerX()) * zoomFactor);
            midY = (int) (radius + orgMidY + (orgRect.top - spotRect.centerY()) * zoomFactor);

            final ValueAnimator moveAnimator = ValueAnimator.ofFloat(0, 1);
            moveAnimator.setInterpolator(
                    currentArrow == 0 ? new AccelerateDecelerateInterpolator() :
                            new DecelerateInterpolator());
            moveAnimator.addUpdateListener(valueAnimator -> {
                mCurAnimationProgress = (Float) valueAnimator.getAnimatedValue();
                if (mCurAnimationProgress == 1.0f) {
                    moveAnimator.cancel();
                    mCurSelecting = NONE;
                    spotFocused = true;
                }
                invalidate();
            });
            if (currentArrow == 0) {
                moveAnimator.setStartDelay(500);
            }
            moveAnimator.setDuration(200);
            moveAnimator.start();
        }/**/
    }

    @Override
    protected void onArrowChanged(int i) {
        if (arrowNumbers.isEmpty() ||
                currentArrow < round.arrowsPerPasse && mPasse.shot[currentArrow].arrow != -1) {
            super.onArrowChanged(i);
        } else {
            List<ArrowNumber> numbersLeft = new ArrayList<>(arrowNumbers);
            for (Shot s : mPasse.shot) {
                numbersLeft.remove((Integer) s.arrow);
            }
            if (numbersLeft.size() == 0) {
                super.onArrowChanged(i);
                return;
            } else if (numbersLeft.size() == 1) {
                mPasse.shot[currentArrow].arrow = numbersLeft.get(0).number;
                super.onArrowChanged(i);
                return;
            }

            // Prepare grid view
            GridView gridView = new GridView(getContext());

            // Set grid view to alertDialog
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(gridView)
                    .setCancelable(false)
                    .setTitle(R.string.arrow_numbers).create();
            gridView.setAdapter(
                    new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                            numbersLeft));
            int cols = Math.min(5, numbersLeft.size());
            gridView.setNumColumns(cols);
            gridView.setOnItemClickListener((parent, view, position, id) ->
            {
                if (currentArrow < mPasse.shot.length) {
                    mPasse.shot[currentArrow].arrow = numbersLeft.get(position).number;
                }
                dialog.dismiss();
                super.onArrowChanged(i);
            });
            dialog.show();
        }
    }

    /**
     * Draws a rect on the right that shows all possible points.
     *
     * @param canvas Canvas to draw on
     */
    private void drawRightSelectorBar(Canvas canvas) {
        if (mZoneSelectionMode || mCurSelecting == MODE_CHANGE) {
            int selectableZonesCount = selectableZones.size();
            for (int i = 0; i < selectableZonesCount; i++) {
                Zone zone = selectableZones.get(i);

                float percent = 1;
                if (mCurSelecting == MODE_CHANGE) {
                    percent = mZoneSelectionMode ? mCurAnimationProgress : 1 - mCurAnimationProgress;
                }
                int X1 = (int) (contentWidth - 60 * percent * density);
                int X2 = (int) (X1 + 40 * density);
                int Y1 = contentHeight * i / selectableZonesCount;
                int Y2 = contentHeight * (i + 1) / selectableZonesCount;

                drawColorP.setColor(round.target.getFillColor(zone.zone));
                canvas.drawRect(X1, Y1 + density, X2, Y2 - density, drawColorP);

                // For yellow and white background use black font color
                mTextPaint.setColor(round.target.getTextColor(zone.zone));
                canvas.drawText(zone.text, X1 + (X2 - X1) / 2, Y1 + (Y2 - Y1) / 2 + 10 * density, mTextPaint);
            }
        }
    }

    private void onLongPressArrow() {
        final int pressed = mPasseDrawer.getPressed();
        if (pressed == -1) {
            return;
        }
        longPressTimer = null;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        onArrowChanged(round.arrowsPerPasse);

        new TextInputDialog.Builder(getContext())
                .setTitle(R.string.comment)
                .setDefaultText(mPasse.shot[pressed].comment)
                .setOnClickListener(new TextInputDialog.OnClickListener() {

                    @Override
                    public void onCancelClickListener() {
                        mPasseDrawer.setPressed(-1);
                        invalidate();
                    }

                    @Override
                    public void onOkClickListener(String input) {
                        mPasse.shot[pressed].comment = input;
                        if (lastSetArrow + 1 >= round.arrowsPerPasse && setListener != null) {
                            setListener.onTargetSet(new Passe(mPasse), false);
                        }
                        mPasseDrawer.setPressed(-1);
                        invalidate();
                    }
                }).show();
    }

    private ArrayList<Zone> getZoneList() {
        ArrayList<Zone> list = new ArrayList<>();
        String last = "";
        for (int i = 0; i < round.target.getZones(); i++) {
            String zone = round.target.zoneToString(i, currentArrow);
            if (!last.equals(zone)) {
                list.add(new Zone(i, zone));
            }
            last = zone;
        }
        if (!last.equals("M")) {
            list.add(new Zone(-1, "M"));
        }
        return list;
    }

    public boolean getInputMode() {
        return mZoneSelectionMode;
    }

    private class Zone {
        final int zone;
        final String text;

        public Zone(int zone, String text) {
            this.zone = zone;
            this.text = text;
        }
    }

    class Midpoint {
        float count = 0;
        float sumX = 0;
        float sumY = 0;
    }
}
