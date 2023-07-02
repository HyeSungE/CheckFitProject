package org.techtown.MyExerciseApp.Feed;

import static android.content.ContentValues.TAG;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static org.techtown.MyExerciseApp.Exercise.SavePhoto.REQUEST_TAKE_GALLERY;
import static org.techtown.MyExerciseApp.Exercise.SavePhoto.REQUEST_TAKE_PHOTO;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Main.LoginActivity;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.MyClass.PermissionHelper;
import org.techtown.MyExerciseApp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditProfile extends AppCompatActivity {
    private Uri postImageUri;
    private Uri photoURI;
    private Uri uriForRe;
    private Uri profileImageUri;
    private String mCurrentPhotoPath;
    private PermissionHelper permissionHelper;
    private boolean doCamera = false;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    private ImageView edit_profile_close, image_profile;
    private TextView edit_profile_save, edit_profile_image_change,edit_profile_image_re;
    private EditText edit_profile_nickname, edit_profile_description;
    private User currentUser;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;

    private Long mLastClickTime = 0L;

    private String from;
    private Button delete_account_bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Intent intent = getIntent();
        savedInstanceState = intent.getExtras();
        currentUser = (User) savedInstanceState.getSerializable("user");
        from = (String) savedInstanceState.getString("from");

        edit_profile_close = findViewById(R.id.edit_profile_close);
        image_profile = findViewById(R.id.image_profile);
        edit_profile_save = findViewById(R.id.edit_profile_save);
        edit_profile_image_change = findViewById(R.id.edit_profile_image_change);
        edit_profile_image_re = findViewById(R.id.edit_profile_image_re);
        edit_profile_nickname = findViewById(R.id.edit_profile_nickname);
        edit_profile_description = findViewById(R.id.edit_profile_description);
        delete_account_bt = findViewById(R.id.delete_account_bt);
        delete_account_bt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle("! 확인 !")
                        .setMessage("정말로 회원탈퇴를 하십니까 ?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                                if (firebaseUser != null) {
                                    firebaseUser.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(EditProfile.this, "회원 탈퇴에 성공하셨습니다 !", Toast.LENGTH_SHORT).show();
                                                        //프로필 이미지
                                                        FirebaseStorage.getInstance().getReference("profile_image/"+ currentUser.getProfileImage()).delete();
                                                        Intent intent = new Intent(EditProfile.this, LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    } else {

                                                        Toast.makeText(EditProfile.this, "회원탈퇴에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }

                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        });
        FirebaseStorage.getInstance().getReference("profile_image").child(currentUser.getProfileImage()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext()).load(uri).into(image_profile);
                        uriForRe = uri;
                    }
                });
        FirebaseDatabase.getInstance().getReference("USER/"+currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                edit_profile_nickname.setText(user.getNickname());
                edit_profile_description.setText(user.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        edit_profile_image_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext()).load(uriForRe).into(image_profile);
                profileImageUri = uriForRe;
            }
        });
        edit_profile_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditProfile.this.finish();
            }
        });

        edit_profile_image_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    doCamera = false;
                    permissionCheck();
                    if (doCamera) showAlertHowGetPhoto();
                }
            }
        });
        edit_profile_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(EditProfile.this);
                if(SystemClock.elapsedRealtime() - mLastClickTime <= 3000){
                    Toast.makeText(EditProfile.this, "잠시만 기다려 주세요 !", Toast.LENGTH_SHORT).show();
                }
                if(SystemClock.elapsedRealtime() - mLastClickTime > 3000){
                    pd.setMessage("프로필을 업데이트 중입니다.");
                    pd.setCancelable(false);
                    pd.show();
                    Map<String, Object> childUpdate = new HashMap<String, Object>();
                    String changeNickname = edit_profile_nickname.getText().toString();
                    childUpdate.put("/nickname/", edit_profile_nickname.getText().toString());
                    childUpdate.put("/description/", edit_profile_description.getText().toString());
                    new GetUser("nickname",changeNickname).getUidByNickname(new GetUserListener() {
                        @Override
                        public User getUserLoaded(User user) {
                            if(user!=null && !changeNickname.equals(currentUser.getNickname())) {
                                Toast.makeText(getApplicationContext(), "중복된 닉네임이 존재합니다.\n닉네임을 변경해주세요!", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                return null;
                            }
                            else if(user == null || changeNickname.equals(currentUser.getNickname())){
                                if(postImageUri!=null){
                                    profileImageUri = postImageUri;
                                    Log.d(TAG, "photoURI!=null");

                                    String fileExtension = null;
                                    if (profileImageUri.toString().contains(".")) {
                                        fileExtension = profileImageUri.toString().split("\\?")[0].substring(profileImageUri.toString().lastIndexOf("."));
                                    }
                                    String fileName = currentUser.getUid() + fileExtension;
                                    childUpdate.put("/profileImage/", fileName+"");
                                    System.out.println("profileImageUri"+profileImageUri); System.out.println("fileExtension"+fileExtension); System.out.println("fileName"+fileName);
                                    uploadTask = FirebaseStorage.getInstance().getReference("profile_image").child(fileName).putFile(profileImageUri);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                FirebaseDatabase.getInstance().getReference("USER").
                                                        child(currentUser.getUid()).updateChildren(childUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                currentUser.setNickname(changeNickname);
                                                                currentUser.setDescription(edit_profile_description.getText().toString());
                                                                currentUser.setProfileImage(fileName);
                                                                Query query = FirebaseDatabase.getInstance().getReference().child("POST").orderByChild("uid").equalTo(currentUser.getUid());
                                                                query.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if(!snapshot.hasChildren()){
                                                                            Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 없음");
                                                                        }else{
                                                                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                                Post p = dataSnapshot.getValue(Post.class);
                                                                                if(p!=null){
                                                                                    FirebaseDatabase.getInstance().getReference("POST").child(p.getPostId()).child("postNickname").setValue(changeNickname);

                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                        Log.d("getUidByNickname", "onCancelled 데이터없음");
                                                                    }
                                                                });
                                                                try {
                                                                    TimeUnit.SECONDS.sleep(2);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                pd.dismiss();
                                                                Toast.makeText(EditProfile.this, "업데이트 성공", Toast.LENGTH_SHORT).show();

                                                            }
                                                        });
                                            }
                                        }
                                    });

                                    return null;
                                }
                                else{
                                    FirebaseDatabase.getInstance().getReference("USER").child(currentUser.getUid()).updateChildren(childUpdate);
                                    currentUser.setNickname(changeNickname);
                                    currentUser.setDescription(edit_profile_description.getText().toString());
                                    Query query = FirebaseDatabase.getInstance().getReference().child("POST").orderByChild("uid").equalTo(currentUser.getUid());
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(!snapshot.hasChildren()){
                                                Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 없음");
                                            }else{
                                                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    Post p = dataSnapshot.getValue(Post.class);
                                                    if(p!=null){
                                                        FirebaseDatabase.getInstance().getReference("POST").child(p.getPostId()).child("postNickname").setValue(changeNickname);

                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d("getUidByNickname", "onCancelled 데이터없음");
                                        }
                                    });
                                    try {
                                        TimeUnit.SECONDS.sleep(2);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    pd.dismiss();
                                    Toast.makeText(EditProfile.this, "업데이트 성공", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                            }
                            return null;
                        }
                    });
                }
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        cropImage(photoURI);
                    }
                    break;
                }

                case REQUEST_TAKE_GALLERY: {
                    if (resultCode == RESULT_OK) {
                        Uri uri = data.getData();
                        cropImage(uri);
                    }
                    break;
                }

                case CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        postImageUri = result.getUri();
                        Glide.with(this).load(postImageUri).into(image_profile);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "프로필 이미지 업로드 중 문제 발생", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "프로필 이미지 업로드 중 문제 발생", e);
        }

    }

    private void cropImage(Uri uri) {
        CropImage.ActivityBuilder activity = CropImage.activity(uri);
        activity.setGuidelines(CropImageView.Guidelines.ON);
        activity.setCropShape(CropImageView.CropShape.RECTANGLE);
        activity.setAspectRatio(1, 1);
        activity.start(this);
    }

    private void showAlertHowGetPhoto() {
        AlertDialog.Builder howGetPhotoAlert_builder = new AlertDialog.Builder(this);
        howGetPhotoAlert_builder.setMessage("사진을 가져옵니다.")
                .setPositiveButton("갤러리", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //갤러리에서 사진 가져오기
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        //intent.putExtra("crop", true);
                        startActivityForResult(intent, REQUEST_TAKE_GALLERY);
                    }
                })
                .setNegativeButton("카메라", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        captureCamera();
                    }
                });

        AlertDialog alertDialog_howGetPhoto = howGetPhotoAlert_builder.create();

        alertDialog_howGetPhoto.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_howGetPhoto.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                alertDialog_howGetPhoto.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_howGetPhoto.show();
    }

    private void captureCamera() {
        Context mContext = getApplicationContext();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 인텐트를 처리 할 카메라 액티비티가 있는지 확인
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // 촬영한 사진을 저장할 파일 생성
            File photoFile = null;
            try {
                //임시로 사용할 파일이므로 경로는 캐시폴더로
                File tempDir = mContext.getCacheDir();
                //임시촬영파일 세팅
                String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String imageFileName = "Capture_" + timeStamp + "_"; //ex) Capture_20201206_
                File tempImage = File.createTempFile(
                        imageFileName,  /* 파일이름 */
                        ".jpg",         /* 파일형식 */
                        tempDir      /* 경로 */
                );
                // ACTION_VIEW 인텐트를 사용할 경로 (임시파일의 경로)
                mCurrentPhotoPath = tempImage.getAbsolutePath();
                photoFile = tempImage;
                tempImage.delete();
            } catch (IOException e) {
                //에러 로그는 이렇게 관리하는 편이 좋다.
                Log.w(TAG, "파일 생성 에러!", e);
            }
            //파일이 정상적으로 생성되었다면 계속 진행
            if (photoFile != null) {
                //Uri 가져오기
                photoURI = FileProvider.getUriForFile(mContext,
                        mContext.getPackageName() + ".fileprovider",
                        photoFile);
                //인텐트에 Uri담기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                photoFile.delete();
                Log.e(TAG, "go Intent! : " + takePictureIntent);
                //인텐트 실행
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    private void permissionCheck() {  // 권한 체크
        permissionHelper = new PermissionHelper(this, getApplicationContext());// PermissionSupport.java 클래스 객체 생성
        if (!permissionHelper.checkPermission()) {// 권한 체크 후 리턴이 false로 들어오면
            permissionHelper.requestPermission();//권한 요청
        } else {
            doCamera = true;
        }
    }

    @Override   // Request Permission에 대한 결과 값 받아와
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionHelper.permissionResult(requestCode, permissions, grantResults)) {  //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
            //System.out.println("1");//permissionHelper.requestPermission();   // 다시 permission 요청//showRetryPermissionsDialog();
            Toast.makeText(getApplicationContext(), "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}