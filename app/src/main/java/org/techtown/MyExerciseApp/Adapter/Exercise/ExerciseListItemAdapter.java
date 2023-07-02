package org.techtown.MyExerciseApp.Adapter.Exercise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExerciseListItemAdapter extends RecyclerView.Adapter<ExerciseListItemAdapter.ViewHolder> {

    private List<Exercise> items = new ArrayList<>();
    private Context mContext;
    private SparseBooleanArray[] selectedExerciseArr;
    private int tabPosition;
    private HashMap<Integer, Integer> temp = new HashMap<>();
    private String mode="";
    private AppDatabase appdatabase;
    private Exercise e;


    public ExerciseListItemAdapter(SparseBooleanArray[] selectedExerciseArr) {
        this.selectedExerciseArr = selectedExerciseArr.clone();

    }
    public void setItem(List<Exercise> data, int tabPosition) {
        items = data;
        this.tabPosition = tabPosition;
        notifyDataSetChanged();
    }
    private int bodypartToTabPosition(String bodypart) {
        int num = 0;
        switch (bodypart) {
            case "가슴":
                num = 1;
                break;
            case "어깨":
                num = 2;
                break;
            case "등":
                num = 3;
                break;
            case "하체":
                num = 4;
                break;
            case "팔":
                num = 5;
                break;
            case "유산소":
                num = 6;
                break;
            case "복근":
                num = 7;
                break;
        }
        ;
        return num;
    }
    public void clearSearchExerciseList() {
        this.selectedExerciseArr[8].clear();
    }
    public void setTemp(HashMap<Integer, Integer> temp) {
        if(!this.temp.isEmpty()) this.temp.clear();
        this.temp.putAll(temp);
    }
    public HashMap<Integer, Integer> getTemp() {
        return temp;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Exercise> getItems() {
        return items;
    }

    public void setAppdatabase(AppDatabase appDatabase){
        this.appdatabase = appDatabase;
    }
    @NonNull
    @Override
    public ExerciseListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.exercise_list_item, viewGroup, false);
        mContext = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.onBind(items.get(position), position);
        if(!mode.equals("home") && !mode.equals("mypage")){
            if (isItemSelected(position))
                viewHolder.itemView.setBackgroundResource(R.drawable.exercise_list_select_border);
            else viewHolder.itemView.setBackgroundResource(R.drawable.exercise_list_non_select_border);
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.exercise_list_non_select_border);
        }
        //Preload
        if (position <= items.size()) {
            int endPosition = (position + 6 > items.size()) ? items.size() : position + 6;
            List<Exercise> list = items.subList(position, endPosition);
            Context context = viewHolder.itemView.getRootView().getContext();
            for (Exercise exercise : list) {
                preload(context, exercise.getExImageUrl());
            }

        }

    }

    private void preload(Context context, String url) {
        float w = context.getResources().getDimensionPixelSize(R.dimen.exerciseListImageWidth);
        float h = context.getResources().getDimensionPixelSize(R.dimen.exerciseListImageHeight);
        Glide.with(context).load(url).preload((int) w, (int) h);
    }

    private boolean isItemSelected(int position) {
        Exercise exercise = items.get(position);
        int exerciseTabPosition =tabPosition;
        int exercisePosition = -1;
        if(exercise != null) {exercisePosition = temp.get(exercise.getExId());}
        return selectedExerciseArr[exerciseTabPosition].get(exercisePosition, false);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
        void onInfoClick(Exercise exercise, int position);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

        private TextView exerciseName;
        private TextView exerciseFrequency;
        private ImageView exerciseImage;
        private ImageButton list_exercise_favorite_add_bt,list_exercise_info_bt;
        private int index;

        public ViewHolder(View view) {
            super(view);
            exerciseName = view.findViewById(R.id.list_exercise_name);
            exerciseFrequency = view.findViewById(R.id.list_exercise_frequency);
            exerciseImage = view.findViewById(R.id.exercise_image);
            list_exercise_favorite_add_bt = view.findViewById(R.id.list_exercise_favorite_add_bt);
            list_exercise_info_bt = view.findViewById(R.id.list_exercise_info_bt);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, position);
                        }
                    }
                    toggleItemSelected(position);
                }
            });
            view.setOnCreateContextMenuListener(this);
        }

        private void toggleItemSelected(int position) {
            Exercise exercise = items.get(position);
            int exerciseTabPosition = bodypartToTabPosition(exercise.getExBodypart());
            int exercisePosition = temp.get(exercise.getExId());

            if (selectedExerciseArr[exerciseTabPosition].get(exercisePosition, false)) {
                selectedExerciseArr[exerciseTabPosition].delete(exercisePosition);
                notifyItemChanged(position);
            } else {
                selectedExerciseArr[exerciseTabPosition].put(exercisePosition, true);
                notifyItemChanged(position);
            }
        }

        public void onBind(Exercise exercise, int position) {
            index = position;
            e = exercise;
            exerciseName.setText(exercise.getExName());
            exerciseFrequency.setText(String.valueOf(exercise.getExFrequency()));
            if(exercise.getExFavorite()==1){
                Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_on).into(list_exercise_favorite_add_bt);
                list_exercise_favorite_add_bt.setTag("favorite");
            }else{
                Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_off).into(list_exercise_favorite_add_bt);
                list_exercise_favorite_add_bt.setTag("notFavorite");
            }

            Glide.with(itemView.getRootView().getContext()).asBitmap()
                    .load(exercise.getExImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(exerciseImage);

            list_exercise_favorite_add_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(list_exercise_favorite_add_bt.getTag().toString().equals("favorite")){
                        Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_off).into(list_exercise_favorite_add_bt);
                        list_exercise_favorite_add_bt.setTag("notFavorite");
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                appdatabase.exerciseDao().updateFavorite(exercise.getExId(),0);
                            }
                        });
                    }else{
                        Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_on).into(list_exercise_favorite_add_bt);
                        list_exercise_favorite_add_bt.setTag("favorite");
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                appdatabase.exerciseDao().updateFavorite(exercise.getExId(),1);
                            }
                        });
                    }
                }
            });
            list_exercise_info_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onInfoClick(exercise,position);
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add("삭제").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showConfirmationDialog(position);
                    }
                    return true;
                }
            });
        }

        private void showConfirmationDialog(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("운동 삭제");
            builder.setMessage("정말로 운동을 삭제하시겠습니까 ?");
            builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    items.remove(position);
                    notifyItemRemoved(position);
                    Runnable runnable = new Runnable(){
                        @Override
                        public void run() {
                            appdatabase.exerciseDao().deleteExercise(e.getExId());
                        }
                    };
                    Thread thread = new Thread(runnable);thread.start();
                    try{Thread.sleep(500);}
                    catch (InterruptedException e){e.printStackTrace();}

                    Toast.makeText(mContext, "운동이 삭제 되었습니다", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void setMode(String mode){
        this.mode = mode;
    }

}
