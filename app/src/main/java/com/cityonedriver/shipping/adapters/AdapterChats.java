package com.cityonedriver.shipping.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityShipChatingBinding;
import com.cityonedriver.databinding.AdapterChatListBinding;
import com.cityonedriver.databinding.AdapterShipingParcelsBinding;
import com.cityonedriver.shipping.activities.ShipChatingActivity;
import com.cityonedriver.shipping.activities.ShippDetailsActivity;
import com.cityonedriver.shipping.models.ModelChatList;
import com.cityonedriver.shipping.models.ModelShipRequest;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.StoreHolder> {

    Context mContext;
    ArrayList<ModelChatList.Result> storeList;

    public AdapterChats(Context mContext, ArrayList<ModelChatList.Result> storeList) {
        this.mContext = mContext;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public AdapterChats.StoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterChatListBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(mContext), R.layout.adapter_chat_list,parent,false);
        return new AdapterChats.StoreHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterChats.StoreHolder holder, int position) {

        ModelChatList.Result data = storeList.get(position);

        holder.binding.setChat(data);

        holder.binding.cvChat.setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, ShipChatingActivity.class)
                    .putExtra("sender_id",data.getSender_id())
                    .putExtra("receiver_id",data.getReceiver_id())
                    .putExtra("name",data.getUser_name())
            );
        });

    }

    @Override
    public int getItemCount() {
        return storeList == null?0:storeList.size();
    }

    public class StoreHolder extends RecyclerView.ViewHolder{

        AdapterChatListBinding binding;

        public StoreHolder(AdapterChatListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}


