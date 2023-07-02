package org.techtown.MyExerciseApp.Adapter.Exercise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.ExerciseMemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ExerciseMemoListItemAdapter extends RecyclerView.Adapter<ExerciseMemoListItemAdapter.MemoViewHolder> {

    private LinkedList<ExerciseMemo> items = new LinkedList<>();
    private final RecyclerView recyclerView;
    private final ImageButton pre;
    private final ImageButton nxt;
    private Context mContext;
    private AppDatabase appDatabase;
    private final int exId;
    private int index;


    public ExerciseMemoListItemAdapter(int exId, RecyclerView recyclerView, ImageButton pre, ImageButton nxt, Button button) {
        this.exId = exId;
        this.recyclerView = recyclerView;
        this.pre = pre;
        this.nxt = nxt;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<ExerciseMemo> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ExerciseMemoListItemAdapter.MemoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.write_exercise_memo_item, viewGroup, false);
        mContext = viewGroup.getContext();
        appDatabase = AppDatabase.getDBInstance(mContext);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseMemoListItemAdapter.MemoViewHolder viewHolder, int position) {
        viewHolder.onBind(items.get(position), position);
    }

    public class MemoViewHolder extends RecyclerView.ViewHolder {

        private  TextView exerciseMemoToday;
        private  TextView exerciseMemoTitle;
        private  TextView exerciseMemoContent;
        private  Button delMemoBt;
        private  Button compMemoBt;


        public MemoViewHolder(View view) {
            super(view);
            exerciseMemoToday = view.findViewById(R.id.exercise_memo_today);
            exerciseMemoTitle = view.findViewById(R.id.exercise_memo_title);
            exerciseMemoContent = view.findViewById(R.id.exercise_memo_content);
            compMemoBt = view.findViewById(R.id.write_exercise_memo_complete_bt);
            delMemoBt = view.findViewById(R.id.write_exercise_memo_delete_bt);
        }

        public void onBind(ExerciseMemo memo, int position) {
            index = position;
            String writeTime = memo.getErimWriteTime();
            String title = memo.getErimTitle();
            String content = memo.getErimContent();

            exerciseMemoToday.setText(writeTime);
            exerciseMemoTitle.setText(title);
            exerciseMemoContent.setText(content);
            if (memo.getErimWriteTime().equals(getToday()) && memo.getErimTitle() == null) {
                delMemoBt.setVisibility(View.GONE);
            } else {
                delMemoBt.setVisibility(View.VISIBLE);
            }





            compMemoBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = exerciseMemoTitle.getText().toString();
                    String content = exerciseMemoContent.getText().toString();
                    //현재 보여지는 메모의 정보
                    if (title.isEmpty() || content.isEmpty()) {
                        //제목과 내용을 입력해주세요 알림창 띄우고 포커스 이동
                        showAlertSimpleMessage(mContext, "알림", "메모의 제목과 내용을 입력해 주세요 !");
                        if (title.isEmpty()) {
                            exerciseMemoTitle.requestFocus();
                        } else if (content.isEmpty()) {
                            exerciseMemoContent.requestFocus();
                        }
                    } else if (!title.isEmpty() && !content.isEmpty()) {
                        //데이터베이스에 오늘 날짜의 메모가 존재하면,
                        // 작성한 메모를 데이터베이스에 insert 이미 작성된 메모가 있으면 데이터베이스에 update
                        ExerciseMemo exerciseMemo = new ExerciseMemo(exId, title, content, writeTime);
                        //백그라운드 데이터베이스 접근
                        AsyncTask.execute(() -> appDatabase.exerciseMemoDao().insertMemo(exerciseMemo));
                        if (writeTime.equals(getToday())) {
                            items.add(0, exerciseMemo);

                        } else {
                            for (ExerciseMemo em : items) {
                                if (em.getErimWriteTime().equals(exerciseMemo.getErimWriteTime())) {
                                    em.setErimTitle(exerciseMemo.getErimTitle());
                                    em.setErimContent(exerciseMemo.getErimContent());
                                }
                            }
                        }
                        //메모를 저장했습니다. 알림창 띄우기
                        showAlertSimpleMessage(mContext, "알림", "메모를 저장했습니다 !");
                    }
                }
            });


            //메모 지우기
            delMemoBt.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    int oldPos = position;
                    //백그라운드 데이터베이스 접근
                    AsyncTask.execute(() -> appDatabase.exerciseMemoDao().deleteMemo(writeTime, exId));
                    //liveData에 설정된 arrayList 변경
                    items.removeIf(exerciseMemo -> exerciseMemo.getErimWriteTime().equals(writeTime));
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("알림").setMessage("메모를 삭제 하시겠습니까 ?")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    moveToExerciseMemoList(recyclerView, pre, nxt, oldPos);
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //nothing
                                }
                            });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.show();

                }
            });
        }

        //오늘의 날짜 스트링으로 반환
        private String getToday() {
            long now;
            Date date;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            now = System.currentTimeMillis();
            date = new Date(now);
            return simpleDateFormat.format(date);
        }

        //간단한 알림 메시지 다이어로그
        private void showAlertSimpleMessage(Context context, String title, String content) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle(title).setMessage(content);

            AlertDialog alertDialog = builder.create();

            alertDialog.show();
        }

        //간단한 알림 메시지 다이어로그
        private void showAlertOkNoMessage(Context context, String title, String content) {

        }

        private void moveToExerciseMemoList(RecyclerView recyclerView, ImageButton pre, ImageButton nxt, int oldPos) {
            int oldPosition = oldPos;
            int memoListSize = getItemCount();
            pre.setVisibility(View.VISIBLE);
            nxt.setVisibility(View.VISIBLE);

            if (memoListSize == 1) {
                pre.setVisibility(View.INVISIBLE);
                nxt.setVisibility(View.INVISIBLE);
            }
            int nextPosition = oldPosition - 1;
            if (oldPosition > 0) {
                recyclerView.scrollToPosition(nextPosition);
                if (nextPosition == 0) {
                    nxt.setVisibility(View.INVISIBLE);
                }
                if (nextPosition == getItemCount() - 1) {
                    pre.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    public void setItem(LinkedList<ExerciseMemo> data) {
        items = data;
        notifyDataSetChanged();
    }
}