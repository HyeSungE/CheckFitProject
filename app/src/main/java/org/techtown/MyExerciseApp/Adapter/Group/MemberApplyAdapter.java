package org.techtown.MyExerciseApp.Adapter.Group;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.R;

import java.util.List;

public class MemberApplyAdapter extends RecyclerView.Adapter<MemberApplyAdapter.ImageViewHolder>{
    private Context context;
    private List<Group> items;
    private User currentUser;
    private FirebaseUser firebaseUser;

    private String uid;
    private boolean isLeader;
    private boolean isMember;

    public MemberApplyAdapter(Context context, List<Group> items){
        this.context = context;
        this.items = items;
    }



//    public interface OnPostClickListener {
//        void onPostClick(@IdRes int ContainerViewId, Fragment fragment);
//    }
//    private PostAdapter.OnPostClickListener onPostClickListener = null;
//    public void setOnPostClickListener(PostAdapter.OnPostClickListener listener) {
//        this.onPostClickListener = listener;
//    }

    public void setItem(List<Group> items){
        this.items = items;
        notifyDataSetChanged();
    }
    public void addItem(Group item){
        items.add(item);
        notifyItemChanged(items.size()-1);
    }


    public MemberApplyAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_list_item_layout, parent, false);
        return new MemberApplyAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberApplyAdapter.ImageViewHolder holder, final int position) {
        ImageView group_list_item_image = holder.group_list_item_image;
        TextView group_list_item_name = holder.group_list_item_name;
        TextView group_list_item_creation_date = holder.group_list_item_creation_date;
        TextView group_list_item_current = holder.group_list_item_current;
        TextView group_list_item_limit = holder.group_list_item_limit;
        Button group_list_item_register = holder.group_list_item_register;
        Group item= items.get(position);

        FirebaseStorage.getInstance().getReference("group_image").child(item.getGroupImageName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(group_list_item_image);
                group_list_item_name.setText(item.getGroupName());
                group_list_item_creation_date.setText(item.getGroupCreationDate().split(" ")[0]);
                if(item.getGroupMembers()==null){
                    group_list_item_current.setText(0+"");
                }else{
                    group_list_item_current.setText(item.getGroupMembers().size()+"");
                }

                group_list_item_limit.setText(item.getGroupLimitSize()+"");

                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                group_list_item_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDatabase.getInstance().getReference("GROUP_WAITING/MEMBER").child(currentUserUid)
                                .child(item.getGroupUid()).setValue(null);
                        FirebaseDatabase.getInstance().getReference("GROUP_WAITING/LEADER").child(item.getGroupLeaderUid())
                                .child(item.getGroupUid()).child(currentUserUid).setValue(null);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        //View
        private ImageView group_list_item_image;
        private TextView group_list_item_name,group_list_item_creation_date;
        private TextView group_list_item_current,group_list_item_limit;
        private Button group_list_item_register;

        public ImageViewHolder(View itemView) {
            super(itemView);
            group_list_item_image = itemView.findViewById(R.id.group_member_list_item_image);
            group_list_item_name = itemView.findViewById(R.id.group_member_list_item_name);
            group_list_item_creation_date = itemView.findViewById(R.id.group_list_item_creation_date);
            group_list_item_current = itemView.findViewById(R.id.apply_group_current);
            group_list_item_limit = itemView.findViewById(R.id.apply_group_limit);
            group_list_item_register = itemView.findViewById(R.id.group_list_item_delete);
            group_list_item_register.setText("신청\n취소");

        }
    }

}
