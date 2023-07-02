package org.techtown.MyExerciseApp.Feed;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Exercise.ExerciseMainActivity;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.R;


public class FeedHomeFragment extends Fragment {

    private Fragment allFeedFragment,myFeedFragment,followFeedFragment;
    private MainActivity mainActivity;
    private ExerciseMainActivity exerciseMainActivity;
    private FirebaseAuth auth; private DatabaseReference reference; FirebaseUser user;
    private User currentUser;

    /*public FeedHomeFragment(User user) {
        this.currentUser = user;
    }*/

    public FeedHomeFragment() {

    }

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
        } else if (mainActivity != null) {
            mainActivity = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed_home, container, false);
        savedInstanceState = getArguments();
        currentUser = (User) savedInstanceState.getSerializable("user");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", currentUser);
        bundle.putString("userNickname",currentUser.getNickname());
        allFeedFragment = new AllFeedFragment(); allFeedFragment.setArguments(bundle);
        myFeedFragment = new MyFeedFragment();  myFeedFragment.setArguments(bundle);
        followFeedFragment = new FollowFeedFragment(); followFeedFragment.setArguments(bundle);

        replaceFragment(R.id.feed_home_tab_container,allFeedFragment);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.feed_home_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0){
                    replaceFragment(R.id.feed_home_tab_container,allFeedFragment);
                }else if(position == 1){
                    replaceFragment(R.id.feed_home_tab_container,myFeedFragment);
                }else if(position == 2){
                    replaceFragment(R.id.feed_home_tab_container,followFeedFragment);
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