package org.techtown.MyExerciseApp.Group;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Adapter.Group.GroupListAdapter;
import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.List;

public class GroupMainFragment extends Fragment {

    //View
    private SearchView group_search_view;
    private Spinner group_spinner_bt;
    private RecyclerView group_all_recycler;
    private String searchMode;
    private GroupListAdapter groupListAdapter;
    private Context context;
    private List<Group> groups;
    private TextView group_main_no_item_message;
    private RequestManager requestManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_group_main, container, false);
        context = requireActivity().getApplicationContext();
        //SearchView
        group_search_view = rootView.findViewById(R.id.group_search_view);
        LinearLayout linearLayout1 = (LinearLayout) group_search_view.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
        //Button
        group_spinner_bt = rootView.findViewById(R.id.group_spinner_bt);

        //TextView
        group_main_no_item_message = rootView.findViewById(R.id.group_main_no_item_message);

        //RecyclerView
        group_all_recycler = rootView.findViewById(R.id.group_all_recycler);
        group_all_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        group_all_recycler.setLayoutManager(mLayoutManager);
        groups = new ArrayList<>();
        requestManager = Glide.with(requireActivity());
        groupListAdapter = new GroupListAdapter(getContext(),groups);
        groupListAdapter.setRequestManager(requestManager);
        groupListAdapter.setItem(groups);
        group_all_recycler.setAdapter(groupListAdapter);
        groupListAdapter.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("GROUP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Group> al = new ArrayList<>();
                if(snapshot.hasChildren()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Group g = ds.getValue(Group.class);
                        System.out.println("g.getGroupRoutines().size()++"+g.getGroupRoutines());
                        al.add(g);
                    }
                    groups.clear();
                    groups.addAll(al);
                    groupListAdapter.setItem(groups);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        searchMode = "leader";
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireActivity().getApplicationContext(),
                R.array.group_main_spinner, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        group_spinner_bt.setAdapter(spinnerAdapter);
        group_spinner_bt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==1) searchMode = "group";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        exerciseSearchView.clearFocus();
//        exerciseSearchView.setQuery("", false);
        group_search_view.setSubmitButtonEnabled(true);
        group_search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.isEmpty())
                    Toast.makeText(requireActivity().getApplicationContext(), "검색어를 입력해 주세요 !", Toast.LENGTH_SHORT).show();
                else{
                    if(searchMode.equals("leader")){
                        searchByLeaderNickname(s);
                    }else if(searchMode.equals("group")){
                        searchByGroup(s, searchMode);
                    }
                    group_search_view.clearFocus();
                    group_search_view.setQuery("", false);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return rootView;
    }

    private void searchByGroup(String s,String searchMode) {
        ArrayList<Group> searchList = new ArrayList<Group>();
        if(groups==null || groups.isEmpty()){
            group_main_no_item_message.setVisibility(View.VISIBLE);
            group_all_recycler.setVisibility(View.GONE);
        }else{
            if(searchMode.equals("group")){for(Group group :groups){
                group_main_no_item_message.setVisibility(View.GONE);
                group_all_recycler.setVisibility(View.VISIBLE);
                if(group.getGroupName().contains(s)){
                    searchList.add(group);
                }
            }}
            else if(searchMode.equals("leader")){for(Group group :groups){
                group_main_no_item_message.setVisibility(View.GONE);
                group_all_recycler.setVisibility(View.VISIBLE);
                if(group.getGroupLeaderUid().equals(s)){
                    searchList.add(group);
                }
            }}

            if(searchList.isEmpty()){
                Toast.makeText(requireActivity().getApplicationContext(), "해당하는 그룹이 없습니다 :(", Toast.LENGTH_SHORT).show();
            }else{
                groupListAdapter.setItem(searchList);
            }
        }
//
//
//        Query query = FirebaseDatabase.getInstance().getReference("GROUP").orderByChild(groupName).equalTo(s);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.hasChildren())
//                    Toast.makeText(requireActivity().getApplicationContext(), "해당하는 그룹이 없습니다 :(", Toast.LENGTH_SHORT).show();
//                else {
//                    ArrayList<Group> al = new ArrayList<>();
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Group g = dataSnapshot.getValue(Group.class);
//                        al.add(g);
//                    }
//                    groups.clear();
//                    groups.addAll(al);
//                    groupListAdapter.setItem(groups);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        try{Thread.sleep(500);}
//        catch (InterruptedException e){e.printStackTrace();}
    }

    private void searchByLeaderNickname(String s) {
        new GetUser("nickname", s).getUidByNickname(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                if(user==null){
                    Toast.makeText(requireActivity().getApplicationContext(), "존재하지 않는 유저입니다 :(", Toast.LENGTH_SHORT).show();
                }else{
                    String leaderUid = user.getUid();
                    searchByGroup(leaderUid,searchMode);
                }
                return null;
            }
        });
        try{Thread.sleep(500);}
        catch (InterruptedException e){e.printStackTrace();}
    }
}