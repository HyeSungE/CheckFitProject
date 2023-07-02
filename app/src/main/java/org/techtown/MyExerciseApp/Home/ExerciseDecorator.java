package org.techtown.MyExerciseApp.Home;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;

public class ExerciseDecorator implements DayViewDecorator {

  //  private final Hashtable<CalendarDay, Integer> doExerciseDays;
    private int round;

    private static final float DEFAULT_DOT_RADIUS = 3;
    //Note that negative values indicate a relative offset to the LEFT
    private static final int[] xOffsets = new int[]{0,-10,10,-20};
    private int color;
    private HashSet<CalendarDay> dates;
    private float dotRadius;
    private int spanType;

    public ExerciseDecorator(int color, float dotRadius, int spanType) {
        this.color = color;
        this.dotRadius = dotRadius;
        this.dates = new HashSet<>();
        this.spanType = spanType;
    }
    public boolean addDate(CalendarDay day){
        return dates.add(day);
    }

//    public ExerciseDecorator(Hashtable<CalendarDay, Integer> doExerciseDays) {
//        this.doExerciseDays = doExerciseDays;
//    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        LineBackgroundSpan span = new CustomSpan(color, xOffsets[spanType]);
        view.addSpan(span);
    }

    private static class CustomSpan extends DotSpan {
        private final int color;
        private final int xOffset;
        private final float radius = 8;

        CustomSpan(int color, int xOffset) {
            this.color = color;
            this.xOffset = xOffset;
        }

        /*Note! The following code is more or less copy-pasted from the DotSpan class. I have commented the changes below.*/
        @Override
        public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline,
                                   int bottom, CharSequence text, int start, int end, int lnum) {
            int oldColor = paint.getColor();
            if (color != 0) {
                paint.setColor(color);
            }
            int x = ((left + right) / 2); /*This is the x-coordinate right
    below the date. If we add to x, we will draw the
    circle to the right of the date and vice vers if we subtract from x.*/
            canvas.drawCircle(x + xOffset * 2, bottom + radius, radius, paint);

            paint.setColor(oldColor);
        }

    }

}
