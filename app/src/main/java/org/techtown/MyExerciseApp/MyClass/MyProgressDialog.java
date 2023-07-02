package org.techtown.MyExerciseApp.MyClass;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ProgressBar;

public class MyProgressDialog {
    private Dialog progressDialog;
    public MyProgressDialog(Context context) {
        Dialog progressDialog = new Dialog(context);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setContentView(new ProgressBar(context));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ((Activity)context).finish();
            }
        });
        this.progressDialog = progressDialog;
    }
    public void showProgressDialog(){
        this.progressDialog.show();
    }
    public void dismissProgressDialog(){
        this.progressDialog.dismiss();
    }
}
