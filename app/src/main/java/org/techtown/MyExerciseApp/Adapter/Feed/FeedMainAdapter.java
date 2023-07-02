package org.techtown.MyExerciseApp.Adapter.Feed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Feed.PostViewFragment;
import org.techtown.MyExerciseApp.R;

import java.util.List;

public class FeedMainAdapter extends RecyclerView.Adapter<FeedMainAdapter.ImageViewHolder> {

    private Context context;
    private List<Post> posts;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference("POST");
    private Bundle postBundle;
    private String from;
    public FeedMainAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;
    }
    public void setFrom(String from){
        this.from = from;
    }

    public void setPostList(List<Post> posts){
        this.posts = posts;
    }
    @NonNull
    @Override
    public FeedMainAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_main_item_layout, parent, false);
        return new FeedMainAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedMainAdapter.ImageViewHolder holder, final int position) {
        if(posts.size()>0){
            final Post post = posts.get(position);
            storageRef.child(post.getPostImage().get(0)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(holder.feed_main_item_image);
                }
            });
            holder.feed_main_item_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postBundle = new Bundle();
                    postBundle.putSerializable("selectedPost",post);
                    Fragment postView = new PostViewFragment();
                    postView.setArguments(postBundle);
                    if(from.equals("allFeed")){
                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().
                                add(R.id.feed_home_tab_container, postView).addToBackStack(null).commit();
                    }else if(from.equals("myFeed")){
                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().
                                add(R.id.feed_home_tab_container, postView).addToBackStack(null).commit();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView feed_main_item_image;


        public ImageViewHolder(View itemView) {
            super(itemView);

            feed_main_item_image = itemView.findViewById(R.id.feed_main_item_image);

        }
    }
}