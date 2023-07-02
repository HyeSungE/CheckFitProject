package org.techtown.MyExerciseApp.MyClass;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Interface.GetUserListener;

public class GetUser {
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private FirebaseUser user;
    private final User currentUser = null;
    private String otherUid;
    private String uid, nickname;

    public GetUser(String mode, String str) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (mode.equals("uid")) {
            this.uid = str;
        } else if (mode.equals("nickname")) {
            this.nickname = str;
        }


    }

    public GetUser() {

    }

    public User getUserByUid(final GetUserListener getUserListener) {
        reference = FirebaseDatabase.getInstance().getReference().child("USER").child(uid);
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    User u = task.getResult().getValue(User.class);
                    getUserListener.getUserLoaded(u);
                }
            }
        });

//        reference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User u = snapshot.getValue(User.class);
//                getUserListener.getUserLoaded(u);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (task.isSuccessful()) {
//                    User u = task.getResult().getValue(User.class);
//                    Log.d("getByUid1", "1");
//                    getUserListener.getUserLoaded(u);
//                }
//            }
//        });
        return currentUser;
    }



//    public void getUidByNickname(final GetUserListener getUserListener) {
//
//        reference = FirebaseDatabase.getInstance().getReference().child("NICKNAME").child(nickname);
//        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (task.isSuccessful()) {
//                    String currentUserUid = task.getResult().getValue(String.class);
//                    FirebaseDatabase.getInstance().getReference().child("USER").child(currentUserUid).get().addOnCompleteListener(
//                            new OnCompleteListener<DataSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                    if (task.isSuccessful()){
//                                        User u = task.getResult().getValue(User.class);
//                                        getUserListener.getUserLoaded(u);
//                                    }
//                                }
//                            }
//                    );
//                }
//            }
//        });
//    }

    public void getUidByNickname(final GetUserListener getUserListener) {
        Query query = FirebaseDatabase.getInstance().getReference().child("USER").orderByChild("nickname").equalTo(nickname);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()){
                    Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 없음");
                    getUserListener.getUserLoaded(null);

                }else{
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User u = dataSnapshot.getValue(User.class);
                        if(u!=null){
                            getUserListener.getUserLoaded(u);
                            Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 있음");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("getUidByNickname", "onCancelled 데이터없음");
            }
        });
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(!snapshot.hasChildren()){
//                    Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 없음");
//                    getUserListener.getUserLoaded(null);
//
//                }else{
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        User u = dataSnapshot.getValue(User.class);
//                        if(u!=null){
//                            getUserListener.getUserLoaded(u);
//                            Log.d("getUidByNickname", "onDataChange 해당하는 닉네임을 가진 유저 있음");
//                            break;
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d("getUidByNickname", "onCancelled 데이터없음");
//            }
//        });

    }


//    public String getUidByNickname(String nickname) {
//        reference = FirebaseDatabase.getInstance().getReference().child("USER").child(nickname);
//        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if(task.isSuccessful()){
//                    otherUid = task.getResult().getValue(String.class);
//                }
//            }
//        });
//        if(otherUid == null) return null;
//        else return getUserByUid(otherUid);
//    }


}
