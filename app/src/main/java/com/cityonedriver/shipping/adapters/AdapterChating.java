package com.cityonedriver.shipping.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterChatListBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipChatingActivity;
import com.cityonedriver.shipping.models.ModelChatList;
import com.cityonedriver.shipping.models.ModelChating;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.BottomReachedInterface;
import com.cityonedriver.utils.SharedPref;

import java.util.ArrayList;

public class AdapterChating extends RecyclerView.Adapter<AdapterChating.StoreHolder> {

    Context mContext;
    ArrayList<ModelChating.Result> chatList;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    BottomReachedInterface bottomReachedInterface;

    public AdapterChating(Context mContext, ArrayList<ModelChating.Result> chatList) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.bottomReachedInterface = (BottomReachedInterface) mContext;
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
    }

    @NonNull
    @Override
    public AdapterChating.StoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_ship_chat,parent,false);
        return new StoreHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterChating.StoreHolder holder, int position) {

        ModelChating.Result data = chatList.get(position);

        Log.e("zjsdgfjksdgjfgkjsdf","position = " + position);
        Log.e("zjsdgfjksdgjfgkjsdf",data.getChat_message());

        if(chatList.size()-1 == position) {
            bottomReachedInterface.isBottomReached(true);
        } else {
            bottomReachedInterface.isBottomReached(false);
        }

        if(modelLogin.getResult().getId().equals(data.getSender_id())) {
            holder.llRightChat.setVisibility(View.VISIBLE);
            holder.llLeftChat.setVisibility(View.GONE);
            holder.tvRightChat.setText(data.getChat_message());
            holder.tvRightDate.setText(data.getDate());
        } else {
            holder.llLeftChat.setVisibility(View.VISIBLE);
            holder.llRightChat.setVisibility(View.GONE);
            holder.tvLeftChat.setText(data.getChat_message());
            holder.tvLeftDate.setText(data.getDate());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return chatList == null?0:chatList.size();
    }

    public class StoreHolder extends RecyclerView.ViewHolder{

        TextView tvLeftChat,tvLeftDate,tvRightChat,tvRightDate;
        LinearLayout llLeftChat,llRightChat;

        public StoreHolder(@NonNull View itemView) {
            super(itemView);
            tvLeftChat = itemView.findViewById(R.id.tvLeftChat);
            tvLeftDate = itemView.findViewById(R.id.tvLeftDate);
            tvRightChat = itemView.findViewById(R.id.tvRightChat);
            tvRightDate = itemView.findViewById(R.id.tvRightDate);
            llLeftChat = itemView.findViewById(R.id.llLeftChat);
            llRightChat = itemView.findViewById(R.id.llRightChat);
        }

    }


}


