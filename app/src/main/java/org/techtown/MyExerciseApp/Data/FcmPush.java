package org.techtown.MyExerciseApp.Data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmPush {

    MediaType json = MediaType.parse("application/json; charset=utf-8");
    String url = "https://fcm.googleapis.com/fcm/send";
    String serverKey = "AAAApuz7TL0:APA91bHbC8oIPAweOblA2dZE-D61inZNE8IwRigjYaqHLeCG6XmId7k-3a0wYnd4yq3FcO493NvHyMGPDy-Gt_ijOocDuzCLUAJPRkt_3wh4EnAbyTTxJUlxJKz5UkmdyjqZfvgRLPVv";
    Gson gson  = null;
    OkHttpClient okHttpClient  = null;
    private static FcmPush instance;

    private FcmPush(){
        gson = new Gson();
        okHttpClient = new OkHttpClient();
    }
    public static FcmPush getFcmInstance(){
        if(instance == null) {
            synchronized (FcmPush.class){
                instance = new FcmPush();
            }
        }
        return instance;
    }

    public void sendMessage(String destinationUid, String title, String message){
        FirebaseDatabase.getInstance().getReference("TOKEN").child(destinationUid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    PushNotification push = new PushNotification();
                    push.to = task.getResult().getValue(String.class);
                    push.notification.body=message;
                    push.notification.title=title;

                    RequestBody requestBody = RequestBody.create(json,gson.toJson(push));
                    Request request = new Request.Builder()
                            .addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key="+serverKey)
                            .url(url).post(requestBody).build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            System.out.println(response.body().toString());
                        }
                    });
                }
            }
        });
    }
}
