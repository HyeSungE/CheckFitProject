package org.techtown.MyExerciseApp.Exercise;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.techtown.MyExerciseApp.Adapter.Exercise.RoutineListItemAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineItem;
import org.techtown.MyExerciseApp.Interface.SendEventListener;
import org.techtown.MyExerciseApp.MyClass.GetDp;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.Routine;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class RoutineListFragment extends Fragment implements ExerciseMainActivity.onBackPressedListener {
    private Activity mActivity;
    private SendEventListener sendEventListener;
    private Context context;
    private Button routineListAddBt,customRoutineListAddBt;
    private SearchView routineSearchView;
    private LinearLayout routineListFragmentBottomLayout;
    private FrameLayout frameLayout;
    private TabLayout routineListTabLayout;
    private RecyclerView recyclerView;
    private TextView no_item_message;
    private AppDatabase appDatabase;
    private RoutineListItemAdapter routineListItemAdapter;
    private RoutineViewFragment routineViewFragment;
    private List<Routine> routineArrayList;
    private int tabPosition;
    private  Gson gson = new Gson();
    private String mode = "none",from,from_2;
    public RoutineListFragment (Context context){
        this.context = context;
    }
    public RoutineListFragment ( ){}

    public void onBackPressed() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {mActivity = (Activity)context;}
        try{sendEventListener = (SendEventListener) context;}
        catch (ClassCastException e){throw new ClassCastException((context.toString()+ " must implements SendEvent"));}
    }
    @Override
    public void onDetach() {
        mActivity = null;context = null;super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_routine_list,container,false);
        init(rootView);
        savedInstanceState = getArguments();
        if(savedInstanceState!=null){
            mode = savedInstanceState.getString("mode");
            from = savedInstanceState.getString("from");
            from_2 = savedInstanceState.getString("from_2");
            if(from.equals("home")) settingBottomOfRoutineListFragment(60);
        }

        return rootView;
    }

    private void init(ViewGroup rootView) {
        tabPosition = 1;
        appDatabase = AppDatabase.getDBInstance(getActivity().getApplicationContext());
        frameLayout = (FrameLayout)rootView.findViewById(R.id.fragment_routine_list_frameLayout);
        routineListFragmentBottomLayout = (LinearLayout) rootView.findViewById(R.id.routine_list_fragment_bottom_lr);
        customRoutineListAddBt = (Button)rootView.findViewById(R.id.custom_routine_bt);

        routineSearchView = (SearchView)rootView.findViewById(R.id.routine_search_view);
        routineListTabLayout = (TabLayout)rootView.findViewById(R.id.main_routine_list_tab_layout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.routine_list_recycler_view);
        no_item_message = (TextView) rootView.findViewById(R.id.no_item_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        routineListItemAdapter = new RoutineListItemAdapter();
        routineListItemAdapter.setAppdatabase(appDatabase);
        recyclerView.setAdapter(routineListItemAdapter);


        /*
        * 데이터베이스에서 루틴리스트를 받아오고 String으로 되어 있는 운동리스트들을 RoutineInfoList로 바꿈
        */
        setRoutineList(0);

        routineListItemAdapter.setOnItemClickListener(mItemClickListener);
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.custom_routine_bt : {
                        routineViewFragment = new RoutineViewFragment();
                        Bundle bundle = new Bundle();
                        if(from.equals("home")) {
                            bundle.putString("mode","custom");
                            bundle.putString("from","home");
                        }
                        else {
                            bundle.putString("mode","custom");
                            bundle.putString("from","notHome");

                        }
                        routineViewFragment.setArguments(bundle);
                        replaceFragment_addToBack(R.id.fragment_routine_list_frameLayout,routineViewFragment);
                        break;
                    }
                }
            }
        };
        customRoutineListAddBt.setOnClickListener(onClickListener);

        routineListTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                if(tabPosition==3){
                    no_item_message.setText("AI 루틴 추천 기능은\n아직 구현 중이에요 :(");
                    no_item_message.setVisibility(View.VISIBLE);
                }else{
                    no_item_message.setText("운동을 추가 해주세요 !");
                    no_item_message.setVisibility(View.GONE);
                }

                routineSearchView.clearFocus();
                routineSearchView.setQuery("", false);
                setRoutineList(tabPosition);

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        routineSearchView.setSubmitButtonEnabled(true);
        routineSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                int tabPosition = routineListTabLayout.getSelectedTabPosition();
                if (s.equals("") || s == null || s.isEmpty())
                    setRoutineList(tabPosition);
                else
                    searchRoutine(s, tabPosition);
                return false;
            }
        });

    }
    //ㅁㄴㅇㅁㄴㅇ
    private RoutineListItemAdapter.OnItemClickListener mItemClickListener = new RoutineListItemAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position,RoutineItem routineItem) {
            Log.d(TAG,"position: " + position + "name : " + routineItem.getRoutine().getRoName());
            routineViewFragment = new RoutineViewFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("selectRoutine", routineItem);
            if(from.equals("home")) {
                bundle.putString("mode","select");
                bundle.putString("from","home");
            }
            else {
                bundle.putString("mode","select");
                bundle.putString("from","notHome");
                if(from_2!=null) bundle.putString("from_2",from_2);
            }
            routineViewFragment.setArguments(bundle);
            replaceFragment_addToBack(R.id.fragment_routine_list_frameLayout,routineViewFragment);
        }
    };
    public void settingBottomOfRoutineListFragment(int margin) {

        GetDp getDp = new GetDp();
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,0.2f
        );
        param.setMargins(0,0,0,getDp.getDp(requireActivity(),margin));
        routineListFragmentBottomLayout.setLayoutParams(param);

    }
    public void hideFragment() {frameLayout.setVisibility(View.GONE);}
    public void showFragment() {
        frameLayout.setVisibility(View.VISIBLE);
    }
    public boolean replaceFragment_addToBack(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().add(viewId, fragment).addToBackStack(null).commit();
        return true;
    }
    public void setRoutineList(int tabPosition) {
        switch (tabPosition) {
            case 0:{
                appDatabase.routineDao().getRoutineFavorite().observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);
                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
            case 1:{
                appDatabase.routineDao().getRoutineList().observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);

                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
            case 2:{
                appDatabase.routineDao().getRoutineLatest().observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);
                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
        }
    }

    public void searchRoutine(String searchString,int tabPosition) {
        switch (tabPosition) {
            case 0:{
                appDatabase.routineDao().searchRoutineFavorite(searchString).observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);

                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
            case 1:{
                appDatabase.routineDao().searchRoutine(searchString).observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);

                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
            case 2:{
                appDatabase.routineDao().searchRoutineLatest(searchString).observe(getViewLifecycleOwner(), new Observer<List<Routine>>() {
                    @Override
                    public void onChanged(List<Routine> routines) {
                        routineArrayList = routines;
                        LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
                        ArrayList<RoutineItem> routineItemArrayList = new ArrayList<>();
                        if(routineArrayList.size() > 0) {
                            for(Routine routine : routineArrayList) {
                                String fromJson =  routine.getRoExerciseList();
                                Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
                                hashMap = gson.fromJson(fromJson, type);
                                ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
                                for(String key : hashMap.keySet()) {
                                    Exercise exercise = gson.fromJson(key,Exercise.class);
                                    ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                                            (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                                    routineInfoListItems.add(routineInfoListItem);
                                }
                                RoutineItem routineItem = new RoutineItem(routine,routineInfoListItems);
                                routineItemArrayList.add(routineItem);
                            }
                            routineListItemAdapter.setItem(routineItemArrayList);

                        }
                        setNo_item_message(routineArrayList.size());
                    }
                });
                break;
            }
        }
    }
    private void setNo_item_message(int size) {
        if(size <= 0){
            recyclerView.setVisibility(View.GONE);
            no_item_message.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            no_item_message.setVisibility(View.GONE);
        }
    }
}