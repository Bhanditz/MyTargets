package de.dreier.mytargets.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.dreier.mytargets.models.LinearSeries;
import de.dreier.mytargets.models.RectD;
import de.dreier.mytargets.models.Target;
import de.dreier.mytargets.utils.TargetOpenHelper;

public class ChartView extends RelativeLayout {

	// View
	private Paint mPaint = new Paint();
	private Paint mTextPaint = new Paint();

	// Series
	private List<LinearSeries> mSeries = new ArrayList<LinearSeries>();

	private int mLeftLabelWidth;
	private int mTopLabelHeight;
	private int mRightLabelWidth;
	private int mBottomLabelHeight;
	private float mLabelTextSize;

	// Range
	private RectD mValueBounds = new RectD();
	private long mMinX = Long.MAX_VALUE;
	private long mMaxX = Long.MIN_VALUE;

	// Grid
	private Rect mGridBounds = new Rect();
	private final int mGridLineWidth;

	private SimpleDateFormat dateFormatMonth;

	private long mMinY = 0;
	private long mMaxY = 15;

	private float mDensity;
    private TargetOpenHelper.Round mRoundInfo;

    private enum Axis { X, Y }

	public ChartView(Context context) {
		this(context, null, 0);
	}

	public ChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mDensity = metrics.density;

		setWillNotDraw(false);
		setBackgroundColor(Color.TRANSPARENT);

		mGridLineWidth = (int) mDensity;
		mLeftLabelWidth = (int) (28*mDensity);
		mTopLabelHeight = (int) (10*mDensity);
		mBottomLabelHeight = (int) (20*mDensity);
		mLabelTextSize = 16f*mDensity;
		
		// Apply the label text settings to the text painter
		mTextPaint.setColor(Color.DKGRAY);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mLabelTextSize);

		/*Calendar cal = (Calendar)QDateMgr.beg_hj[0].clone();
		mMinX = cal.getTimeInMillis();
		cal.add(Calendar.MONTH, 1);
		mMaxX = cal.getTimeInMillis();*/
		
		dateFormatMonth = new SimpleDateFormat("MMM",Locale.GERMANY);
	}

	/*
	 * Remove all lines from the chart
	 */
	public void clearSeries() {
		mSeries.clear();
		resetRange();
		invalidate();
	}

	/*
	 * Add a series to the chart
	 */
	public void addSeries(LinearSeries series) {
		// Add the series
		mSeries.add(series);

		// Make sure the chart is the right size
		resetRange();

		// And redraw
		invalidate();
	}

	// Reset the visible range to show nothing
	public void resetRange() {
		/*Calendar cal = (Calendar)QDateMgr.beg_hj[0].clone();
		mMinX = cal.getTimeInMillis();
		cal.add(Calendar.MONTH, 1);
		mMaxX = cal.getTimeInMillis();*/

		for(LinearSeries series : mSeries) {
			if (series.getMinX() < mMinX) mMinX = series.getMinX();
			if (series.getMaxX() > mMaxX) mMaxX = series.getMaxX();
		}

		mValueBounds.set(mMinX, mMinY, mMaxX, mMaxY);
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		final int gridLeft = mLeftLabelWidth + mGridLineWidth - 1;
		final int gridTop = mTopLabelHeight + mGridLineWidth - 1;
		final int gridRight = getWidth() - mRightLabelWidth - mGridLineWidth;
		final int gridBottom = getHeight() - mBottomLabelHeight - mGridLineWidth;

		mGridBounds.set(gridLeft, gridTop, gridRight, gridBottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw on the grid lines and labels
		float scaleX = drawGridFixedGap(canvas, Axis.X);
		float scaleY = drawGridFixedGap(canvas, Axis.Y);

		// Draw on the series
		for (LinearSeries series : mSeries)
			series.draw(canvas, mGridBounds, mValueBounds, scaleX, scaleY);
	}
	
	public void setYRange(long min, long max) {
		mMinY = min;
		mMaxY = max;
		mValueBounds.set(mMinX, mMinY, mMaxX, mMaxY);
	}

	// Draw a grid with lines at every point which == 0 modulo a fixed gap
	private float drawGridFixedGap(Canvas canvas, Axis axis) {
		mPaint.setColor(0xFFE5E5E5);
		mPaint.setStrokeWidth(mGridLineWidth);

		long minPoint = axis == Axis.X ? mValueBounds.left : mValueBounds.top;
		long maxPoint = axis == Axis.X ? mValueBounds.right : mValueBounds.bottom;

		Long pointCoord;
		final int originPointCoord = axis == Axis.X ? mGridBounds.left : mGridBounds.top;

		// Enclose the grid on both sides for neatness
		if (axis == Axis.X) {
			canvas.drawLine(mGridBounds.left, mGridBounds.top,
					mGridBounds.left, mGridBounds.bottom, mPaint);
			canvas.drawLine(mGridBounds.right, mGridBounds.top,
					mGridBounds.right, mGridBounds.bottom, mPaint);
		} else {
			canvas.drawLine(mGridBounds.left, mGridBounds.top,
					mGridBounds.right, mGridBounds.top, mPaint);
			canvas.drawLine(mGridBounds.left, mGridBounds.bottom,
					mGridBounds.right, mGridBounds.bottom, mPaint);
		}

		int drawn = 0;
		if (axis == Axis.X) {
			final float gridWidth = mGridBounds.width();
			final float valueWidth = mValueBounds.width();
            return gridWidth / valueWidth;
		} else {
			final float gridHeight = mGridBounds.height();
			float valueHeight = maxPoint-minPoint;
			final int step=1;
			final float scaleY = gridHeight / valueHeight;
			for (long point = minPoint; point <= maxPoint && drawn < 50; // Go right up to the maximum
												// point, but because this
												// comparison isn't 100%
												// reliable, draw at most 50
												// lines
			point++, drawn++ // Move along by the specified amount each
									// time
			) {
				// Get the drawing co-ordinate for this line: get the distance
				// it should be in value from the left, scale
				// that to the drawing distance, and move it away from the
				// origin co-ordinate
				pointCoord = (long) (originPointCoord + (scaleY * (point - minPoint)));

				// Points
				// Draw a horizontal line at this y-value
				canvas.drawLine(mGridBounds.left, pointCoord.floatValue(),
						mGridBounds.right, pointCoord.floatValue(), mPaint);
				// And the text label
				if (point <= maxPoint)
					canvas.drawText(""+ Target.getStringByZone(mRoundInfo.target, (int)point, mRoundInfo.compound),
							mLeftLabelWidth / 2, // centre it in the left label
													// gutter
							pointCoord.floatValue() + (mLabelTextSize / 2),
							// since the text is drawn from the middle-bottom we
							// need to push it down a little more
							mTextPaint);
			}
			return scaleY;
		}
	}

    public void setRoundInfo(TargetOpenHelper.Round round) {
        mRoundInfo = round;
        setYRange(0, Target.target_rounds[round.target].length);
    }
}