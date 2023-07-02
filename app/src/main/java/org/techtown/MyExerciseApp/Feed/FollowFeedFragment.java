package org.techtown.MyExerciseApp.Feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Adapter.Feed.PostAdapter;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.List;


public class FollowFeedFragment extends Fragment {
    private RecyclerView follow_feed_recycler;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseUser firebaseUser;
    private TextView no_follow_message;

    public FollowFeedFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_follow_feed, container, false);
        no_follow_message = rootView.findViewById(R.id.no_follow_message);
        follow_feed_recycler = rootView.findViewById(R.id.follow_feed_recycler);
        follow_feed_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        follow_feed_recycler.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        new GetUser("uid",FirebaseAuth.getInstance().getCurrentUser().getUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                postAdapter = new PostAdapter(getContext(), postList,user);
                follow_feed_recycler.setAdapter(postAdapter);
                postAdapter.setOnPostClickListener(new PostAdapter.OnPostClickListener(){
                    @Override
                    public void onPostClick(@IdRes int ContainerViewId, Fragment fragment) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                               .add(ContainerViewId, fragment).addToBackStack(null).commit();

                    }
                });

                FollowPosts(user);

                return null;
            }
        });

        return rootView;
    }

    private void FollowPosts(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("FOLLOW");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    followRef.child(user.getUid()).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChildren()){
                                if(snapshot.child(post.getUid()).exists()){
                                    postList.add(post);
                                    postAdapter.setItem(postList);
                                }
                            }else{
                                no_follow_message.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
