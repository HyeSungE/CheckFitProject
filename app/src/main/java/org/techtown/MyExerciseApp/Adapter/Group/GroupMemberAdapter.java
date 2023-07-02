package org.techtown.MyExerciseApp.Adapter.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ImageViewHolder>{

    private Context context;
    private List<User> items;
    private Group group;
    public GroupMemberAdapter(Context context, List<User> items,Group group){
        this.context = context;
        this.items = items;
        this.group = group;
    }
    public void setItem(List<User> items){
        this.items = items;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_member_item_layout, parent, false);
        return new GroupMemberAdapter.ImageViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ImageViewHolder holder, int position) {
        ImageView group_member_list_item_image = holder.group_member_list_item_image;
        TextView group_member_list_item_name = holder.group_member_list_item_name;
        ImageButton group_list_item_delete = holder.group_list_item_delete;
        User item= items.get(position);

        FirebaseStorage.getInstance().getReference("profile_image").child(item.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(group_member_list_item_image);
                group_member_list_item_name.setText(item.getNickname());
                group_list_item_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAlertDeleteMember(item,position);
                    }
                });
            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        //View
        private ImageView group_member_list_item_image;
        private TextView group_member_list_item_name;
        private ImageButton group_list_item_delete;

        public ImageViewHolder(View itemView) {
            super(itemView);
            group_member_list_item_image = itemView.findViewById(R.id.group_member_list_item_image);
            group_member_list_item_name = itemView.findViewById(R.id.group_member_list_item_name);
            group_list_item_delete = itemView.findViewById(R.id.group_list_item_delete);
        }
    }
    private void showAlertDeleteMember(User user,int position) {
        AlertDialog.Builder deleteGroupAlert_builder = new AlertDialog.Builder(context);
        deleteGroupAlert_builder.setTitle("확인").setMessage("정말로 멤버를 내보내시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference("GROUP/"+group.getGroupUid()+"/"+"groupMembers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChildren()){
                                    List<String> uids = new ArrayList<String>();
                                    for(DataSnapshot child : snapshot.getChildren()){
                                       String uid =  child.getValue(String.class);
                                            if(uid.equals(items.get(position).getUid())){
                                                items.remove(position);
                                                notifyItemRemoved(position);
                                            }else{
                                                uids.add(uid);
                                            }

                                        FirebaseDatabase.getInstance().getReference("GROUP/"+group.getGroupUid()+"/"+"groupMembers").setValue(uids).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(context, "멤버를 성공적으로 내보냈습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog_deleteGroup = deleteGroupAlert_builder.create();

        alertDialog_deleteGroup.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(android.R.color.black));
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_deleteGroup.show();
    }

}
