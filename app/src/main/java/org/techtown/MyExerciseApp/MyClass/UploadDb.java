package org.techtown.MyExerciseApp.MyClass;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class UploadDb {


    public UploadDb(FirebaseAuth auth, Context context,String FROM) {

            try{
                FirebaseUser user = auth.getCurrentUser();
                FirebaseStorage storage = FirebaseStorage.getInstance("gs://myexerciseapp-4e7d1.appspot.com");
                StorageReference storageRef = storage.getReference();
                StorageReference dbRef = storageRef.child("database/EXERCISE_"+user.getUid()+".db");
                InputStream inputStream = null;
                if(FROM.equals("assets")) {
                    inputStream = context.getAssets().open("EXERCISE.db");
                }
                if(FROM.equals("local")) {
                    inputStream = new FileInputStream(new File("/data/data/org.techtown.MyExerciseApp/databases/EXERCISE_"+
                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                            +".db"));
                }
                UploadTask uploadTask = dbRef.putStream(inputStream);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("업로드성공");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("업로드failure " + e.getMessage());
                    }
                });
            }catch (Exception e){}
        }
}
