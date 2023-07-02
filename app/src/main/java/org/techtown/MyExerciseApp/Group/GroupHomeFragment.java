package org.techtown.MyExerciseApp.Group;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.R;


public class GroupHomeFragment extends Fragment {

    private User currentUser;
    private TabLayout group_home_tab_layout;
    private Fragment groupMainFragment, groupMyFragment;
    //FloatingActionButton
    private FloatingActionButton group_floating_bt;
    private LinearLayout group_floating_menu;
    private ImageView group_home_alarm;
    public GroupHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_group_home,container,false);
        groupMainFragment = new GroupMainFragment();
        groupMyFragment = new MyGroupFragment();
        replaceFragment(R.id.group_home_tab_container,groupMainFragment);
        group_home_tab_layout = (TabLayout) rootView.findViewById(R.id.group_home_tab_layout);
        group_floating_menu = rootView.findViewById(R.id.group_floating_menu);
        group_home_alarm = rootView.findViewById(R.id.group_home_alarm);

        FirebaseDatabase.getInstance().getReference("GROUP_WAITING").child("LEADER").child(
                FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) group_home_alarm.setVisibility(View.VISIBLE);
                else group_home_alarm.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        group_home_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0) replaceFragment(R.id.group_home_tab_container,groupMainFragment);
                else if(position == 1)  replaceFragment(R.id.group_home_tab_container,groupMyFragment);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        group_floating_bt = rootView.findViewById(R.id.group_floating_bt);
        //FAB click event
        group_floating_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(group_floating_menu.getVisibility() == View.GONE){
                    group_floating_menu.setVisibility(View.VISIBLE);
                }else if(group_floating_menu.getVisibility() == View.VISIBLE){
                    group_floating_menu.setVisibility(View.GONE);
                }
            }
        });
        group_floating_menu.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertGoCreateGroup();
            }
        });

        return rootView;
    }
    public boolean replaceFragment(int viewId,Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId,fragment).commit();
        return true;
    }
    private void showAlertGoCreateGroup() {
        AlertDialog.Builder goCreateGroupAlert_builder = new AlertDialog.Builder(requireActivity());
        goCreateGroupAlert_builder.setTitle("확인").setMessage("그룹을 만드시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Fragment groupCreateFragment = new GroupCreateFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().add(R.id.tab_content_view,groupCreateFragment).addToBackStack(null).commit();

                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });
        AlertDialog alertDialog_goCreateGroup = goCreateGroupAlert_builder.create();
        alertDialog_goCreateGroup.show();
    }

//    private void showAlertGoApplyManageGroup() {
//        AlertDialog.Builder goApplyManageGroupAlert_builder = new AlertDialog.Builder(requireActivity());
//        goApplyManageGroupAlert_builder.setTitle("확인").setMessage("그룹 관리 화면으로 이동합니다.")
//                .setPositiveButton("예", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Fragment groupApplyManageFragment = new GroupApplyManageFragment();
//                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                        fragmentManager.beginTransaction().add(R.id.tab_content_view,groupApplyManageFragment).addToBackStack(null).commit();
//
//                    }
//                })
//                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//
//                    }
//                });
//        AlertDialog alertDialog_goApplyManageGroup = goApplyManageGroupAlert_builder.create();
//        alertDialog_goApplyManageGroup.show();
//    }


}