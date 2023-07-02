package org.techtown.MyExerciseApp.Feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Adapter.Feed.FeedMainAdapter;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllFeedFragment extends Fragment {

    private User currentUser;
    private List<Post> postList;
    private FeedMainAdapter feedMainAdapter;
    private RecyclerView all_feed_recycler;
//    public AllFeedFragment(User user) {
//        this.currentUser = user;
//    }
    public AllFeedFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_all_feed, container, false);
        savedInstanceState = getArguments();
        currentUser = (User) savedInstanceState.getSerializable("user");
        all_feed_recycler = rootView.findViewById(R.id.all_feed_recycler);
        all_feed_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        all_feed_recycler.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        feedMainAdapter = new FeedMainAdapter(getContext(), postList);
        all_feed_recycler.setAdapter(feedMainAdapter);
        feedMainAdapter.setFrom("allFeed");
        AllPosts(null);
        return rootView;
    }

    private void AllPosts(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    postList.add(post);
                }
                Collections.reverse(postList);
                feedMainAdapter.setPostList(postList);
                feedMainAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}