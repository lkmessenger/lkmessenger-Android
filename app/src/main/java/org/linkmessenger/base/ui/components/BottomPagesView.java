package org.linkmessenger.base.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import org.linkmessenger.base.ui.AndroidUtilities;
import org.thoughtcrime.securesms.R;

public class BottomPagesView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress;
    private int scrollPosition;
    private int currentPage;
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private RectF rect = new RectF();
    private float animatedProgress;
    private ViewPager viewPager;
    public int pagesCount;

    public BottomPagesView(Context context, ViewPager pager, int count) {
        super(context);
        viewPager = pager;
        pagesCount = count;
    }

    public void setPageOffset(int position, float offset) {
        progress = offset;
        scrollPosition = position;
        invalidate();
    }

    public void setCurrentPage(int page) {
        currentPage = page;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float d = AndroidUtilities.dp(5);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.signal_colorSurface1));

        int x;
        currentPage = viewPager.getCurrentItem();
        for (int a = 0; a < pagesCount; a++) {
            if (a == currentPage) {
                continue;
            }
            x = a * AndroidUtilities.dp(11);
            rect.set(x, 0, x + AndroidUtilities.dp(5), AndroidUtilities.dp(5));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(2.5f), AndroidUtilities.dp(2.5f), paint);
        }
        paint.setColor(ContextCompat.getColor(getContext(), R.color.signal_colorPrimary));

        x = currentPage * AndroidUtilities.dp(11);
        if (progress != 0) {
            if (scrollPosition >= currentPage) {
                rect.set(x, 0, x + AndroidUtilities.dp(5) + AndroidUtilities.dp(11) * progress, AndroidUtilities.dp(5));
            } else {
                rect.set(x - AndroidUtilities.dp(11) * (1.0f - progress), 0, x + AndroidUtilities.dp(5), AndroidUtilities.dp(5));
            }
        } else {
            rect.set(x, 0, x + AndroidUtilities.dp(5), AndroidUtilities.dp(5));
        }
        canvas.drawRoundRect(rect, AndroidUtilities.dp(2.5f), AndroidUtilities.dp(2.5f), paint);
    }
}

