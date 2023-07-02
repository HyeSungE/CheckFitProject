package org.techtown.MyExerciseApp.Main;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.MyProgressDialog;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {


    private int BOX_STROKE_COLOR;

    private static final String userNameHelper = "한글 이름 : 2 ~ 4글자\n영어 이름 : First Name(2-15) Last Name(2-15)";
    private static final String emailHelper = "메일 인증 링크가 발송되는 이메일입니다.";
    private static final String passwordHelper = "최소 6 자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자를 포함합니다.";
    private static final String usernameRegex1 = "^[가-힣]{2,4}$";
    private static final String usernameRegex2 = "^[a-zA-Z]{2,15}\\s[a-zA-Z]{2,15}$";
    private static final String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&_.])[A-Za-z[0-9]$@$!%*#?&_.]{6,}$";
    private final ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();

    private EditText username, email, password, password_2, firstNum, midNum, lastNum, verifiPhoneEt;
    private Button register, sendPhoneBt, verifiPhoneBt;
    private TextView txt_login;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private LinearLayout activityRegisterLl;
    private TextInputLayout tilName, tilEmail, tilPw1, tilPw2;
    private @ColorInt
    int TilStrokeColor;
    private ColorStateList TilHinTextColorStateList;
    private boolean usernameB, emailB, passwordB, passwordEqualB;

    //vertification phone number
    private boolean authOk; //폰 인증 정상적인지
    private boolean requestedAuth; // 폰인증 요청을 보냈는지 -> editext enabled
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken otp;
    private LinearLayout verificationNumberLl;
    private String phoneNumber;
    private boolean emailOk;
    private HashMap<String, String> smsCodeMap;
    private HashMap<String, PhoneAuthProvider.ForceResendingToken> tokenMap;

    private MyProgressDialog myProgressDialog;
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
                            Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
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
        setContentView(R.layout.activity_register);

        myProgressDialog = new MyProgressDialog(RegisterActivity.this);
        BOX_STROKE_COLOR = ContextCompat.getColor(RegisterActivity.this, R.color.md_green_A700);
        activityRegisterLl = findViewById(R.id.activity_register_ll);
        activityRegisterLl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        authOk = false;
        requestedAuth = false;
        emailOk = false;
        username = findViewById(R.id.register_username);
        email = findViewById(R.id.register_email_et);
        password = findViewById(R.id.register_password_et);
        password_2 = findViewById(R.id.register_password_et2);
        register = findViewById(R.id.register_bt);
        register.setEnabled(false);
        txt_login = findViewById(R.id.txt_login);
        tilName = findViewById(R.id.til_register_username);
        tilName.setHelperText(userNameHelper);
        tilEmail = findViewById(R.id.til_register_email);
        tilEmail.setHelperText(emailHelper);
        tilPw1 = findViewById(R.id.til_register_password);
        tilPw1.setHelperText(passwordHelper);
        tilPw2 = findViewById(R.id.til_register_password2);

        sendPhoneBt = findViewById(R.id.register_send_phone_bt);
        verifiPhoneBt = findViewById(R.id.register_verifi_pn_bt);
        verifiPhoneEt = findViewById(R.id.register_verifi_num_et);

        firstNum = findViewById(R.id.register_first_pn);
        midNum = findViewById(R.id.register_mid_pn);
        lastNum = findViewById(R.id.register_last_pn);
        verificationNumberLl = findViewById(R.id.register_verificaton_number_ll);
        verificationNumberLl.setVisibility(View.GONE);

        TilStrokeColor = tilEmail.getBoxStrokeColor();
        TilHinTextColorStateList = tilEmail.getHintTextColor();

        smsCodeMap = new HashMap<>();
        tokenMap = new HashMap<>();
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myProgressDialog.showProgressDialog();
                String str_username = username.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();
                String str_password_2 = password_2.getText().toString();

                if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    myProgressDialog.dismissProgressDialog();
                    Toast.makeText(RegisterActivity.this, "빈 칸의 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (str_password.length() < 6 || str_password_2.length() < 6) {
                    myProgressDialog.dismissProgressDialog();
                    Toast.makeText(RegisterActivity.this, "6자리 이상의 비밀번호를 설정해주세요", Toast.LENGTH_SHORT).show();
                    if (str_password.length() < 6) password.requestFocus();
                    else password_2.requestFocus();
                } else if (!str_password.equals(str_password_2)) {
                    myProgressDialog.dismissProgressDialog();
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    password.requestFocus();
                } else {
                    register(str_username, str_email, str_password, phoneNumber);

                }
            }
        });

        sendPhoneBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                myProgressDialog.showProgressDialog();
                if (sendPhoneBt.getText().toString().equals("인증번호")) {
                    phoneNumber = setPhoneNumber(
                            firstNum.getText().toString() + midNum.getText().toString() + lastNum.getText().toString()
                    );
                    if (phoneNumber != null) {
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks callback =
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        Log.d(TAG, "Register Activity/sendPhoneBt.setOnClickListener/onVerificationCompleted");
                                        myProgressDialog.dismissProgressDialog();
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        Log.d(TAG, "Register Activity/sendPhoneBt.setOnClickListener/onVerificationFailed" + e.getMessage());
                                        if (e instanceof FirebaseAuthInvalidCredentialsException) {

                                        } else if (e instanceof FirebaseTooManyRequestsException) {

                                        }
                                        myProgressDialog.dismissProgressDialog();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        super.onCodeSent(s, forceResendingToken);
                                        requestedAuth = true;
                                        verificationId = s;
                                        otp = forceResendingToken;
                                        smsCodeMap.put(phoneNumber, s);
                                        tokenMap.put(phoneNumber, otp);

                                        Log.d(TAG, "Register Activity/sendPhoneBt.setOnClickListener/onCodeSent/" + s);
                                        firstNum.setEnabled(false);
                                        midNum.setEnabled(false);
                                        lastNum.setEnabled(false);
                                        setVerifiPhoneNum(true);
                                        myProgressDialog.dismissProgressDialog();
                                        Toast.makeText(RegisterActivity.this, "인증번호를 전송했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                };

                        PhoneAuthOptions phoneAuthOptions = null;
                        auth.setLanguageCode("kr");
                        if (smsCodeMap.get(phoneNumber) != null && tokenMap.get(phoneNumber) != null) {
                            phoneAuthOptions =
                                    PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber).setTimeout(120l, TimeUnit.SECONDS)
                                            .setActivity(RegisterActivity.this).setCallbacks(callback).setForceResendingToken(tokenMap.get(phoneNumber)).build();
                        } else {
                            phoneAuthOptions =
                                    PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber).setTimeout(120l, TimeUnit.SECONDS)
                                            .setActivity(RegisterActivity.this).setCallbacks(callback).build();
                        }

                        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
                        sendPhoneBt.setText("번호초기화");
                        setEnabledRegisterBt();


                    } else {
                        Toast.makeText(RegisterActivity.this, "전화번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                        myProgressDialog.dismissProgressDialog();
                    }
                } else if (sendPhoneBt.getText().toString().equals("번호초기화")) {

                    firstNum.setEnabled(true);
                    midNum.setEnabled(true);
                    lastNum.setEnabled(true);
                    firstNum.setText("");
                    midNum.setText("");
                    lastNum.setText("");
                    verificationNumberLl.setVisibility(View.GONE);
                    authOk = false;
                    sendPhoneBt.setText("인증번호");
                    myProgressDialog.dismissProgressDialog();
                }
            }
        });
        verifiPhoneBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PhoneAuthCredential phoneAuthCredential =
                            PhoneAuthProvider.getCredential(verificationId, verifiPhoneEt.getText().toString());
                    loginPhoneNumber(phoneAuthCredential, "success");
                    setEnabledRegisterBt();
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "인증번호를 인증하는 과정에서 문제 발생", Toast.LENGTH_SHORT).show();
                }


            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = username.getText().toString();
                usernameB = patternMatch(tilName, str, usernameRegex1, usernameRegex2);
                setEnabledRegisterBt();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = username.getText().toString();
                usernameB = patternMatch(tilName, str, usernameRegex1, usernameRegex2);
                setEnabledRegisterBt();
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = email.getText().toString();
                emailB = patternMatch(tilEmail, str, emailRegex, null);
                setEnabledRegisterBt();
                emailOk = false;

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = email.getText().toString();
                emailB = patternMatch(tilEmail, str, emailRegex, null);
                setEnabledRegisterBt();

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = password.getText().toString();
                passwordB = patternMatch(tilPw1, str, passwordRegex, null);
                setEnabledRegisterBt();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = password.getText().toString();
                passwordB = patternMatch(tilPw1, str, passwordRegex, null);
                setEnabledRegisterBt();
            }
        });
        password_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = password_2.getText().toString();

                if (password.getText().toString().equals(str) && !str.isEmpty()) {
                    successTIL(tilPw2);
                    passwordEqualB = true;
                } else {
                    errorTIL(tilPw2);
                    passwordEqualB = false;
                }
                setEnabledRegisterBt();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = password_2.getText().toString();
                if (password.getText().toString().equals(str)) {
                    successTIL(tilPw2);
                    passwordEqualB = true;
                } else {
                    errorTIL(tilPw2);
                    passwordEqualB = false;
                }
                setEnabledRegisterBt();
            }
        });
    }

    private void register(final String username, String email, String password, String phoneNumber) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String uid = firebaseUser.getUid();
                    reference = reference.child("USER").child(uid);
                    User user = new User(uid, username, email, password, phoneNumber,
                            "default_profile_image.png", email.split("@")[0],
                            new GetToday().getTodayTime(), "기본 소개입니다. *^^*");

                    reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseDatabase.getInstance().getReference().child("NICKNAME").child(email.split("@")[0]).setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myProgressDialog.dismissProgressDialog();
                                            //new UploadDb(auth,RegisterActivity.this,"assets");
                                            showAlertSimpleMessage.show(RegisterActivity.this, "회원가입 완료", "가입 시, 사용한 이메일에 전송된 " +
                                                    "인증 이메일의 링크를 눌러 회원가입을 완료해주세요");
                                            Intent intent = new Intent(RegisterActivity.this, SuccessRegisterActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    myProgressDialog.dismissProgressDialog();
                    showAlertSimpleMessage.show(RegisterActivity.this, "실패", "중복된 이메일이 존재합니다. 이메일을 변경해주세요");
                    //Toast.makeText(RegisterActivity.this, "중복된 이메일이 존재합니다. 이메일을 변경해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void loginPhoneNumber(PhoneAuthCredential phoneAuthCredential, String mode) {
        myProgressDialog.showProgressDialog();
        auth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            authOk = true;
                            requestedAuth = false;
                            Objects.requireNonNull(auth.getCurrentUser()).delete();
                            auth.signOut();
                            myProgressDialog.dismissProgressDialog();
                            if (mode.equals("success")) {
                                Toast.makeText(RegisterActivity.this, "인증번호가 일치합니다.\n핸드폰 인증을 완료합니다.", Toast.LENGTH_SHORT).show();
                                setVerifiPhoneNum(requestedAuth);
                            } else if (mode.equals("resend")) {
                                Toast.makeText(RegisterActivity.this, "인증번호를 재전송합니다.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            if (mode.equals("success")) {
                                myProgressDialog.dismissProgressDialog();
                                Toast.makeText(RegisterActivity.this, "인증번호가 일치하지 않습니다.\n인증번호를 확인해 주세요", Toast.LENGTH_SHORT).show();
                                setVerifiPhoneNum(requestedAuth);
                            }

                        }

                    }
                });

    }

    private void setVerifiPhoneNum(boolean b) {
        if (b) {
            verificationNumberLl.setVisibility(View.VISIBLE);
        } else {
            verificationNumberLl.setVisibility(View.GONE);
            sendPhoneBt.setText("인증완료");
            sendPhoneBt.setEnabled(false);
        }
    }

    private boolean patternMatch(TextInputLayout til, String str, String regex1, String regex2) {
        Pattern pattern1 = Pattern.compile(regex1);

        Matcher m1 = pattern1.matcher(str);

        if (str.isEmpty()) {
            resetTIL(til);
            return false;
        } else if (regex2 == null) {
            if (m1.matches()) {
                successTIL(til);

                return true;
            } else {
                errorTIL(til);

                return false;
            }
        } else {
            Pattern pattern2 = Pattern.compile(regex2);
            Matcher m2 = pattern2.matcher(str);
            if ((!m1.matches() && m2.matches()) || (!m2.matches() && m1.matches())) {
                successTIL(til);
                return true;
            } else {
                errorTIL(til);
                return false;
            }
        }
    }

    private void setEnabledRegisterBt() {
        register.setEnabled(usernameB && emailB && passwordB && passwordEqualB && authOk);
    }

    private void successTIL(TextInputLayout til) {
        til.setBoxStrokeColor(BOX_STROKE_COLOR);
        til.setHintTextColor(ColorStateList.valueOf(BOX_STROKE_COLOR));
        til.setHelperText(" Success !");
    }

    private void errorTIL(TextInputLayout til) {
        til.setErrorEnabled(true);
        if (Objects.requireNonNull(til.getHint()).toString().contains("확인")) {
            til.setError("비밀번호가 다릅니다. 다시 입력해 주세요");
        } else {
            til.setError("다시 입력해주세요");
        }

    }

    private void resetTIL(TextInputLayout til) {
        til.setErrorEnabled(false);
        til.setError(null);
        til.setHelperText(getTilHelp(til));
        til.setBoxStrokeColor(TilStrokeColor);
        til.setHintTextColor(TilHinTextColorStateList);
    }

    private String getTilHelp(TextInputLayout TIL) {
        switch (Objects.requireNonNull(TIL.getHint()).toString()) {
            case "이름": {
                return userNameHelper;
            }
            case "이메일": {
                return emailHelper;
            }
            case "비밀번호":
            case "비밀번호 확인": {
                return passwordHelper;
            }
            default:
                return null;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activityRegisterLl.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private String setPhoneNumber(String phoneNumber) {
        if (!phoneNumber.isEmpty()) {
            String firstNumber = phoneNumber.substring(0, 3);
            String phoneEdit = phoneNumber.substring(3);

            switch (firstNumber) {
                case "010": {
                    phoneEdit = "+8210" + phoneEdit;
                    //phoneEdit = "+1 10" + phoneEdit;
                    break;
                }
                case "011": {
                    phoneEdit = "+8211" + phoneEdit;
                    break;
                }
                case "016": {
                    phoneEdit = "+8216" + phoneEdit;
                    break;
                }
                case "017": {
                    phoneEdit = "+8217" + phoneEdit;
                    break;
                }
                case "018": {
                    phoneEdit = "+8218" + phoneEdit;
                    break;
                }
                case "019": {
                    phoneEdit = "+8219" + phoneEdit;
                    break;
                }
                case "106": {
                    phoneEdit = "+82106" + phoneEdit;
                    break;
                }
            }
            return phoneEdit;
        }
        return null;
    }
}