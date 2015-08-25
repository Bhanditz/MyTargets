package de.dreier.mytargets.shared.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.dreier.mytargets.shared.models.Coordinate;
import de.dreier.mytargets.shared.models.Passe;
import de.dreier.mytargets.shared.models.RoundTemplate;
import de.dreier.mytargets.shared.models.Shot;
import de.dreier.mytargets.shared.utils.OnTargetSetListener;
import de.dreier.mytargets.shared.utils.PasseDrawer;

/**
 * Created by Florian on 18.03.2015.
 */
public abstract class TargetViewBase extends View implements View.OnTouchListener {
    protected PasseDrawer mPasseDrawer;
    protected int currentArrow = 0;
    protected int lastSetArrow = -1;
    protected Passe mPasse;
    protected RoundTemplate round;
    protected int mCurSelecting = -1;
    protected int contentWidth;
    protected int contentHeight;
    protected OnTargetSetListener setListener = null;
    protected float mCurAnimationProgress;
    protected boolean mKeyboardMode = true;
    protected float density;
    protected int mZoneCount;
    protected float mOutFromX;
    protected float mOutFromY;

    public TargetViewBase(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    protected TargetViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    protected TargetViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    public void reset() {
        currentArrow = 0;
        lastSetArrow = -1;
        mCurSelecting = -1;
        mPasse = new Passe(round.arrowsPerPasse);
        mPasseDrawer.setPasse(mPasse);
        animateToZoomSpot();
        invalidate();
    }

    public void setRoundTemplate(RoundTemplate r) {
        round = r;
        mZoneCount = r.target.getZones();
        mPasseDrawer = new PasseDrawer(this, density, round.target);
        reset();
    }

    protected abstract Coordinate initAnimationPositions(int i);

    public void saveState(Bundle b) {
        b.putSerializable("passe", mPasse);
        b.putSerializable("round", round);
        b.putInt("currentArrow", currentArrow);
        b.putInt("lastSetArrow", lastSetArrow);
        mPasseDrawer.saveState(b);
    }

    public void restoreState(Bundle b) {
        mPasse = (Passe) b.getSerializable("passe");
        currentArrow = b.getInt("currentArrow");
        lastSetArrow = b.getInt("lastSetArrow");
        round = (RoundTemplate) b.getSerializable("round");
        mPasseDrawer.restoreState(b);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        contentWidth = getWidth();
        contentHeight = getHeight();
        calcSizes();
    }

    protected abstract void calcSizes();

    public void setOnTargetSetListener(OnTargetSetListener listener) {
        setListener = listener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Cancel animation
        if (mCurSelecting != -1) {
            mPasseDrawer.cancel();
        }

        float x = motionEvent.getX();
        float y = motionEvent.getY();

        if (selectPreviousShots(motionEvent, x, y)) {
            return true;
        }

        Shot shot = getShotFromPos(x, y);
        if (shot == null) {
            return true;
        }

        // If a valid selection was made save it in the passe
        if (currentArrow < round.arrowsPerPasse &&
                (mPasse.shot[currentArrow].zone != shot.zone || !mKeyboardMode)) {
            mPasse.shot[currentArrow].zone = shot.zone;
            mPasse.shot[currentArrow].x = shot.x;
            mPasse.shot[currentArrow].y = shot.y;
            mPasseDrawer.setSelection(currentArrow, initAnimationPositions(currentArrow), 0);
            invalidate();
        }

        // If finger is released go to next shoot
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            //go to next page
            if (currentArrow == lastSetArrow + 1) {
                lastSetArrow++;
            }

            onArrowChanged(lastSetArrow + 1);
            return true;
        }
        return true;
    }

    protected void onArrowChanged(final int i) {
        animateCircle(i);
        animateFromZoomSpot();

        if (lastSetArrow + 1 >= round.arrowsPerPasse && setListener != null) {
            mPasse.setId(setListener.onTargetSet(new Passe(mPasse), false));
        }
    }

    private void animateCircle(int i) {
        Coordinate pos = null;
        int nextSel = i;
        if (i > -1 && i < round.arrowsPerPasse && mPasse.shot[i].zone > Shot.NOTHING_SELECTED) {
            pos = initAnimationPositions(i);
        } else {
            nextSel = PasseDrawer.NO_SELECTION;
        }
        if (mKeyboardMode) {
            mPasseDrawer.setSelection(nextSel, pos, 0);
            invalidate();
        } else {
            mPasseDrawer.animateToSelection(nextSel, pos, 0);
        }
        currentArrow = i;
    }

    protected void animateFromZoomSpot() {
    }

    protected void animateToZoomSpot() {
    }

    /**
     * Creates a {@link Shot} object given a position
     *
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @return Returns a fully populated {@link Shot} object or null if the position is not a valid shot
     */
    protected abstract Shot getShotFromPos(float x, float y);

    protected abstract boolean selectPreviousShots(MotionEvent motionEvent, float x, float y);
}
