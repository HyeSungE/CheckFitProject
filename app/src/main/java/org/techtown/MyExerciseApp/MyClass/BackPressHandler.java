package org.techtown.MyExerciseApp.MyClass;

import android.app.Activity;
import android.widget.Toast;

public class BackPressHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressHandler(Activity activity){
        this.activity = activity;
    }
    public boolean onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return false;
        }
        else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();
            return true;
        }
        return false;
    }

    private void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

}
