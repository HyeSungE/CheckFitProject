package org.techtown.MyExerciseApp.Adapter.Feed;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.techtown.MyExerciseApp.R;

import java.util.List;

public class PostViewImageAdapter extends RecyclerView.Adapter<PostViewImageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageNameList;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference("POST");
    public PostViewImageAdapter(Context context, List<String> imageNameList){
        this.context = context;
        this.imageNameList = imageNameList;
    }
    @Override
    public int getItemCount() {
        return imageNameList.size();
    }
    public void setItem(List<String> imageNameList){
        this.imageNameList = imageNameList;
    }

    @NonNull
    @Override
    public PostViewImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_view_image_layout, parent, false);
        return new PostViewImageAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewImageAdapter.ImageViewHolder holder, final int position) {
       // final Uri uri = uriList.get(position);
        //Glide.with(context).load(uri).into(holder.post_view_add_image_layout);

        final String imageName = imageNameList.get(position);
        storageRef.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.post_view_image);
            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_view_image;
        public ImageViewHolder(View itemView) {
            super(itemView);
            post_view_image = itemView.findViewById(R.id.post_view_image);
        }
    }





}