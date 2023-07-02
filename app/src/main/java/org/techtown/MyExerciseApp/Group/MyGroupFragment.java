package org.techtown.MyExerciseApp.Group;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Adapter.Group.GroupListAdapter;
import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class MyGroupFragment extends Fragment {
    //view
    private RecyclerView my_group_recycler;
    private GroupListAdapter groupListAdapter;
    private TabLayout my_group_tab_layout;
    private TabItem my_group_leader_tab;
    private Context context;
    private List<Group> groups;
    private TextView my_group_no_item_message;
    private RequestManager requestManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my_group, container, false);
        context = requireActivity().getApplicationContext();
        my_group_tab_layout = rootView.findViewById(R.id.my_group_tab_layout);
        my_group_recycler = rootView.findViewById(R.id.my_group_recycler);
        my_group_no_item_message = rootView.findViewById(R.id.my_group_no_item_message);
        my_group_leader_tab = rootView.findViewById(R.id.my_group_leader_tab);

        my_group_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        my_group_recycler.setLayoutManager(mLayoutManager);
        groups = new ArrayList<>();
        requestManager = Glide.with(requireActivity());
        groupListAdapter = new GroupListAdapter(getContext(),groups);
        groupListAdapter.setRequestManager(requestManager);
        groupListAdapter.setItem(groups);
        my_group_recycler.setAdapter(groupListAdapter);
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        iAmLeaderGroup(currentUid);
        my_group_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0){
                    iAmLeaderGroup(currentUid);
                }
                else if(position == 1){
                    iAmMemberGroup(currentUid);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        return rootView;
    }

    private void iAmMemberGroup(String currentUid) {
        //내가 멤버인 그룹
        my_group_no_item_message.setVisibility(View.GONE);
        my_group_recycler.setVisibility(View.VISIBLE);
        ArrayList<Group> al = new ArrayList<>();
        groupListAdapter.setIsLeader(false);
        groupListAdapter.setIsMember(true);
        FirebaseDatabase.getInstance().getReference("GROUP").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Group g =dataSnapshot.getValue(Group.class);
                        if(g.getGroupMembers()!=null && g.getGroupMembers().contains(currentUid)) {
                            al.add(g);
                        }
                    }
                    if(!al.isEmpty()) {
                        List<Group> sortedAl = al.stream().sorted(Comparator.comparing(Group::getCreationDate).reversed()).collect(Collectors.toList());
                        groups.clear();
                        groups.addAll(sortedAl);
                        groupListAdapter.setItem(groups);
                    }else{
                        my_group_no_item_message.setText("그룹에 참여해서 \n운동을 함꼐 해보세요 !");
                        my_group_no_item_message.setVisibility(View.VISIBLE);
                        my_group_recycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {Thread.sleep(500);} catch(InterruptedException e){e.printStackTrace();}
    }

    private void iAmLeaderGroup(String currentUid) {
        my_group_no_item_message.setVisibility(View.GONE);
        my_group_recycler.setVisibility(View.VISIBLE);
        groupListAdapter.setIsLeader(true);
        groupListAdapter.setIsMember(false);
        Query query = FirebaseDatabase.getInstance().getReference().child("GROUP")
                .orderByChild("groupLeaderUid").equalTo(currentUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    ArrayList<Group> al = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        al.add(dataSnapshot.getValue(Group.class));
                    }
                    if (!al.isEmpty()) {
                        List<Group> sortedAl = al.stream().sorted(Comparator.comparing(Group::getCreationDate).reversed()).collect(Collectors.toList());
                        groups.clear();
                        groups.addAll(sortedAl);
                        groupListAdapter.setItem(groups);
                    } else {
                        my_group_no_item_message.setText("그룹을 만들어 \n운동을 공유해 보세요 !");
                        my_group_no_item_message.setVisibility(View.VISIBLE);
                        my_group_recycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}