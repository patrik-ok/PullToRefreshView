package patrik.com.pullto.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import patrik.com.pullto.R;

/**
 * usage
 *
 * @author 骆胜华.
 * @date 2016/10/10.
 * ==============================================
 * Copyright (c) 2016 TRANSSION.Co.Ltd.
 * All rights reserved.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private Context mContext;
    private List<String> mDatas;
    public HomeAdapter(Context context, List<String> datas){
        mContext = context;
        if(datas!=null){
            this.mDatas = datas;
        }else{
            this.mDatas = new ArrayList<>();
        }
    }
    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HomeViewHolder holder = new HomeViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_main,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        holder.tv.setText(mDatas.get(position)+"");
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class HomeViewHolder extends RecyclerView.ViewHolder
    {

        TextView tv;

        public HomeViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.id_tv_home);
        }
    }
}
