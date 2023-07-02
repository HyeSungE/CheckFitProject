package org.techtown.MyExerciseApp.Exercise;


import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.techtown.MyExerciseApp.Data.Exercise.CompleteExerciseItem;
import org.techtown.MyExerciseApp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SavePhoto extends Fragment {
    public static final String tag = SavePhoto.class.getSimpleName();

    private LinearLayout savePhotoLl;
    private ImageButton savePhotoReturnArrow, savePhotoCamera, savePhotoForwardArrow, savePhotoBackArrow;
    private Button savePhotoGallerySave,savePhotoSharePhoto;
    private TextView savePhotoFrameName, savePhotoRound, savePhotoExerciseTime, savePhotoSet, savePhotoWeight;
    private ArrayList<RelativeLayout> InFrameArrayList;
    private ArrayList<CompleteExerciseItem> completeExerciseItemArrayList;
    private String[] totalInfo;
    private ImageView savePhotoExerciseSmile,savePhotoBackgroundImage;
    private RelativeLayout savePhotoFrameLayout;
    private CardView  savePhotoFrameCard;
    private ScrollView savePhotoMainScrollView;

    public Bitmap inputBitmap, outputBitmap;
    public ImageView ivCapture;

    private final int[] savePhotoFrameLayoutArray = {
            R.layout.save_photo_frame_1,
            R.layout.save_photo_frame_2
    };

    private final String[] savePhotoFrameNameArray = {
            "Basic Style",
            "Nike Running Club Style"
    };
    private final FrameLayout.LayoutParams savePhotoFrameLayoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT

    );

    private final TableRow.LayoutParams params = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT
    );
    private final TableRow.LayoutParams paramsTr = new TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.MATCH_PARENT,
            1f
    );

    //tabke a picture
    public final static int REQUEST_TAKE_GALLERY = 0;
    public final static int REQUEST_TAKE_PHOTO = 1;
    public final static int MULTIPLE_PERMISSIONS = 1023;

    String mCurrentPhotoPath;



    private LayoutInflater layoutInflater;
    private int currentIndex;


    public void onStart() {super.onStart();Log.d(tag, "onStart: ");}
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {super.onCreate(savedInstanceState);Log.d(tag, "onCreate: ");}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getArguments();
        completeExerciseItemArrayList = (ArrayList<CompleteExerciseItem>) savedInstanceState.getSerializable("completeExerciseItemArrayListBundle");
        totalInfo = (String[]) savedInstanceState.getSerializable("totalInfoBundle");
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_save_photo, container, false);
        layoutInflater = LayoutInflater.from(rootView.getContext());
        init(rootView);
        setOnClickListener();

        return rootView;
    }

    private void init(ViewGroup rootView) {
        savePhotoReturnArrow = rootView.findViewById(R.id.save_photo_return_arrow);
        savePhotoCamera = rootView.findViewById(R.id.save_photo_camera);
        savePhotoForwardArrow = rootView.findViewById(R.id.save_photo_forward_arrow);
        savePhotoBackArrow = rootView.findViewById(R.id.save_photo_back_arrow);
        savePhotoGallerySave = rootView.findViewById(R.id.save_photo_gallery_save);
        savePhotoSharePhoto = rootView.findViewById(R.id.save_photo_share_photo);
        savePhotoFrameName = rootView.findViewById(R.id.save_photo_frame_name);
        savePhotoFrameLayout = (RelativeLayout) rootView.findViewById(R.id.save_photo_frame_layout);
        savePhotoMainScrollView = (ScrollView) rootView.findViewById(R.id.save_photo_main_scroll_view);

        currentIndex = 0;
        InFrameArrayList = new ArrayList<>();
        setFrameLayout(savePhotoFrameLayoutArray);
        setVisiblePhotoFrame(currentIndex);
        savePhotoFrameName.setText(savePhotoFrameNameArray[currentIndex]);
    }

    private void setOnClickListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.save_photo_return_arrow:{
                        //사진 저장 다이어로그 끄기
                        requireActivity().getSupportFragmentManager().beginTransaction().remove(SavePhoto.this).commit();
                        requireActivity().getSupportFragmentManager().popBackStack();
                        break;
                }
                    case R.id.save_photo_camera:{
                        //카메라 갤러리 선택 다이어로그 출현
                        showAlertHowGetPhoto();
                        break;
                }
                    case R.id.save_photo_forward_arrow:{
                        //프레임 앞으로 이동
                        currentIndex += 1;
                        if (currentIndex >= savePhotoFrameLayoutArray.length) {
                            currentIndex = savePhotoFrameLayoutArray.length - 1;
                        }
                        setVisiblePhotoFrame(currentIndex);
                        savePhotoFrameName.setText(savePhotoFrameNameArray[currentIndex]);
                        break;
                }
                    case R.id.save_photo_back_arrow:{
                        //프레임 뒤로 이동
                        currentIndex -= 1;
                        if (currentIndex < 0) {
                            currentIndex = 0;
                        }
                        Log.d(tag, "save_photo_back_arrow: " + currentIndex);
                        setVisiblePhotoFrame(currentIndex);
                        savePhotoFrameName.setText(savePhotoFrameNameArray[currentIndex]);
                        break;
                }
                    case R.id.save_photo_gallery_save:{
                        //현재 보여지는 프레임 스크린샷 기능을 이용해 저장
                        View view = null;
                        Bitmap screenshot = null;


                        Log.d(TAG, savePhotoMainScrollView.getChildAt(0).getClass().getSimpleName());
                        view = savePhotoMainScrollView;
                        view.setDrawingCacheEnabled(true);
                        switch (currentIndex) {
                            case 0: {
                                screenshot = Bitmap.createBitmap(
                                        ((ScrollView) view).getChildAt(0).getWidth()
                                        , ((ScrollView) view).getChildAt(0).getHeight(),
                                        Bitmap.Config.ARGB_8888);
                                break;
                            }
                            case 1: {
                                screenshot = Bitmap.createBitmap(
                                        ((ScrollView) view).getWidth()
                                        , ((ScrollView) view).getHeight(),
                                        Bitmap.Config.ARGB_8888);
                                break;
                            }
                        }

                        Canvas canvas = new Canvas(screenshot);
                        Drawable bgDrawable = view.getBackground();
                        if (bgDrawable != null) bgDrawable.draw(canvas);
                        else canvas.drawColor(Color.WHITE);
                        view.draw(canvas);
                        String fileName = "MyExerciseRecord" + getToday() + ".jpeg";
                        String folderName = "MyExerciseRecord";
                        String dirPath = Environment.getExternalStorageDirectory() + "/pictures/" + folderName;
                        File dir = new File(dirPath);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File file = new File(dir, fileName);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                            showAlertSimpleMessage(getContext(), "성공", "스크린샷에 성공했습니다.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            showAlertSimpleMessage(getContext(), "실패", "스크린샷에 실패했습니다.");
                        }
                        view.setDrawingCacheEnabled(false);
                        if (screenshot != null)
                            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        break;
                }
                    case R.id.save_photo_share_photo:{
                        break;
                    }
                }
            }
        };
        savePhotoReturnArrow.setOnClickListener(clickListener);
        savePhotoCamera.setOnClickListener(clickListener);
        savePhotoForwardArrow.setOnClickListener(clickListener);
        savePhotoBackArrow.setOnClickListener(clickListener);
        savePhotoGallerySave.setOnClickListener(clickListener);
    }


    private void setFrameLayout(int[] savePhotoFrameLayoutArray) {
        if (savePhotoFrameLayout.getChildCount() > 0) savePhotoFrameLayout.removeAllViews();
        for (int i = 0; i < savePhotoFrameLayoutArray.length; i++) {
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(savePhotoFrameLayoutArray[i], null, false);
            RelativeLayout spfRL = (RelativeLayout) viewGroup.findViewById(R.id.spf_rl);
            savePhotoRound = viewGroup.findViewById(R.id.save_photo_round);
            savePhotoExerciseTime = viewGroup.findViewById(R.id.save_photo_exercise_time);
            savePhotoSet = viewGroup.findViewById(R.id.save_photo_set);
            savePhotoWeight = viewGroup.findViewById(R.id.save_photo_weight);
            savePhotoExerciseSmile = viewGroup.findViewById(R.id.save_photo_exercise_smile);
            savePhotoFrameCard = viewGroup.findViewById(R.id.spf_card);
            savePhotoBackgroundImage = (ImageView) viewGroup.findViewById(R.id.save_photo_backgroung_image);
            // - 1 ROUND - 00:00:06 2 종목  80.0 kg/80.0 kg android.resource://org.techtown.MyExerciseApp/2131230998 android
            String round = totalInfo[0];
            String time = totalInfo[1];
            String set = totalInfo[2].split(" ")[0];
            System.out.println("totalInfo++"+totalInfo[2]);
            String weight = totalInfo[2].split(" ")[2].split("kg")[0].trim();
            Glide.with(this).load(totalInfo[3]).into(savePhotoExerciseSmile);
            if (i == 1) {
                set += " SET";
                weight += " KG";
            } // 두번째 프레임
            savePhotoRound.setText(round);
            savePhotoExerciseTime.setText(time);
            savePhotoSet.setText(set);
            savePhotoWeight.setText(weight);
            //첫번째프레임 - 프레임 레이아웃에 표시될 레이아웃을 만들고 arrayList에 추가

            savePhotoLl = viewGroup.findViewById(R.id.spf1_ll);
            if (savePhotoLl != null) {
                for(CompleteExerciseItem completeExerciseItem : completeExerciseItemArrayList) {
                    ViewGroup compExerciseListItem = (ViewGroup) layoutInflater.inflate(R.layout.complete_exercise_list_item, null, false);
                    Context mContext = compExerciseListItem.getContext();
                    TextView compExerciseName =
                            (TextView) compExerciseListItem.findViewById(R.id.comp_exercise_name);
                    TextView compExerciseTotalSet =
                            (TextView) compExerciseListItem.findViewById(R.id.comp_exercise_total_set);
                    TextView compExerciseWeight =
                            (TextView) compExerciseListItem.findViewById(R.id.comp_exercise_weight);
                    TableLayout compTable =
                            (TableLayout) compExerciseListItem.findViewById(R.id.comp_table_layout);
                    TableRow compTableHeader =
                            (TableRow) compTable.findViewById(R.id.comp_exercise_list_header_row);
                    compExerciseName.setText(completeExerciseItem.getCompExerciseName());
                    compExerciseTotalSet.setText(completeExerciseItem.getCompExerciseTotalSet());
                    compExerciseWeight.setText(completeExerciseItem.getCompExerciseWeight());

                    ArrayList<String> getHeader = completeExerciseItem.getHeader();
                    int len = getHeader.size();
                    if(len==3){
                        ((TextView)compTableHeader.getChildAt(1)).setText(getHeader.get(1));
                        ((TextView)compTableHeader.getChildAt(2)).setText(getHeader.get(2));
                        compTableHeader.removeViewAt(3);
                    }else if(len==4){
                        ((TextView)compTableHeader.getChildAt(1)).setText(getHeader.get(1));
                        ((TextView)compTableHeader.getChildAt(2)).setText(getHeader.get(2));
                    }

                    ArrayList<String> getBody = completeExerciseItem.getBody();
                    if(getBody.size()==1){
                        TableRow tableRow = new TableRow(mContext);
                        tableRow.setLayoutParams(params);
                        TextView textView = new TextView(mContext);
                        textView.setLayoutParams(paramsTr);
                        textView.setGravity(Gravity.CENTER);
                        textView.setText(getBody.get(0));
                        compTable.addView(tableRow);

                    }
                    else{
                        for(int row=1; row<getBody.size(); row++){
                            String recordRow = getBody.get(row);
                            TableRow tableRow = new TableRow(mContext);
                            tableRow.setLayoutParams(params);
                            for(int row_i=0; row_i<len; row_i++){
                                TextView textView = new TextView(mContext);
                                textView.setLayoutParams(paramsTr);
                                textView.setGravity(Gravity.CENTER);
                                textView.setText(recordRow.split("/")[row_i]);
                                tableRow.addView(textView);
                            }
                            compTable.addView(tableRow);
                        }

                    }
                    if (compTable.getParent() != null)
                        ((ViewGroup) compTable.getParent()).removeView(compTable);
                    savePhotoLl.addView(compTable);

                }
            }
            savePhotoFrameLayout.addView(viewGroup);
            InFrameArrayList.add(spfRL);
        }
    }

    private void setVisiblePhotoFrame(int currentIndex) {
        for (int i = 0; i < InFrameArrayList.size(); i++) {
            if (i == currentIndex) {
                Log.d(tag, "getFrameLayout: " + InFrameArrayList.get(i));
                InFrameArrayList.get(i).setVisibility(View.VISIBLE);
                if (i == 0) {
                    savePhotoCamera.setVisibility(View.INVISIBLE);
                    savePhotoFrameLayoutParams.gravity = Gravity.NO_GRAVITY;
                    savePhotoFrameLayout.setLayoutParams(savePhotoFrameLayoutParams);
                } else {
                    savePhotoCamera.setVisibility(View.VISIBLE);
                    savePhotoFrameLayoutParams.gravity = Gravity.CENTER;
                    savePhotoFrameLayout.setLayoutParams(savePhotoFrameLayoutParams);
                }
            } else {
                Log.d(tag, "getFrameLayout: " + InFrameArrayList.get(i));
                InFrameArrayList.get(i).setVisibility(View.GONE);
            }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context mContext = getContext();
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        Glide.with(this).load(mCurrentPhotoPath).into(savePhotoBackgroundImage);
                    }break;
                }

                case REQUEST_TAKE_GALLERY:{
                    if (resultCode == RESULT_OK) {
                       Uri imageUri = data.getData();
                        Glide.with(this).asBitmap().load(imageUri).into(savePhotoBackgroundImage);
                    }break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "onActivityResult Error !", e);
        }

    }

    private void captureCamera() {
        Context mContext = getContext();
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.e(TAG, "go Intent! : " + takePictureIntent);
                //인텐트 실행
                ((Activity) mContext).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }


    private String getToday() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }

    //간단한 메시지를 보여주는 다이얼로그
    private void showAlertSimpleMessage(Context context, String title, String content) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        builder.setTitle(title).setMessage(content);

        android.app.AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }





}
