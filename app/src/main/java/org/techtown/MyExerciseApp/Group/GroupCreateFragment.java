package org.techtown.MyExerciseApp.Group;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static org.techtown.MyExerciseApp.Exercise.SavePhoto.REQUEST_TAKE_GALLERY;
import static org.techtown.MyExerciseApp.Exercise.SavePhoto.REQUEST_TAKE_PHOTO;
import static org.techtown.MyExerciseApp.Exercise.SavePhoto.tag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.PermissionHelper;
import org.techtown.MyExerciseApp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class GroupCreateFragment extends Fragment {
    //View
    private ImageView creation_group_image;
    private Button creation_group_image_add,group_creation_create,group_creation_cancle;
    private ImageButton creation_group_image_re;
    private EditText creation_group_name,creation_group_size,group_creation_description;

    //
    private String mCurrentPhotoPath;
    private Uri uriForRe;
    private Uri photoUri;
    private Uri groupImageUri;
    private PermissionHelper permissionHelper;
    private boolean doCamera = false;
    public GroupCreateFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_group_create, container, false);
        //ImageView
        creation_group_image = rootView.findViewById(R.id.creation_group_image);
        //Button
        creation_group_image_add = rootView.findViewById(R.id.creation_group_image_add);
        group_creation_create = rootView.findViewById(R.id.group_creation_create);
        group_creation_cancle = rootView.findViewById(R.id.group_creation_cancle);
        //ImageButton
        creation_group_image_re = rootView.findViewById(R.id.creation_group_image_re);
        //EditText
        creation_group_name = rootView.findViewById(R.id.creation_group_name);
        creation_group_size = rootView.findViewById(R.id.creation_group_size);
        group_creation_description = rootView.findViewById(R.id.group_creation_description);

        creation_group_image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doCamera = false;
                permissionCheck();
                if (doCamera) showAlertHowGetPhoto();
                else {
                    Toast.makeText(requireActivity().getApplicationContext(), "설정에서 카메라 권한을 허용해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        group_creation_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(creation_group_name.getText().toString().equals("")){
                    Toast.makeText(requireActivity().getApplicationContext(), "그룹의 이름을 만들어 주세요 !", Toast.LENGTH_SHORT).show();
                }else if(creation_group_size.getText().toString().equals("")){
                    Toast.makeText(requireActivity().getApplicationContext(), "그룹의 인원 수를 설정해 주세요 !", Toast.LENGTH_SHORT).show();
                }else if( group_creation_description.getText().toString().equals("")){
                    Toast.makeText(requireActivity().getApplicationContext(), "그룹 설명을 작성해 주세요 !", Toast.LENGTH_SHORT).show();
                }else if(!creation_group_size.getText().toString().equals("")&&Integer.parseInt(creation_group_size.getText().toString()) < 2){
                    Toast.makeText(requireActivity().getApplicationContext(), "그룹의 인원은 최소 2명이어야 합니다 !", Toast.LENGTH_SHORT).show();
                }

                else{
                    final ProgressDialog pd = new ProgressDialog(requireActivity());
                    pd.setMessage("잠시만 기다려 주세요");
                    pd.show();
                    //photoUri
                    String key = FirebaseDatabase.getInstance().getReference("GROUP").push().getKey();
                    Group group = new Group(
                            key,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            creation_group_name.getText().toString(),
                            group_creation_description.getText().toString(),
                            new GetToday().getTodayTime(),
                            Integer.parseInt(creation_group_size.getText().toString())
                    );
                    group.setGroupImageName("group_default_image.png");
                    if(groupImageUri!=null){
                        //photoUri
                        Log.d(TAG, "groupImageUri!=null");
                        String fileExtension = null;
                        if (groupImageUri.toString().contains(".")) {
                            fileExtension = groupImageUri.toString().split("\\?")[0].substring(groupImageUri.toString().lastIndexOf("."));
                        }

                        String fileName = key+ fileExtension;
                        group.setGroupImageName(fileName);
                        FirebaseStorage.getInstance().getReference("group_image").child(fileName).putFile(groupImageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance().getReference("GROUP").child(key).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    pd.dismiss();
                                                    Toast.makeText(requireActivity().getApplicationContext(), "그룹 등록을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                                    requireActivity().getSupportFragmentManager().beginTransaction().remove(GroupCreateFragment.this).commit();
                                                    requireActivity().getSupportFragmentManager().popBackStack();
                                                }
                                            });
                                        }
                                    }
                                });
                    }else{
                        FirebaseDatabase.getInstance().getReference("GROUP").child(key).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(requireActivity().getApplicationContext(), "그룹 등록을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().beginTransaction().remove(GroupCreateFragment.this).commit();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                    }
                }

            }
        });
        //뒤로가기
        group_creation_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction().remove(GroupCreateFragment.this).commit();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        creation_group_image_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //@drawable/exercise_no_image_placeholder
                Glide.with(getContext()).load(R.drawable.exercise_no_image_placeholder).into(creation_group_image);
                groupImageUri=null;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        cropImage(photoUri);
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
                        groupImageUri = result.getUri();
                        Glide.with(this).load(groupImageUri).into(creation_group_image);
                    } else {
                        Toast.makeText(requireActivity().getApplicationContext(),
                                "프로필 이미지 업로드 중 문제 발생", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "프로필 이미지 업로드 중 문제 발생", e);
        }
    }
    @Override   // Request Permission에 대한 결과 값 받아와
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionHelper.permissionResult(requestCode, permissions, grantResults)) {  //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
            Toast.makeText(requireActivity().getApplicationContext(), "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireActivity().getApplicationContext(), "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void cropImage(Uri uri) {
        CropImage.ActivityBuilder activity = CropImage.activity(uri);
        activity.setGuidelines(CropImageView.Guidelines.ON);
        activity.setCropShape(CropImageView.CropShape.RECTANGLE);
        activity.setAspectRatio(1, 1);
        activity.start(getContext(),this);
    }
    private void captureCamera() {
        Context mContext =getContext();
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
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        mContext.getPackageName() + ".fileprovider",
                        photoFile);
                //인텐트에 Uri담기
                photoUri =photoURI;
                System.out.println("photoUri++"+photoUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.e(TAG, "go Intent! : " + takePictureIntent);
                photoFile.delete();
                //인텐트 실행
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }
    private void permissionCheck() {  // 권한 체크
        permissionHelper = new PermissionHelper(requireActivity(), requireActivity().getApplicationContext());// PermissionSupport.java 클래스 객체 생성
        if (!permissionHelper.checkPermission()) {// 권한 체크 후 리턴이 false로 들어오면
            permissionHelper.requestPermission();//권한 요청
        } else {
            doCamera = true;
        }
    }
    private void showAlertHowGetPhoto() {
        AlertDialog.Builder howGetPhotoAlert_builder = new AlertDialog.Builder(requireActivity());
        howGetPhotoAlert_builder.setMessage("사진을 가져옵니다.")
                .setPositiveButton("갤러리", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //갤러리에서 사진 가져오기
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_TAKE_GALLERY);
                    }
                })
                .setNegativeButton("카메라", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean ok = false;
                        if(ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED) {
                            Log.d(tag, "권한 설정 완료");
                            ok = true;
                        } else {
                            Log.d(tag, "권한 설정 요청");
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                                    Manifest.permission.CAMERA
                            },  10);
                        }
                        if(ok) {
                            captureCamera();
                        }
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
}