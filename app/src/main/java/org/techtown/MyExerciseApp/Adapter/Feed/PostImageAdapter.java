package org.techtown.MyExerciseApp.Adapter.Feed;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import org.techtown.MyExerciseApp.Interface.PostClickEventListener;
import org.techtown.MyExerciseApp.R;

import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Uri> uriList;
    private PostClickEventListener postClickEventListener;
    public PostImageAdapter(Context context, List<Uri> uriList){
        this.context = context;
        this.uriList = uriList;
    }
    @Override
    public int getItemCount() {
        return uriList.size();
    }
    public void setItem(List<Uri> uriList){
        this.uriList = uriList;
        notifyDataSetChanged();
    }
    public void addItem(Uri uri){
        this.uriList.add(uri);
        notifyDataSetChanged();
        postClickEventListener.callImageCountTv();

    }
    public List<Uri> getUriList(){
      return this.uriList;

    }

    public void setPostClickEventListener(PostClickEventListener postClickEventListener) {this.postClickEventListener = postClickEventListener;}


    @NonNull
    @Override
    public PostImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_add_image_layout, parent, false);
        return new PostImageAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostImageAdapter.ImageViewHolder holder, final int position) {

        final Uri uri = uriList.get(position);
        Glide.with(context).load(uri).into(holder.post_view_add_image_layout);

        holder.post_view_add_image_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postClickEventListener.callSetMainImage(uri);
            }
        });

        holder.post_view_add_image_layout_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                postClickEventListener.callImageCountTv();
                postClickEventListener.callSetMainImage(null);
            }
        });

    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_view_add_image_layout;
        public Button post_view_add_image_layout_delete;


        public ImageViewHolder(View itemView) {
            super(itemView);

            post_view_add_image_layout = itemView.findViewById(R.id.post_view_add_image_layout);
            post_view_add_image_layout_delete = itemView.findViewById(R.id.post_view_add_image_layout_delete);

        }
    }





}