package org.techtown.MyExerciseApp.Main;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Exercise.ExerciseListHomeFragment;
import org.techtown.MyExerciseApp.Feed.FeedHomeFragment;
import org.techtown.MyExerciseApp.Group.GroupHomeFragment;
import org.techtown.MyExerciseApp.Home.HomeFragment;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Interface.SendEventListener;
import org.techtown.MyExerciseApp.MyClass.BackPressHandler;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.MyPage.MypageHomeFragment;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SendEventListener, GetUserListener {

    private Fragment homeFragment,exerciseHomeFragment,feedHomeFragment,grouphomeFragment,mypagehomeFragment;
    private AppDatabase appDatabase = null;
    private FirebaseAuth auth; private DatabaseReference reference; FirebaseUser user;
    private FirebaseStorage storage;
    private MaterialToolbar topAppBar;
    private ShowAlertSimpleMessage showAlertSimpleMessage;
    private User currentUser;
    private String str = "";
    private ActionMenuItemView logoutActionMenuItem;
    private Long mLastClickTime = 0L;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar,menu);
       return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private BackPressHandler backPressHandler = new BackPressHandler(this);
     public interface onBackPressedListener{
         void onBackPressed();
     }

    @Override
    public void onBackPressed() {

        for(Fragment fragment : getSupportFragmentManager().getFragments()){
            if(fragment instanceof onBackPressedListener){
              ((onBackPressedListener) fragment).onBackPressed();
            }
        }

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




    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAlertSimpleMessage = new ShowAlertSimpleMessage();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        createNotificationChannel();
        if(user == null){
            showAlertSimpleMessage.show(MainActivity.this,"오류","로그인 후 이용해 주세요\n로그인으로 이동합니다!");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

       // loadingMyDB();
       appDatabase = AppDatabase.getDBInstance(this.getApplicationContext());
        setContentView(R.layout.activity_main);homeFragment = new HomeFragment();
        exerciseHomeFragment = new ExerciseListHomeFragment();
        //
        BottomNavigationView bottomNavigationCircles = findViewById(R.id.bottom_navigation_bar);
        currentUser = new GetUser("uid",user.getUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                feedHomeFragment = new FeedHomeFragment(); feedHomeFragment.setArguments(bundle);
                grouphomeFragment = new GroupHomeFragment(); grouphomeFragment.setArguments(bundle);
                mypagehomeFragment = new MypageHomeFragment();
                return user;
            }
        });

        topAppBar = (MaterialToolbar) findViewById(R.id.topAppBar);
        topAppBar.setVisibility(View.VISIBLE);
        logoutActionMenuItem = topAppBar.findViewById(R.id.top_app_bar_logout);
        logoutActionMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckLogout();
            }
        });
        Log.d("topAppbar",topAppBar.findViewById(R.id.top_app_bar_logout).getClass()+"");

        bottomNavigationCircles.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(SystemClock.elapsedRealtime() - mLastClickTime > 3000){
                    switch (item.getItemId()){
                        case R.id.home:
                            /*getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,homeFragment).commit();
                            return true;*/
                            topAppBar.setVisibility(View.VISIBLE);
                            return replaceFragment(R.id.tab_content_view,homeFragment);

                        case R.id.exerciseHome:
                           /* getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,exerciseHomeFragment).commit();
                            return true;*/
                            topAppBar.setVisibility(View.VISIBLE);
                            return replaceFragment(R.id.tab_content_view,exerciseHomeFragment);
                        case R.id.feedHome:
                            /*getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,feedHomeFragment).commit();
                            return true;*/
                            topAppBar.setVisibility(View.VISIBLE);
                            return replaceFragment(R.id.tab_content_view,feedHomeFragment);
                        case R.id.group_home_tab:
                            /*getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,grouphomeFragment).commit();
                            return true*/;
                            topAppBar.setVisibility(View.VISIBLE);
                            return replaceFragment(R.id.tab_content_view,grouphomeFragment);
                        case R.id.mypageHome:
                            /*getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,mypagehomeFragment).commit();
                            return true*/;
                            topAppBar.setVisibility(View.VISIBLE);
                            return replaceFragment(R.id.tab_content_view,mypagehomeFragment);
                    }
                }

                mLastClickTime = SystemClock.elapsedRealtime();
                return false;
            }
        });
        replaceFragment(R.id.tab_content_view,homeFragment);
        System.out.println("MainActivity 실행");
    }

    public boolean replaceFragment(int viewId,Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId,fragment).commit();
        return true;
    }



    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    @Override
    public void sendExerciseList(ArrayList<Exercise> exerciseArrayList) {

    }

    @Override
    public void sendExerciseListToRoutine(ArrayList<Exercise> exerciseArrayList) {

    }

    @Override
    public void sendRoutine(List<RoutineInfoListItem> routineInfoListItemArrayList) {

    }


    @Override
    public User getUserLoaded(User user) {
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "restTimer";
            String description = "restTimerSuccessNotification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("rest", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showCheckLogout() {
        AlertDialog.Builder checkLogoutAlert_builder = new AlertDialog.Builder(MainActivity.this);
        checkLogoutAlert_builder.setTitle("로그아웃").setMessage("로그아웃 후, 메인으로 이동합니다.\n로그아웃 하시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class).
                                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog_checkLogout = checkLogoutAlert_builder.create();

        alertDialog_checkLogout.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_checkLogout.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                alertDialog_checkLogout.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_checkLogout.show();
    }
}