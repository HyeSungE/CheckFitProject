package org.techtown.MyExerciseApp.Exercise;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.R;

public class ExerciseListHomeFragment extends Fragment {

    private MainActivity mainActivity;
    private ExerciseMainActivity exerciseMainActivity;
    private Fragment fragment_exercise_list,fragment_routine_list;
    private ExerciseListFragment exerciseListFragment;
    private RoutineListFragment routineListFragment;
    private FragmentManager fragmentManager;
    private FrameLayout frameLayout;
    private Button customRoutineBt;
    private MaterialToolbar topAppBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(getActivity().equals(ExerciseMainActivity.class)){
            exerciseMainActivity = (ExerciseMainActivity) getActivity();
        }else if(getActivity().equals(MainActivity.class)){
            mainActivity = (MainActivity) getActivity();
        }

    }
    @Override
    public void onDetach() {
        super.onDetach();
        if (exerciseMainActivity != null) {
            exerciseMainActivity = null;
        }else if(mainActivity != null){
            mainActivity = null;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_exercise_list_home, container, false);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.home_exercise_list_tab_layout);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.home_exercise_list_tab_container);
//        fragmentManager = getChildFragmentManager();
//        exerciseListFragment = (ExerciseListFragment)fragmentManager.findFragmentById(R.id.exercise_list_fragment);
//        e
//        routineListFragment = (RoutineListFragment)fragmentManager.findFragmentById(R.id.routine_list_fragment);

        exerciseListFragment = new ExerciseListFragment(requireActivity().getApplicationContext());
        Bundle exerciseListFragmentBundle = new Bundle();
        //exerciseListFragmentBundle.putString("mode","custom");
        exerciseListFragmentBundle.putString("from","home");
        exerciseListFragment.setArguments(exerciseListFragmentBundle);

        routineListFragment = new RoutineListFragment(requireActivity().getApplicationContext());
        Bundle routineListFragmentBundle = new Bundle();
        routineListFragmentBundle.putString("mode","custom");
        routineListFragmentBundle.putString("from","home");
        routineListFragment.setArguments(routineListFragmentBundle);

        addFragment(R.id.home_exercise_list_tab_container,exerciseListFragment);


        addFragment(R.id.home_exercise_list_tab_container,routineListFragment);

        hideFragment(routineListFragment);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0 ) {
                    hideFragment(routineListFragment);
                    showFragment(exerciseListFragment);
                }else if(position == 1) {
                    hideFragment(exerciseListFragment);
                    showFragment(routineListFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return rootView;
    }
    public boolean replaceFragment(int viewId,Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId,fragment).commit();
        return true;
    }
    public boolean addFragment(int viewId,Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().add(viewId,fragment).commit();
        return true;
    }
    public boolean showFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().show(fragment).commit();
        return true;
    }
    public boolean hideFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(fragment).commit();
        return true;
    }

    public Fragment getFragment(Fragment fragment) {
        for(Fragment tmp : getActivity().getSupportFragmentManager().getFragments()){
            System.out.println(tmp);
            if(tmp.getClass().equals(fragment.getClass())){
                return tmp;
            }
        }
        return null;
    }

}