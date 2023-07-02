package org.techtown.MyExerciseApp.Feed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import org.techtown.MyExerciseApp.Adapter.Feed.CommentAdapter;
import org.techtown.MyExerciseApp.Data.FcmPush;
import org.techtown.MyExerciseApp.Data.Feed.Comment;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentFragment extends Fragment {

    private ImageView comment_mini_post_image,comment_image_profile;
    private TextView comment_post_nickname,comment_post_content,comment_post_comment,comment_post_creation_date;
    private RecyclerView comment_recycler;
    private EditText comment_add_comment;
    private Context context;
    private Post post;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;
    private Comment parentComment;
    private int parentCommentPosition;
    public CommentFragment(Post post){
        this.post = post;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_comment, container, false);
        comment_mini_post_image = rootView.findViewById(R.id.comment_mini_post_image);
        comment_image_profile = rootView.findViewById(R.id.comment_image_profile);
        comment_post_nickname = rootView.findViewById(R.id.comment_post_nickname);
        comment_post_content = rootView.findViewById(R.id.comment_post_content);
        comment_post_comment = rootView.findViewById(R.id.comment_post_comment);
        comment_recycler = rootView.findViewById(R.id.comment_recycler);
        comment_add_comment = rootView.findViewById(R.id.comment_add_comment);
        comment_post_creation_date = rootView.findViewById(R.id.comment_post_creation_date);

        comment_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        comment_recycler.setLayoutManager(mLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(context, commentList, post.getPostId());
        commentAdapter.setOnCommentClickListener(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onCommentClick(int ContainerViewId, Fragment fragment) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .add(ContainerViewId, fragment).addToBackStack(null).commit();
            }

            @Override
            public void onAddReplyClick(Comment comment,int position,String nickname) {
                String commentContent = comment.getCommentContent();
                if(comment.getCommentContent().length() > 5) {
                    commentContent = commentContent.substring(0, 5);
                }
                comment_add_comment.requestFocus();
                comment_add_comment.setHint(nickname+" : "+commentContent+"에 답글 작성");
                parentComment = comment;
                parentCommentPosition = position;
            }
        });
        comment_recycler.setAdapter(commentAdapter);

        postInfo(comment_mini_post_image,comment_post_nickname,comment_post_content,comment_post_creation_date,post);
        setCurrentUserProfile(currentUser.getUid());
        readComments();

        comment_post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comment_add_comment.getText().toString().equals("")){
                    Toast.makeText(context, "댓글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                }else{
                    if(parentComment == null) addComment();
                    else addComment(parentComment);
                }
            }
        });


        return rootView;
    }

    private void postInfo(final ImageView postImage, final TextView publisher, final TextView content,
                          final TextView creationDate,final Post post){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("POST");
        storageRef.child(post.getPostImage().get(0)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(postImage);
                publisher.setText(post.getPostNickname());
                content.setText(post.getContent());
                creationDate.setText(post.getCreationDate());
            }
        });
    }

    private void setCurrentUserProfile(String currentUserUid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("USER").child(currentUserUid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_image");
                storageRef.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(comment_image_profile);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("COMMENT").
                child(post.getPostId());
        String commentId = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", currentUser.getUid());
        hashMap.put("postId", post.getPostId());
        hashMap.put("commentId", commentId);
        hashMap.put("commentContent", comment_add_comment.getText().toString());
        hashMap.put("commentCreationDate", new GetToday().getTodayTime());

        reference.child(commentId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                new GetUser("uid",currentUser.getUid()).getUserByUid(new GetUserListener() {
                    @Override
                    public User getUserLoaded(User user) {
                        FcmPush.getFcmInstance()
                                .sendMessage(post.getUid(),"댓글",user.getNickname()
                                        + "님이 회원님의 게시글에 댓글을 작성했습니다.");
                        return null;
                    }
                });
            }
        });
        //addNotification();
        comment_add_comment.setText("");
    }
    private void addComment(Comment comment){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("COMMENT").child(post.getPostId());
        ArrayList<Comment> commentArrayList = comment.getReplies();
        if(commentArrayList == null){
            commentArrayList = new ArrayList<Comment>();
        }
        String commentCreationDate = new GetToday().getTodayTime();
        String commentId = reference.push().getKey();
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("uid", currentUser.getUid());
//        hashMap.put("postId", post.getPostId());
//        hashMap.put("commentId", commentId);
//        hashMap.put("commentContent", comment_add_comment.getText().toString());
//        hashMap.put("commentCreationDate",commentCreationDate );
//        hashMap.put("replies",null);
        commentArrayList.add(new Comment(currentUser.getUid(),post.getPostId(),
                commentId,comment_add_comment.getText().toString(),commentCreationDate,null));
        reference.child(parentComment.getCommentId()).child("replies").setValue(commentArrayList).addOnSuccessListener(
                new OnSuccessListener<Void>() {
//                      FcmPush.getFcmInstance()
//                              .sendMessage(post.getUid(),"답글",user.getNickname()
//                            + "님이"+ parentComment.getUid()+"회원님의 게시글에 댓글을 작성했습니다.");
                    @Override
                    public void onSuccess(Void unused) {
                        new GetUser("uid",currentUser.getUid()).getUserByUid(new GetUserListener() {
                            @Override
                            public User getUserLoaded(User user1) {
                                    new GetUser("uid",parentComment.getUid()).getUserByUid(
                                            new GetUserListener() {
                                                @Override
                                                public User getUserLoaded(User user) {
                                                    FcmPush.getFcmInstance()
                                                            .sendMessage(post.getUid(),"답글",user1.getNickname()
                                                                    + "님이"+ user.getNickname()+"님의 답글" +
                                                                    parentComment.getCommentContent()+"에 댓글을 작성했습니다.");
                                                    return null;
                                                }
                                            }
                                    );
                                return null;
                            }
                        });
                    }
                }
        );
        //addNotification();
        comment_add_comment.setText("");
        comment_add_comment.setHint("댓글 달기");
        parentComment = null;
        
    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("COMMENT")
                .child(post.getPostId());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.setItem(commentList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}