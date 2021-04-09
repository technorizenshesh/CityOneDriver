package com.cityonedriver.stores.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterStoreOrdersBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.stores.activities.OrderDetailsActivity;
import com.cityonedriver.stores.models.ModelStoreOrders;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterStoreOrders extends RecyclerView.Adapter<AdapterStoreOrders.StoreHolder> {

    Context mContext;
    ArrayList<ModelStoreOrders.Result> storeList;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    public AdapterStoreOrders(Context mContext, ArrayList<ModelStoreOrders.Result> storeList) {
        this.mContext = mContext;
        this.storeList = storeList;
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
    }

    @NonNull
    @Override
    public AdapterStoreOrders.StoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterStoreOrdersBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(mContext), R.layout.adapter_store_orders,parent,false);
        return new StoreHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterStoreOrders.StoreHolder holder, int position) {
        ModelStoreOrders.Result data = storeList.get(position);

        Log.e("sfdsfxdsfdsf","data.getStatus() = " + data.getStatus());

        if(!data.getStatus().equals("Pending")) {
            holder.binding.btAccept.setText(mContext.getString(R.string.order_details));
        } else {
            holder.binding.btAccept.setText(mContext.getString(R.string.accept));
        }

        if(data.getStatus().equals("Delivered")) {
            holder.binding.btAccept.setText(mContext.getString(R.string.delivered));
        }

        holder.binding.setOrder(data);

        holder.binding.btAccept.setOnClickListener(v -> {
           if(!data.getStatus().equals("Pending")) {
               mContext.startActivity(new Intent(mContext, OrderDetailsActivity.class)
                       .putExtra("data",data)
               );
           } else {
               if(!data.getStatus().equals("Delivered")) {
                   acceptOrderApi(data.getId(),position);
               }
           }
        });

    }

    private void acceptOrderApi(String id,int position) {

        ProjectUtil.showProgressDialog(mContext,false,mContext.getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> paramsHash = new HashMap<>();
        paramsHash.put("status","Accept");
        paramsHash.put("order_id",id);
        paramsHash.put("driver_id",modelLogin.getResult().getId());

        Call<ResponseBody> call = api.acceptStoreApiCall(paramsHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        Log.e("hfdsfsdfdsf","responseString = " + responseString);
                        storeList.get(position).setStatus("Accept");
                        notifyDataSetChanged();

                    } else {
                        // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    // Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Exception","Exception = " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
            }

        });


    }

    @Override
    public int getItemCount() {
        return storeList == null ? 0 : storeList.size();
    }

    public class StoreHolder extends RecyclerView.ViewHolder{

        AdapterStoreOrdersBinding binding;

        public StoreHolder(AdapterStoreOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}
