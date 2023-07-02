package org.techtown.MyExerciseApp.Adapter.Feed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.techtown.MyExerciseApp.Adapter.Exercise.RoutineInfoListAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Data.FcmPush;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Feed.CommentFragment;
import org.techtown.MyExerciseApp.Feed.MyFeedFragment;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator2;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Post> mPosts;
    private User currentUser;
    private FirebaseUser firebaseUser;
    private PagerSnapHelper pagerSnapHelper;
    private String routineDialogName = "";
    private long insertResult;
    private AppDatabase appDatabase;

    public PostAdapter(Context context, List<Post> posts,User user){
        mContext = context;
        mPosts = posts;
        currentUser = user;
        appDatabase = AppDatabase.getDBInstance(context );
    }
    public interface OnPostClickListener {
        void onPostClick(@IdRes int ContainerViewId, Fragment fragment);
    }
    private OnPostClickListener onPostClickListener = null;

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.onPostClickListener = listener;
    }

    public void setItem(List<Post> mPosts){
        this.mPosts = mPosts;
        notifyDataSetChanged();
    }
    public void addItem(Post post){
        mPosts.add(post);
        notifyItemChanged(mPosts.size()-1);
    }
    @NonNull
    @Override
    public PostAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_view_layout, parent, false);
        return new PostAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPosts.get(position);
        holder.post_view_image_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext,
                LinearLayoutManager.HORIZONTAL, false);
        holder.post_view_image_recycler.setLayoutManager(mLayoutManager);
        holder.imageNameList = post.getPostImage();
        holder.postViewImageAdapter = new PostViewImageAdapter(mContext, holder.imageNameList);
        holder.postViewImageAdapter.setItem(holder.imageNameList);
        holder.post_view_image_recycler.setAdapter(holder.postViewImageAdapter);

        //닉네임,프로필이미지
        publisherInfo(holder.post_view_image_profile,holder.post_view_nickname,holder.post_view_publisher,post.getUid());
        holder.post_view_creation_date.setText(post.getCreationDate());
        getLike(holder.post_view_likes,post.getPostId()); getCommetns(holder.post_view_more_comment,post.getPostId());
        isLiked(holder.post_view_like_icon,post.getPostId());

        //프로필
        holder.post_view_image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPostClickListener!=null) {
                    new GetUser("uid",currentUser.getUid()).getUserByUid(new GetUserListener() {
                        @Override
                        public User getUserLoaded(User user) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user", user);
                            bundle.putString("userNickname",post.getPostNickname());
                            Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                            onPostClickListener.onPostClick(R.id.feed_home_tab_container,myProfile);
                            return user;
                        }
                    });
                }
            }
        });
        holder.post_view_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPostClickListener!=null) {
                    new GetUser("uid",currentUser.getUid()).getUserByUid(new GetUserListener() {
                        @Override
                        public User getUserLoaded(User user) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user", user);
                            bundle.putString("userNickname",post.getPostNickname());
                            Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                            onPostClickListener.onPostClick(R.id.feed_home_tab_container,myProfile);
                            return user;
                        }
                    });
                }
            }
        });
        //좋아요
        holder.post_view_like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.post_view_like_icon.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("LIKE").child(post.getPostId())
                            .child(currentUser.getUid()).setValue(true);
                    FcmPush.getFcmInstance().sendMessage(post.getUid(), "1","1");
                    new GetUser("uid",firebaseUser.getUid()).getUserByUid(new GetUserListener() {
                        @Override
                        public User getUserLoaded(User user) {
                            FcmPush.getFcmInstance().sendMessage(post.getUid(),"좋아요",user.getNickname() + "님이 회원님의 게시글에 좋아요를 눌렀습니다.");
                            return null;
                        }
                    });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("LIKE").child(post.getPostId())
                            .child(currentUser.getUid()).removeValue();

                }
            }
        });
        //댓글
        holder.post_view_comment_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPostClickListener!=null){
                    Fragment commentFragment = new CommentFragment(post);
                    onPostClickListener.onPostClick(R.id.feed_home_tab_container,commentFragment);
                }

            }
        });
        holder.post_view_more_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPostClickListener!=null) {
                    Fragment commentFragment = new CommentFragment(post);
                    onPostClickListener.onPostClick(R.id.feed_home_tab_container, commentFragment);
                }
            }
        });
        holder.post_view_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.post_menu_edit:
                                editPost(post.getPostId());
                                return true;
                            case R.id.post_menu_delete:
                                final String id = post.getPostId();
                                FirebaseDatabase.getInstance().getReference("POST")
                                        .child(post.getPostId()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    //deleteNotifications(id, firebaseUser.getUid());
                                                }
                                            }
                                        });
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getUid().equals(currentUser.getUid())){
                    popupMenu.getMenu().findItem(R.id.post_menu_edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.post_menu_delete).setVisible(false);
                }
                popupMenu.show();
            }
        });

        holder.post_view_routine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPostRoutineView(post,mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView post_view_more;
        private CircleImageView post_view_image_profile;
        private TextView post_view_nickname,post_view_likes,post_view_publisher,post_view_content,post_view_more_comment,post_view_creation_date;
        private ImageView post_view_like_icon,post_view_comment_icon;
        private Button post_view_routine;
        private RecyclerView post_view_image_recycler;
        private List<String> imageNameList;
        private PostViewImageAdapter postViewImageAdapter;
        private Context context;
        private FirebaseUser currentUser;
        private FirebaseAuth auth;
        private CircleIndicator2 post_view_indicator;


        public ImageViewHolder(View itemView) {
            super(itemView);
            post_view_image_profile = itemView.findViewById(R.id.post_view_image_profile);
            post_view_nickname = itemView.findViewById(R.id.post_view_nickname);
            post_view_creation_date = itemView.findViewById(R.id.post_view_creation_date);
            post_view_image_recycler = itemView.findViewById(R.id.post_view_image_recycler);
            post_view_like_icon = itemView.findViewById(R.id.post_view_like_icon);
            post_view_comment_icon = itemView.findViewById(R.id.post_view_comment_icon);
            post_view_routine = itemView.findViewById(R.id.post_view_routine);
            post_view_likes = itemView.findViewById(R.id.post_view_likes);
            post_view_publisher = itemView.findViewById(R.id.post_view_publisher);
            post_view_content = itemView.findViewById(R.id.post_view_content);
            post_view_more_comment = itemView.findViewById(R.id.post_view_more_comment);
            post_view_more = itemView.findViewById(R.id.post_view_more);
            post_view_indicator = itemView.findViewById(R.id.post_view_indicator);
            pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(post_view_image_recycler);
            post_view_indicator.attachToRecyclerView(post_view_image_recycler,pagerSnapHelper);

        }
    }


    private void publisherInfo(final ImageView image_profile, final TextView nickname, final TextView publisher, final String uid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("USER").child(uid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_image");
                storageRef.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(mContext).load(uri).into(image_profile);
                        nickname.setText(user.getNickname());
                        publisher.setText(user.getNickname());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private void getLike(final TextView likes, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("LIKE").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount()+" likes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void getCommetns(final TextView comments,String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("COMMENT").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments.setText("View All "+dataSnapshot.getChildrenCount()+" Comments");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void isLiked(final ImageView imageView,String postId){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("LIKE").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.post_lie_fill);//post_like_empty
                    imageView.setTag("liked");
                } else{
                    imageView.setImageResource(R.drawable.post_like_empty);//post_lie_fill
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void editPost(final String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("수정");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getContent(postid, editText);

        alertDialog.setPositiveButton("수정",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("POST")
                                .child(postid).updateChildren(hashMap);
                    }
                });
        alertDialog.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
    }
    private void getContent(String postid, final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getContent());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void showPostRoutineView(Post post,Context context) {
        String routineString = post.getRoutine();
        List<RoutineInfoListItem> goPostRoutineViewList = new ArrayList<RoutineInfoListItem>();
        HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
        HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseList =  new HashMap<>();
        Type type = new TypeToken<HashMap<String, ArrayList<RoutineInfoListItemDetailed>>>(){}.getType();
        hashMap = new Gson().fromJson(routineString,type);
        for(String key : hashMap.keySet()){
            RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
            routineInfoListItem.setExercise(new Gson().fromJson(key, Exercise.class));
            routineInfoListItem.setRoutineInfoListItemDetailedArrayList((ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key));
            goPostRoutineViewList.add(routineInfoListItem);
        }
        showPostRoutineDialog(context,goPostRoutineViewList,routineString);
    }
    private void showPostRoutineDialog(Context context, List<RoutineInfoListItem> routineInfoListItemArrayList, String routineString) {
        AlertDialog.Builder viewRoutineAlert_builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRoutineView = layoutInflater.inflate(R.layout.fragment_post_routine_view_dialog, null);
        RecyclerView post_routine_view_info_recycler_view = viewRoutineView.findViewById(R.id.post_routine_view_info_recycler_view);
        Button post_routine_view_save = viewRoutineView.findViewById(R.id.post_routine_view_save);
        RoutineInfoListAdapter routineInfoListAdapter = new RoutineInfoListAdapter();


        post_routine_view_info_recycler_view.setLayoutManager(new LinearLayoutManager(context));
        post_routine_view_info_recycler_view.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        post_routine_view_info_recycler_view.setLayoutManager(layoutManager);
        post_routine_view_info_recycler_view.setAdapter(routineInfoListAdapter);
        routineInfoListAdapter.setItem(routineInfoListItemArrayList);
        routineInfoListAdapter.setMode("Post");



        post_routine_view_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout routineNameDialog = (LinearLayout) View.inflate(context, R.layout.routine_name_dialog, null);
                EditText routineNameEt = (EditText) routineNameDialog.findViewById(R.id.routine_name_dg_name_et);
                routineNameEt.setText(new GetToday().getTodayTime() + " 운동루틴");
                new AlertDialog.Builder(context)
                        .setView(routineNameDialog)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                routineDialogName = routineNameEt.getText().toString();
                                if (routineDialogName.equals("")) {
                                    Toast.makeText(context, "루틴에 이름이 없습니다 !", Toast.LENGTH_SHORT).show();
                                } else {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            insertResult = appDatabase.routineDao().insertRoutine(
                                                    routineDialogName, "", new GetToday().getTodayTime(), routineString);
                                        }
                                    });
                                    if (insertResult == 0) {
                                        Toast.makeText(context, "루틴 등록에 성공했습니다 !", Toast.LENGTH_SHORT).show();
                                    } else if (insertResult == -1) {
                                        Toast.makeText(context, "등록에 실패했습니다 !", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //다이어로그 보이기
        viewRoutineAlert_builder.setView(viewRoutineView);
        AlertDialog alertDialog_viewRoutine = viewRoutineAlert_builder.create();
        alertDialog_viewRoutine.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog_viewRoutine.show();


        //다이어로그 크기 조절
        WindowManager.LayoutParams params = alertDialog_viewRoutine.getWindow().getAttributes();
        DisplayMetrics dm = alertDialog_viewRoutine.getContext().getResources().getDisplayMetrics();
        // int widthDp = Math.round(400 * dm.density);
        int heightDp = Math.round(600 * dm.density);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = heightDp;
        alertDialog_viewRoutine.getWindow().setAttributes(params);
    }
}