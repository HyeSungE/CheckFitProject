package org.techtown.MyExerciseApp.MyClass;

import android.app.AlertDialog;
import android.content.Context;

public class ShowAlertSimpleMessage {
    private  AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    public ShowAlertSimpleMessage() {

    }

    public void show(Context context, String title, String content) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(content);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void dismiss(){
        alertDialog.dismiss();
    }

}
