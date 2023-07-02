package org.techtown.MyExerciseApp.Adapter.Group;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Group.ShowGroupFragment;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ImageViewHolder>{
    private Context context;
    private List<Group> items;
    private User currentUser;
    private FirebaseUser firebaseUser;

    private String uid;
    private boolean isLeader;
    private boolean isMember;

    private RequestManager requestManager;
    HashMap<String,List<User>> registeringUsers = new HashMap<String,List<User>>();
    public GroupListAdapter(Context context, List<Group> items){
        this.context = context;
        this.items = items;
    }
    public void setUid(String uid){
        this.uid = uid;
    }
    public void setIsLeader(boolean isLeader){
        this.isLeader = isLeader;
    }
    public void setIsMember(boolean isMember){
        this.isMember = isMember;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }


    public interface OnGroupClickListener {
        void onGroupClick(@IdRes int ContainerViewId, Fragment fragment);
    }


    public void setItem(List<Group> items){
        this.items = items;
        notifyDataSetChanged();
    }
    public void addItem(Group item){
        items.add(item);
        notifyItemChanged(items.size()-1);
    }
    public void setRegisteringUsers(HashMap<String,List<User>> registeringUsers){
        this.registeringUsers=registeringUsers;
    }

    public GroupListAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_list_item_layout, parent, false);
        return new GroupListAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupListAdapter.ImageViewHolder holder, final int position) {
        ImageView group_list_item_image = holder.group_list_item_image;
        TextView group_list_item_name = holder.group_list_item_name;
        TextView group_list_item_creation_date = holder.group_list_item_creation_date;
        TextView group_list_item_current = holder.group_list_item_current;
        TextView group_list_item_limit = holder.group_list_item_limit;
        Button group_list_item_register = holder.group_list_item_register;
        RelativeLayout group_list_rel_layout = holder.group_list_rel_layout;
        Group item= items.get(position);

        FirebaseStorage.getInstance().getReference("group_image").child(item.getGroupImageName()).getDownloadUrl().
                addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseDatabase.getInstance().getReference("GROUP").child(item.getGroupUid()).child("groupMembers").addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            requestManager.load(uri).into(group_list_item_image);


                                        group_list_item_name.setText(item.getGroupName());
                                        group_list_item_creation_date.setText(item.getGroupCreationDate().split(" ")[0]);
                                        int currentMemSize = (int) snapshot.getChildrenCount();
                                        group_list_item_current.setText(currentMemSize+"");
                                        group_list_item_limit.setText(item.getGroupLimitSize()+"");
                                        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        if((item.getGroupMembers()!=null && item.getGroupMembers().contains(currentUid))
                                                || item.getGroupLeaderUid().equals(currentUid)
                                                || item.getGroupLimitSize() <=currentMemSize){
                                            group_list_item_register.setEnabled(false);
                                        }else{
                                            group_list_item_register.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    List<String> memberUids = item.getGroupMembers();
                                                    if(memberUids==null || memberUids.isEmpty()) { memberUids = new ArrayList<String>();}
                                                    memberUids.add(currentUid);
                                                    FirebaseDatabase.getInstance().getReference("GROUP").child(item.getGroupUid()).child("groupMembers").setValue(memberUids).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(context, "가입을 성공했습니다!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                }
                        );
                    }
                }
                );
        group_list_item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment ShowGroupFragment = new ShowGroupFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedGroup",item);
                ShowGroupFragment.setArguments(bundle);
                FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                fragmentManager.beginTransaction().add(R.id.tab_content_view, ShowGroupFragment).addToBackStack(null).commit();
            }
        });
        group_list_item_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment ShowGroupFragment = new ShowGroupFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedGroup",item);
                ShowGroupFragment.setArguments(bundle);
                FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                fragmentManager.beginTransaction().add(R.id.tab_content_view, ShowGroupFragment).addToBackStack(null).commit();
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
        private RelativeLayout group_list_rel_layout;
        public ImageViewHolder(View itemView) {
            super(itemView);
            group_list_item_image = itemView.findViewById(R.id.group_member_list_item_image);
            group_list_item_name = itemView.findViewById(R.id.group_member_list_item_name);
            group_list_item_creation_date = itemView.findViewById(R.id.group_list_item_creation_date);
            group_list_item_current = itemView.findViewById(R.id.apply_group_current);
            group_list_item_limit = itemView.findViewById(R.id.apply_group_limit);
            group_list_item_register = itemView.findViewById(R.id.group_list_item_delete);
            if(isLeader || isMember){
                group_list_item_register.setVisibility(View.INVISIBLE);
            }
            group_list_rel_layout = itemView.findViewById(R.id.group_list_rel_layout);
        }
    }

}
