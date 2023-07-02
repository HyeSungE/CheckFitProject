package org.techtown.MyExerciseApp.Adapter.Mypage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.techtown.MyExerciseApp.Data.Mypage.MypageBodypartRank;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;

public class BodypartRankItemAdapter extends BaseAdapter {

    private ArrayList<MypageBodypartRank> items;
    private Context context;
    LayoutInflater mLayoutInflater = null;
    public BodypartRankItemAdapter(Context context,ArrayList<MypageBodypartRank> items) {
        this.context = context;
        this.items = items;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View converView, ViewGroup viewGroup) {
        View view = mLayoutInflater.inflate(R.layout.mypage_rank_list_item_layout, null);

        TextView mypage_list_item_rank_number = (TextView)view.findViewById(R.id.mypage_list_item_rank_number);
        TextView mypage_list_item_rank_name = (TextView)view.findViewById(R.id.mypage_list_item_rank_name);
        TextView mypage_list_item_rank_value = (TextView)view.findViewById(R.id.mypage_list_item_rank_value);
        MypageBodypartRank item = items.get(i);
        mypage_list_item_rank_number.setText(item.getRank()+".");
        if(item.getValue() == 0) mypage_list_item_rank_number.setText("-.");
        mypage_list_item_rank_name.setText(item.getBodypartName());
        mypage_list_item_rank_value.setText(item.getValue()+"회");


        return view;
    }

    public void setItems(ArrayList<MypageBodypartRank> mypageBodypartRanks) {
        this.items = mypageBodypartRanks;
        notifyDataSetChanged();
    }
}
