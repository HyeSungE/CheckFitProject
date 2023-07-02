package org.techtown.MyExerciseApp.Home;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {

    private CalendarDay today;
    public TodayDecorator(CalendarDay today){
        this.today = today;

    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(today);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.addSpan(new RelativeSizeSpan(1.4f));
        //view.addSpan(new ForegroundColorSpan(Color.parseColor("#87CEEB")));
    }
}
