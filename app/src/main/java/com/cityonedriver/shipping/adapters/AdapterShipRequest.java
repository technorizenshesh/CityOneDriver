package com.cityonedriver.shipping.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterShipingParcelsBinding;
import com.cityonedriver.shipping.activities.ShippDetailsActivity;
import com.cityonedriver.shipping.models.ModelShipRequest;

import java.util.ArrayList;

public class AdapterShipRequest extends RecyclerView.Adapter<AdapterShipRequest.StoreHolder> {

    Context mContext;
    ArrayList<ModelShipRequest.Result> storeList;

    public AdapterShipRequest(Context mContext, ArrayList<ModelShipRequest.Result> storeList) {
        this.mContext = mContext;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public AdapterShipRequest.StoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterShipingParcelsBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(mContext), R.layout.adapter_shiping_parcels,parent,false);
        return new AdapterShipRequest.StoreHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShipRequest.StoreHolder holder, int position) {
        ModelShipRequest.Result data = storeList.get(position);
        holder.binding.setParcel(data);
        holder.binding.btParcel.setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, ShippDetailsActivity.class)
                    .putExtra("parcelid",data.getId())
            );
        });
    }

    @Override
    public int getItemCount() {
        return storeList == null?0:storeList.size();
    }

    public class StoreHolder extends RecyclerView.ViewHolder{

        AdapterShipingParcelsBinding binding;

        public StoreHolder(AdapterShipingParcelsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}


