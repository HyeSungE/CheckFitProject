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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.MyExerciseApp.Adapter.Feed.PostImageAdapter;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Interface.PostClickEventListener;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.MyClass.PermissionHelper;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PostActivity extends AppCompatActivity {

    private Uri postImageUri;
    private Uri photoURI;
    private final String urlOk = "";
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private StorageReference storageRef;

    private ImageView post_register_close;
    private ImageView post_register_main_image;
    private TextView post_register_post, post_total_image_count_tv;
    private EditText post_register_content;

    private Button post_register_add_image_button;
    private RecyclerView post_register_add_image_recycler;
    private PostImageAdapter postImageAdapter;
    private ArrayList<Uri> uriList;
    private String mCurrentPhotoPath;
    // 클래스 선언
    private PermissionHelper permissionHelper;
    private boolean doCamera = false;
    private Bundle bundle;

    private BackPressHandler backPressHandler = new BackPressHandler(this);

    @Override
    public void onBackPressed() {
        if(backPressHandler.onBackPressed()){
            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setMessage("홈으로 이동합니다.").setTitle("안내")
                    .setPositiveButton("이동", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(PostActivity.this, MainActivity.class);
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
        setContentView(R.layout.activity_post);
        bundle = getIntent().getExtras();


        post_register_close = findViewById(R.id.post_register_close);
        post_register_main_image = findViewById(R.id.post_register_main_image);
        post_register_main_image.setTag(R.drawable.ic_baseline_add_to_photos_24);

        post_register_post = findViewById(R.id.post_register_post);
        post_register_content = findViewById(R.id.post_register_content);
        post_total_image_count_tv = findViewById(R.id.post_total_image_count_tv);

        post_register_add_image_button = findViewById(R.id.post_register_add_image_button);
        post_register_add_image_recycler = findViewById(R.id.post_register_add_image_recycler);

        post_register_add_image_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(PostActivity.this, LinearLayoutManager.HORIZONTAL, false);
        post_register_add_image_recycler.setLayoutManager(mLayoutManager);
        uriList = new ArrayList<>();
        postImageAdapter = new PostImageAdapter(getApplicationContext(), uriList);
        postImageAdapter.setItem(uriList);
        post_register_add_image_recycler.setAdapter(postImageAdapter);
        post_register_add_image_recycler.scrollToPosition(postImageAdapter.getItemCount() - 1);
        postImageAdapter.setPostClickEventListener(new PostClickEventListener() {
            @Override
            public void callImageCountTv() {
                System.out.println("postImageAdapter.getItemCount()" + postImageAdapter.getItemCount());
                post_total_image_count_tv.setText("총 이미지 ( " + postImageAdapter.getItemCount() + " / 5)\n(<== 양 옆으로 슬라이드 ==>)");
            }

            @Override
            public void callSetMainImage(Uri uri) {
                if (uri == null) {
                    post_register_main_image.setImageResource(R.drawable.ic_baseline_add_to_photos_24);
                } else {
                    Glide.with(PostActivity.this).load(uri).into(post_register_main_image);
                }

            }
        });
        post_total_image_count_tv.setText("총 이미지 ( " + postImageAdapter.getItemCount() + " / 5)\n<== 양 옆으로 슬라이드 ==>");

        post_register_add_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postImageAdapter.getItemCount() > 5) {
                    ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();
                    showAlertSimpleMessage.show(PostActivity.this, "알림", "사진은 최대 5장입니다.\n사진을 삭제하시고 다시 클릭해주세요!");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        doCamera = false;
                        permissionCheck();
                        if (doCamera) showAlertHowGetPhoto();
                    }
                }
            }
        });

        storageRef = FirebaseStorage.getInstance().getReference("POST");

        post_register_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post_register_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(PostActivity.this);
                pd.setMessage("잠시만 기다려 주세요");
                pd.show();
                if (postImageAdapter.getItemCount() < 1) {
                    ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();
                    showAlertSimpleMessage.show(PostActivity.this, "알림",
                            "최소 1장의 사진을 넣어주세요 !");
                    pd.dismiss();
                } else {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    new GetUser("uid", uid).getUserByUid(new GetUserListener() {
                        @Override
                        public User getUserLoaded(User user) {
                            uploadImage_10(user);
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            pd.dismiss();
                            Toast.makeText(PostActivity.this, "게시글을 성공적으로 올렸습니다.\n [피드 -> 내 피드] 에서 확인가능합니다.", Toast.LENGTH_SHORT).show();
                            return user;
                        }
                    });
                }
            }
        });
    }

//    private String getFileExtension(Uri uri){
//        ContentResolver cR = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        return mime.getExtensionFromMimeType(cR.getType(uri));
//    }

    private void uploadImage_10(User user) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("운동기록을 널리 알리는 중");
        pd.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        //final StorageReference fileReference = storageRef.child(user.getUid());
        String postId = user.getUid() + "_" + new GetToday().getTodayTimeOnlyNumber();
        if (postImageAdapter.getItemCount() > 0) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("postId", postId);
            hashMap.put("uid", user.getUid());
            hashMap.put("postNickname", user.getNickname());
            hashMap.put("content", post_register_content.getText().toString());
            hashMap.put("creationDate", new GetToday().getTodayTime());
            //이미지를 스토리지에 업로드 ++ 이미지의 이름을 파이어베이스에 업데이트
            ArrayList<String> imageNameList = new ArrayList<String>();
            int i = 1;
            for (Uri uri : postImageAdapter.getUriList()) {
                String fileExtension = null;
                if (uri.toString().contains(".")) {
                    fileExtension = uri.toString().substring(uri.toString().lastIndexOf("."));
                }
                uploadTask = storageRef.child(postId + "_" + i+fileExtension).putFile(uri);
                imageNameList.add(postId + "_" + i+fileExtension);
                i += 1;
                hashMap.put("postImage", imageNameList);
            }
            //루틴 스트링 저장
            hashMap.put("routine", bundle.getString("routine"));
            reference.child(postId).setValue(hashMap);
            pd.dismiss();
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }
    private Drawable getDrawableFromUri(Uri uri) {
        try {
            return Drawable.createFromStream(getContentResolver().openInputStream(uri),
                    uri.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        //Uri uri = data.getData();
                        System.out.println("REQUEST_TAKE_PHOTO" + photoURI);
                        //Glide.with(this).load(mCurrentPhotoPath).into(savePhotoBackgroundImage);
                        cropImage(photoURI);
                    }
                    break;
                }

                case REQUEST_TAKE_GALLERY: {
                    if (resultCode == RESULT_OK) {
                        Uri uri = data.getData();
                        //Glide.with(this).asBitmap().load(imageUri).into(savePhotoBackgroundImage);
                        cropImage(uri);
                    }
                    break;
                }

                case CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        postImageUri = result.getUri();
                        postImageAdapter.addItem(postImageUri);

                    } else {
                        Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "onActivityResult Error !", e);
        }

    }

    private void cropImage(Uri uri) {
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1, 1)
                //사각형 모양으로 자른다
                .start(PostActivity.this);
    }

    private void showAlertHowGetPhoto() {
        AlertDialog.Builder howGetPhotoAlert_builder = new AlertDialog.Builder(PostActivity.this);
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
        permissionHelper = new PermissionHelper(this, this);// PermissionSupport.java 클래스 객체 생성
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
            Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
