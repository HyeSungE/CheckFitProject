package org.techtown.MyExerciseApp.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.R;

public class StartActivity extends AppCompatActivity {

    Button startLoginBt, startRegisterBt;

    FirebaseUser firebaseUser;

    private BackPressHandler backPressHandler = new BackPressHandler(this);

    @Override
    public void onBackPressed() {
        if(backPressHandler.onBackPressed()){
            android.app.AlertDialog.Builder backAlert = new android.app.AlertDialog.Builder(this);
            backAlert.setMessage("앱을 종료 하시겠습니까 ?").setTitle("안내")
                    .setPositiveButton("이동", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //nothing
                        }
                    });
            android.app.AlertDialog backAlertDialog = backAlert.create();
            backAlertDialog.show();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //check if user is null
//        if (firebaseUser != null){
//            Intent intent = new Intent(StartActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startLoginBt = findViewById(R.id.start_login_bt);
        startRegisterBt = findViewById(R.id.start_register_bt);

        startLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        startRegisterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}