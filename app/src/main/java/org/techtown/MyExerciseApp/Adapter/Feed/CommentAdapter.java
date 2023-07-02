package org.techtown.MyExerciseApp.Adapter.Feed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.MyExerciseApp.Data.Feed.Comment;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Feed.MyFeedFragment;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ImageViewHolder> {

    private Context context;
    private List<Comment> commentList,replyList;
    private String postId;
    private FirebaseUser currentUser;
    private ReplyAdapter replyAdapter;

    public CommentAdapter(Context context, List<Comment> commentList, String postId){
        this.context = context;
        this.commentList = commentList;
        this.postId = postId;
    }

    public void setItem(List<Comment> commentList){
        this.commentList = commentList;
        notifyDataSetChanged();
    }
    public interface OnCommentClickListener {
        void onCommentClick(@IdRes int ContainerViewId, Fragment fragment);
        void onAddReplyClick(Comment comment,int position,String nickname);
    }
    private OnCommentClickListener onCommentClickListener = null;
    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.onCommentClickListener = listener;
    }

    @NonNull
    @Override
    public CommentAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ImageViewHolder holder, final int position) {

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = commentList.get(position);
        new GetUser("uid",comment.getUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                StorageReference storageReference =
                        FirebaseStorage.getInstance().getReference("profile_image");
                storageReference.
                        child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.comment_item_image_profile);
                        holder.comment_item_nickname.setText(user.getNickname());
                        holder.comment_creation_date.setText(comment.getCommentCreationDate());
                        holder.comment_item_content.setText(comment.getCommentContent());

                        if(comment.getReplies() != null && comment.getReplies().size() > 0) {
                            holder.comment_reply_recycler.setVisibility(View.VISIBLE);
                            if(comment.getReplies().size() > 1) {
                                holder.comment_item_bottom.setVisibility(View.VISIBLE);
                                holder.comment_view_reply.setVisibility(View.VISIBLE);
                                holder.comment_view_reply.setText("답글 보기 ("+comment.getReplies().size()+")");
                            }
                            ArrayList<Comment> onlyOneComment = new ArrayList<>();
                            onlyOneComment.add(comment.getReplies().get(0));
                            holder.replyAdapter.setItem(onlyOneComment);
                        }
                        //답글남기기
                        holder.comment_add_reply.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onCommentClickListener.onAddReplyClick(comment,holder.getAdapterPosition(),holder.comment_item_nickname.getText().toString());
                            }
                        });
                        //댓글에서 피드로
                        String commentNickname = user.getNickname();
                        new GetUser("uid",currentUser.getUid()).getUserByUid(new GetUserListener() {
                            @Override
                            public User getUserLoaded(User user) {
                                holder.comment_item_image_profile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("user", user);
                                        bundle.putString("userNickname",commentNickname);
                                        System.out.println(user.getUid());
                                        System.out.println("commentNickname"+commentNickname);

                                        Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                                        onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
                                    }
                                });
                                holder.comment_item_nickname.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("user", user);
                                        bundle.putString("userNickname",commentNickname);
                                        System.out.println(user.getUid());
                                        System.out.println("commentNickname"+commentNickname);
                                        Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                                        onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
                                    }
                                });
                                return user;
                            }
                        });
                        //답글 더보기
                        holder.comment_view_reply.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               // holder.comment_view_reply.setText("답글 보기 ("+comment.getReplies().size()+")");
                                String comment_view_replyText = holder.comment_view_reply.getText().toString();
                                if(comment_view_replyText.contains("보기")){
                                    if(comment.getReplies() != null && comment.getReplies().size() > 0) {
                                        holder.replyAdapter.setItem(comment.getReplies());
                                    }
                                    holder.comment_view_reply.setText("답글 숨기기");
                                }else if(comment_view_replyText.contains("숨기기")){
                                    if(comment.getReplies() != null && comment.getReplies().size() > 0) {
                                        holder.comment_reply_recycler.setVisibility(View.VISIBLE);
                                        if(comment.getReplies().size() > 1) {
                                            holder.comment_item_bottom.setVisibility(View.VISIBLE);
                                            holder.comment_view_reply.setVisibility(View.VISIBLE);
                                            holder.comment_view_reply.setText("답글 보기 ("+comment.getReplies().size()+")");
                                        }
                                        ArrayList<Comment> onlyOneComment = new ArrayList<>();
                                        onlyOneComment.add(comment.getReplies().get(0));
                                        holder.replyAdapter.setItem(onlyOneComment);
                                    }
                                    holder.comment_view_reply.setText("답글 보기 ("+comment.getReplies().size()+")");
                                }
                            }
                        });
                    }
                });
                return user;
            }
        });


//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                if (comment.getPublisher().equals(firebaseUser.getUid())) {
//
//                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
//                    alertDialog.setTitle("Do you want to delete?");
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    FirebaseDatabase.getInstance().getReference("Comments")
//                                            .child(postid).child(comment.getCommentid())
//                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()){
//                                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                }
//                                            });
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.show();
//                }
//                return true;
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView comment_item_image_profile;
        public TextView comment_item_nickname, comment_creation_date,comment_item_content,comment_view_reply,
                comment_add_reply,comment_edit_comment,comment_delete_comment;
        public LinearLayout comment_options,comment_item_bottom;
        public RecyclerView comment_reply_recycler;
        public ReplyAdapter replyAdapter;
        public LinearLayoutManager mLayoutManager;
        public ImageViewHolder(View itemView) {
            super(itemView);
            comment_item_image_profile = itemView.findViewById(R.id.comment_item_image_profile);
            comment_item_nickname = itemView.findViewById(R.id.comment_item_nickname);
            comment_creation_date = itemView.findViewById(R.id.comment_creation_date);
            comment_item_content = itemView.findViewById(R.id.comment_item_content);
            comment_view_reply = itemView.findViewById(R.id.comment_view_reply);
            comment_view_reply.setVisibility(View.GONE);
            comment_options = itemView.findViewById(R.id.comment_options);
            comment_reply_recycler = itemView.findViewById(R.id.comment_reply_recycler);
            comment_reply_recycler.setVisibility(View.GONE);
            comment_add_reply = itemView.findViewById(R.id.comment_add_reply);
            comment_edit_comment = itemView.findViewById(R.id.comment_edit_comment);
            comment_delete_comment = itemView.findViewById(R.id.comment_delete_comment);
            comment_edit_comment.setVisibility(View.GONE);comment_delete_comment.setVisibility(View.GONE);
            comment_item_bottom = itemView.findViewById(R.id.comment_item_bottom);
            comment_item_bottom.setVisibility(View.GONE);

            comment_reply_recycler.setHasFixedSize(true);
             mLayoutManager = new LinearLayoutManager(context);
            comment_reply_recycler.setLayoutManager(mLayoutManager);
            replyAdapter = new ReplyAdapter(context);
            comment_reply_recycler.setAdapter(replyAdapter);
            replyAdapter.setItem(new ArrayList<>());
            replyAdapter.setOnCommentClickListener(new OnCommentClickListener() {
                @Override
                public void onCommentClick(int ContainerViewId, Fragment fragment) {

                    onCommentClickListener.onCommentClick(ContainerViewId,fragment);
                }

                @Override
                public void onAddReplyClick(Comment comment, int position, String nickname) {

                }
            });
        }
    }


}
