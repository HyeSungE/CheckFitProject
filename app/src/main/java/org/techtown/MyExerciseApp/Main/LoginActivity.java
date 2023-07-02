package org.techtown.MyExerciseApp.Main;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button login;
    private TextView txt_signup,forgotPasswordTextView;
    private ShowAlertSimpleMessage showAlertSimpleMessage;
    private FirebaseAuth auth;
    private boolean res;
    private BackPressHandler backPressHandler = new BackPressHandler(this);

    @Override
    public void onBackPressed() {
        if(backPressHandler.onBackPressed()){
            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setMessage("시작 화면으로 이동합니다.").setTitle("안내")
                    .setPositiveButton("이동", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //nothing
                        }
                    });
            AlertDialog backAlertDialog = backAlert.create();
            backAlertDialog.show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showAlertSimpleMessage = new ShowAlertSimpleMessage();
        email = findViewById(R.id.login_email_et);
        password = findViewById(R.id.login_pw_et);
        login = findViewById(R.id.login_bt);
        txt_signup = findViewById(R.id.txt_signup);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setMessage("잠시만 기다려 주세요");
                pd.setCancelable(false);
                pd.show();

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_email)) {
                    Toast.makeText(LoginActivity.this, "이메일을 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(str_password)) {
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("USER")
                                                .child(auth.getCurrentUser().getUid());

                                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                boolean b = sendEmailVertification(LoginActivity.this);
                                                if (b) {
                                                    FirebaseUser user = auth.getCurrentUser();
                                                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://myexerciseapp-4e7d1.appspot.com");
                                                    StorageReference storageRef = storage.getReference();
                                                    StorageReference dbRef = storageRef.child("database/EXERCISE_" + user.getUid() + ".db");
                                                    uploadDB();
                                                    File localFile = null;
                                                    try {
                                                        localFile = File.createTempFile("EXERCISE_" + user.getUid(), ".db");
                                                        File finalLocalFile = localFile; localFile.delete();
                                                        dbRef.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                Log.d("success","success");
                                                                copyDB(finalLocalFile);
                                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                                //finish();
                                                                //token
                                                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                                    @Override
                                                                    public void onSuccess(String s) {
                                                                        FirebaseDatabase.getInstance().getReference("TOKEN").child(user.getUid()).setValue(s).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                pd.dismiss();
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                Log.d("onFailure","onFailure");
                                                                try{
                                                                    InputStream inputStream = LoginActivity.this.getAssets().open("EXERCISE.db");
                                                                    UploadTask uploadTask = dbRef.putStream(inputStream);
                                                                    uploadTask.addOnCompleteListener(new OnCompleteListener() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task task) {
                                                                            Log.d("onComplete","onComplete");
                                                                            dbRef.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                                                        @Override
                                                                                        public void onSuccess(String s) {
                                                                                            FirebaseDatabase.getInstance().getReference("TOKEN").child(user.getUid()).setValue(s).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    copyDB(finalLocalFile);
                                                                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                    startActivity(intent);
                                                                                                    pd.dismiss();
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                    //finish();
                                                                                    //token
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }catch (IOException e){
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }


                                                } else {
                                                    showAlertSimpleMessage.show(LoginActivity.this, "알림", "이메일을 먼저 인증해 주세요");
                                                    pd.dismiss();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                pd.dismiss();
                                            }
                                        });
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean sendEmailVertification(Context context) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (auth.getCurrentUser().isEmailVerified()) {
                Log.d(TAG, "이 사용자는 이미 이메일 인증이 완료된 상태입니다.");
                return true;
            }
            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showAlertSimpleMessage.show(context, "인증", "가입하신 이메일로 인증메일이 발송되었습니다.\n 인증 메일의 링크를 눌러주세요");
                }
            });
        }
        return false;
    }
    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.activity_forgot_password);
        auth = FirebaseAuth.getInstance();
        EditText emailEditText = dialog.findViewById(R.id.forgot_email_et);
        Button resetButton = dialog.findViewById(R.id.reset_password_bt);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(LoginActivity.this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("잠시만 기다려 주세요...");
                    progressDialog.show();

                    auth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "이메일 전송에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        dialog.show();
    }
    private void copyDB(File file) {
        FirebaseUser user = auth.getCurrentUser();
        String DB_NAME = "EXERCISE_" + user.getUid() + ".db";
        String DB_PATH = "/data/data/org.techtown.MyExerciseApp/databases/";
        String TAG = "DataBaseHelper";
        File dbFile = new File(DB_PATH + DB_NAME);
        if (!dbFile.exists()) {
            System.out.println("copy");
            try {
                File folder = new File(DB_PATH);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                InputStream inputStream = file.toURL().openStream();
//                StorageReference dbRef = FirebaseStorage.getInstance().getReference().child("database/EXERCISE_" + user.getUid() + ".db");
//                UploadTask uploadTask = dbRef.putStream(inputStream);
                String out_filename = DB_PATH + DB_NAME;
                OutputStream outputStream = new FileOutputStream(out_filename, false);
                byte[] mBuffer = new byte[1024];
                int mLength;
                while ((mLength = inputStream.read(mBuffer)) > 0) {
                    outputStream.write(mBuffer, 0, mLength);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Database is copied.");
        }

        AppDatabase.renewInstance(LoginActivity.this.getApplicationContext());



    }
    private void uploadDB() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String DB_NAME = "EXERCISE_" + user.getUid() + ".db";
        String DB_PATH = "/data/data/org.techtown.MyExerciseApp/databases/";
        String TAG = "DataBaseHelper";
        File dbFile = new File(DB_PATH + DB_NAME);
        if (dbFile.exists()) {
            try {
                File folder = new File(DB_PATH);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                InputStream inputStream = dbFile.toURL().openStream();
                StorageReference dbRef = FirebaseStorage.getInstance().getReference().child("database/EXERCISE_" + user.getUid() + ".db");
                UploadTask uploadTask = dbRef.putStream(inputStream);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //  Toast.makeText(getContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                            Log.d("uploadSuccess", "Successfully uploaded");
                        }else{
                            Log.d("uploadFail", "Fail uploaded");
                        }
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }
            Log.d(TAG, "Database is uploaded.");
        }
        AppDatabase.renewInstance(LoginActivity.this);
    }
}
