package org.techtown.MyExerciseApp.Adapter.Exercise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
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

import org.techtown.MyExerciseApp.Data.Exercise.RoutineItem;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Routine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RoutineListItemAdapter extends RecyclerView.Adapter<RoutineListItemAdapter.ViewHolder> {

    private List<RoutineItem> items = new ArrayList<>();
    private Routine item;
    private Context mContext;
    private HashMap<Integer, Integer> temp = new HashMap<>();
    private String from;
    private AppDatabase appdatabase;
    public void setAppdatabase(AppDatabase appDatabase){
        this.appdatabase = appDatabase;
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<RoutineItem> getItems() {
        return items;
    }

    public void setFrom(String from){
        this.from = from;
    }
    public String getFrom(){
        return this.from;
    }
    @NonNull
    @Override
    public RoutineListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_list_item, viewGroup, false);
        mContext = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.onBind(items.get(position), position);
        //Preload
        if (position <= items.size()) {
            int endPosition = (position + 6 > items.size()) ? items.size() : position + 6;
            List<RoutineItem> list = items.subList(position, endPosition);
            Context context = viewHolder.itemView.getRootView().getContext();
        }

    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position,RoutineItem routineItem);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private ImageView routineImage;
        private TextView listRoutineName;
        private TextView listRoutineLatestTime;
        private ImageButton  listRoutineFavoriteAddBt;
        private int index;

        public ViewHolder(View view) {
            super(view);
            routineImage = view.findViewById(R.id.routine_image);
            listRoutineName = view.findViewById(R.id.list_routine_name);
            listRoutineLatestTime = view.findViewById(R.id.list_routine_latest_time);

            listRoutineFavoriteAddBt = view.findViewById(R.id.list_routine_favorite_add_bt);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, position,items.get(position));
                        }
                    }
                }
            });
            view.setOnCreateContextMenuListener(this);
        }

        public void onBind(RoutineItem routineItem, int position) {
            index = position;
            Routine routine = routineItem.getRoutine();
            item = routine;
            listRoutineName.setText(routine.getRoName());
            listRoutineLatestTime.setText("최근 선택 : " + routine.getRoLatestTime() + " | " + "이용 횟수 : " + routine.getRoUsedCount());
            if(routineItem.getRoutine().getRoFavorite()==1){
                Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_on).into(listRoutineFavoriteAddBt);
                listRoutineFavoriteAddBt.setTag("favorite");
            }else{
                Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_off).into(listRoutineFavoriteAddBt);
                listRoutineFavoriteAddBt.setTag("notFavorite");
            }
            listRoutineFavoriteAddBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listRoutineFavoriteAddBt.getTag().toString().equals("favorite")){
                        Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_off).into(listRoutineFavoriteAddBt);
                        listRoutineFavoriteAddBt.setTag("notFavorite");
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                appdatabase.routineDao().updateFavorite(routineItem.getRoutine().getRoId(),0);
                            }
                        });
                    }else{
                        Glide.with(itemView.getRootView().getContext()).asDrawable().load(android.R.drawable.btn_star_big_on).into(listRoutineFavoriteAddBt);
                        listRoutineFavoriteAddBt.setTag("favorite");
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                appdatabase.routineDao().updateFavorite(routineItem.getRoutine().getRoId(),1);
                            }
                        });
                    }
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
            builder.setMessage("정말로 루틴을 삭제하시겠습니까 ?");
            builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    items.remove(position);
                    notifyDataSetChanged();
                    Runnable runnable = new Runnable(){
                        @Override
                        public void run() {
                            appdatabase.routineDao().deleteRoutine(item.getRoId());
                        }
                    };
                    Thread thread = new Thread(runnable);thread.start();
                    try{Thread.sleep(500);}
                    catch (InterruptedException e){e.printStackTrace();}

                    Toast.makeText(mContext, "루틴이 삭제 되었습니다", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void setItem(List<RoutineItem> data) {
        items = data;
        notifyDataSetChanged();
    }
}
