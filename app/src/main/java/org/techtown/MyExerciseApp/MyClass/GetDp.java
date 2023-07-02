package org.techtown.MyExerciseApp.MyClass;

import android.content.Context;
import android.util.DisplayMetrics;

public class GetDp {

    public GetDp() {}

    public int getDp(Context context,int size) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int dp = Math.round(size * dm.density);
        return dp;
    }

}
