package org.techtown.MyExerciseApp.Feed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.MyExerciseApp.Adapter.Feed.FeedMainAdapter;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Main.LoginActivity;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


 public class MyFeedFragment extends Fragment implements GetUserListener {

    private static final String TAG = "MyFeedFragment";
    public static final int EDIT_PROFILE_REQUEST = 0327;
    private ImageView image_profile;
    private TextView my_feed_posts,my_feed_followers,my_feed_following,my_feed_fullname,my_feed_description;
    private Button my_feed_edit_profile;
    private RecyclerView my_feed_posts_recycler_view;
    private FirebaseAuth auth; private DatabaseReference reference; FirebaseUser user;
    private ShowAlertSimpleMessage showAlertSimpleMessage;
    private Context context;
    private List<Post> postList;
    private FeedMainAdapter feedMainAdapter;
    private String feedOwnerNickname;
    private User currentUser,feedOwner,targetUser;


    public MyFeedFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my_feed, container, false);



        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        showAlertSimpleMessage = new ShowAlertSimpleMessage();
        context = requireActivity().getApplicationContext();
        if(user == null){
            showAlertSimpleMessage.show(context,"오류","로그인 후 이용해 주세요\n로그인으로 이동합니다!");
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        image_profile = rootView.findViewById(R.id.comment_image_profile);
        my_feed_posts = rootView.findViewById(R.id.my_feed_posts);
        my_feed_followers = rootView.findViewById(R.id.my_feed_followers);
        my_feed_following = rootView.findViewById(R.id.my_feed_following);
        my_feed_fullname = rootView.findViewById(R.id.my_feed_nickname);
        my_feed_description = rootView.findViewById(R.id.my_feed_description);
        my_feed_edit_profile = rootView.findViewById(R.id.my_feed_edit_profile);

        my_feed_posts_recycler_view = rootView.findViewById(R.id.my_feed_posts_recycler_view);
        my_feed_posts_recycler_view.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        my_feed_posts_recycler_view.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        feedMainAdapter = new FeedMainAdapter(getContext(), postList);
        my_feed_posts_recycler_view.setAdapter(feedMainAdapter);
        feedMainAdapter.setFrom("myFeed");
        feedOwnerNickname = (String) getArguments().getString("userNickname");
        currentUser = (User) getArguments().getSerializable("user");
        System.out.println("feedOwnerNickname : " + feedOwnerNickname);
        new GetUser("nickname",feedOwnerNickname).getUidByNickname(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                if(!currentUser.getNickname().equals(feedOwnerNickname)){
                    my_feed_edit_profile.setText("팔로우");
                    userInfo(user); getFollowers(user); getPosts(user); myPosts(user);
                    targetUser = user;
                    checkFollow(user);
                    System.out.println(user.getDescription());
                    System.out.println(user.getUid());
                }else{
                    targetUser = currentUser;
                    userInfo(currentUser); getFollowers(currentUser); getPosts(currentUser); myPosts(currentUser);
                }


                my_feed_edit_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String btn = my_feed_edit_profile.getText().toString();

                        if (btn.equals("프로필 편집")){
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user",currentUser);
                            Intent intent = new Intent(getActivity(),EditProfile.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent,EDIT_PROFILE_REQUEST);
                        } else if (btn.equals("팔로우")){
                            FirebaseDatabase.getInstance().getReference().child("FOLLOW").child(currentUser.getUid())
                                    .child("following").child(user.getUid()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("FOLLOW").child(user.getUid())
                                    .child("followers").child(currentUser.getUid()).setValue(true);
                            //addNotification();
                        } else if (btn.equals("팔로잉")){

                            FirebaseDatabase.getInstance().getReference().child("FOLLOW").child(currentUser.getUid())
                                    .child("following").child(user.getUid()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("FOLLOW").child(user.getUid() )
                                    .child("followers").child(currentUser.getUid()).removeValue();
                        }
                    }
                });
                return user;
            }
        });
        return rootView;
    }

    private void checkFollow(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("FOLLOW").child(currentUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user.getUid()).exists()){
                    my_feed_edit_profile.setText("팔로잉");
                } else{
                    my_feed_edit_profile.setText("팔로우");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void userInfo(User user){

        DatabaseReference reference = FirebaseDatabase.getInstance().
                getReference("USER").child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User u = dataSnapshot.getValue(User.class);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_image");
                storageRef.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(image_profile);
                        my_feed_fullname.setText(u.getNickname());
                        my_feed_description.setText(u.getDescription());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private void getFollowers(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("FOLLOW").child(user.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_feed_followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("FOLLOW").child(user.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {my_feed_following.setText(""+dataSnapshot.getChildrenCount());}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private void getPosts(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getUid().equals(user.getUid())){
                        i++;
                    }
                }
                my_feed_posts.setText(""+i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private void myPosts(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getUid().equals(user.getUid())){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                feedMainAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_PROFILE_REQUEST){
        }
    }

    @Override
    public User getUserLoaded(User user) {
        return user;
    }
}