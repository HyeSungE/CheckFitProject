package org.techtown.MyExerciseApp.Adapter.Feed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import java.util.List;


public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ImageViewHolder> {

    private Context context;
    private List<Comment> replyList;

    private FirebaseUser firebaseUser;

    public ReplyAdapter(Context context){
        this.context = context;

    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    public void setItem(List<Comment> replyList){
        this.replyList = replyList;
        notifyDataSetChanged();
    }

    public interface OnCommentClickListener {
        void onCommentClick(@IdRes int ContainerViewId, Fragment fragment);

    }
    private CommentAdapter.OnCommentClickListener onCommentClickListener = null;

    public void setOnCommentClickListener(CommentAdapter.OnCommentClickListener listener) {
        this.onCommentClickListener = listener;
    }


    @NonNull
    @Override
    public ReplyAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new ReplyAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReplyAdapter.ImageViewHolder holder, final int position) {
        final Comment reply = replyList.get(position);

        new GetUser("uid",reply.getUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_image");
                storageReference.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(holder.getAdapterPosition()==0){
                            holder.comment_reply_start.setVisibility(View.VISIBLE);
                            holder.comment_reply_start.setImageResource(R.drawable.ic_baseline_subdirectory_arrow_right_24);
                            Log.d("ReplyAdapter",holder.getAdapterPosition()+"");
                        }
                        Glide.with(context).load(uri).into(holder.comment_item_image_profile);

                        holder.comment_item_nickname.setText(user.getNickname());
                        holder.comment_creation_date.setText(reply.getCommentCreationDate());
                        holder.comment_item_content.setText(reply.getCommentContent());

                        String replyNickname = user.getNickname();
                        new GetUser("uid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .getUserByUid(new GetUserListener() {
                                    @Override
                                    public User getUserLoaded(User user) {
                                        holder.comment_item_image_profile.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("user", user);
                                                bundle.putString("userNickname",replyNickname);
                                                System.out.println(user.getUid());
                                                System.out.println("replyNickname"+replyNickname);
                                                Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                                                onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
                                            }
                                        });
                                        holder.comment_item_nickname.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("user", user);
                                                bundle.putString("userNickname",replyNickname);
                                                System.out.println(user.getUid());
                                                System.out.println("replyNickname"+replyNickname);
                                                Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
                                                onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
                                            }
                                        });
                                        return null;
                                    }
                                });
//                        holder.comment_item_image_profile.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Bundle bundle = new Bundle();
//                                bundle.putSerializable("user", user);
//                                bundle.putString("userNickname",user.getNickname());
//                                Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
//                                onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
//                            }
//                        });
//                        holder.comment_item_nickname.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Bundle bundle = new Bundle();
//                                bundle.putSerializable("user", user);
//                                bundle.putString("userNickname",user.getNickname());
//                                Fragment myProfile = new MyFeedFragment(); myProfile.setArguments(bundle);
//                                onCommentClickListener.onCommentClick(R.id.feed_home_tab_container,myProfile);
//                            }
//                        });
                    }
                });
                return user;
            }
        });

    }

//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        final Comment comment = mComment.get(position);
//
//        holder.comment.setText(comment.getComment());
//        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());
//
//        holder.username.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(mContext, MainActivity.class);
//                intent.putExtra("publisherid", comment.getPublisher());
//                mContext.startActivity(intent);
//            }
//        });
//
//        holder.image_profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(mContext, MainActivity.class);
//                intent.putExtra("publisherid", comment.getPublisher());
//                mContext.startActivity(intent);
//            }
//        });
//
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




    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView comment_item_image_profile,comment_reply_start;
        public TextView comment_item_nickname, comment_creation_date, comment_item_content, comment_view_reply,
                comment_add_reply, comment_edit_comment, comment_delete_comment;
        public LinearLayout comment_options;

        public ImageViewHolder(View itemView) {
            super(itemView);
            comment_item_image_profile = itemView.findViewById(R.id.comment_item_image_profile);
            comment_item_nickname = itemView.findViewById(R.id.comment_item_nickname);
            comment_creation_date = itemView.findViewById(R.id.comment_creation_date);
            comment_item_content = itemView.findViewById(R.id.comment_item_content);
            comment_view_reply = itemView.findViewById(R.id.comment_view_reply);
            comment_options = itemView.findViewById(R.id.comment_options);
            comment_options.setVisibility(View.GONE);
            comment_add_reply = itemView.findViewById(R.id.comment_add_reply);
            comment_edit_comment = itemView.findViewById(R.id.comment_edit_comment);
            comment_delete_comment = itemView.findViewById(R.id.comment_delete_comment);
            comment_reply_start = itemView.findViewById(R.id.comment_reply_start);
            comment_reply_start.setVisibility(View.INVISIBLE);
        }
    }


}
