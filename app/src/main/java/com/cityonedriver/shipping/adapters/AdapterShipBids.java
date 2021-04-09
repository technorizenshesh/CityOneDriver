package com.cityonedriver.shipping.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterShipBidsBinding;
import com.cityonedriver.databinding.BidDetailDialogBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.models.ModelShipBid;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterShipBids extends RecyclerView.Adapter<AdapterShipBids.MyBidsViewHolder> {

    Context mContext;
    ArrayList<ModelShipBid.Result> bidList;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    public AdapterShipBids(Context mContext, ArrayList<ModelShipBid.Result> bidList) {
        this.mContext = mContext;
        this.bidList = bidList;
        sharedPref  = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
    }

    @NonNull
    @Override
    public MyBidsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterShipBidsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext)
        ,R.layout.adapter_ship_bids,parent,false);
        return new MyBidsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBidsViewHolder holder, int position) {
        ModelShipBid.Result data = bidList.get(position);
        holder.binding.setBids(data);

        holder.binding.llRoot.setOnClickListener(v -> {
            if(modelLogin.getResult().getId().equals(data.getDriver_id())){
                openRemoveCancelBidDialog(data,position);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.you_are_not_allowed_to_see_other_bids), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openRemoveCancelBidDialog(ModelShipBid.Result data,int position) {
        Dialog dialog = new Dialog(mContext, WindowManager.LayoutParams.MATCH_PARENT);
        BidDetailDialogBinding dialogBinding = DataBindingUtil
                .inflate(LayoutInflater.from(mContext),R.layout.bid_detail_dialog,null,false);
        dialog.setContentView(dialogBinding.getRoot());

        Picasso.get().load(data.getImage())
                .placeholder(R.drawable.default_profile_icon)
                .error(R.drawable.default_profile_icon)
                .into(dialogBinding.bidUserProfile);

        dialogBinding.tvUsername.setText(modelLogin.getResult().getUser_name());
        dialogBinding.pickDate.setText(data.getPick_date());
        dialogBinding.dropDate.setText(data.getDrop_date());
        dialogBinding.comment.setText(data.getComment());

        dialogBinding.ivBack.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogBinding.btRemove.setOnClickListener(v -> {
            removeBidApi(data,position,dialog);
        });
        dialog.show();
    }

    private void removeBidApi(ModelShipBid.Result data, int position,Dialog dialog) {
        ProjectUtil.showProgressDialog(mContext,false,mContext.getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("bid_id",data.getId());

        Call<ResponseBody> call = api.removeBidApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String stringRes = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringRes);
                    if(jsonObject.getString("status").equals("1")) {
                        dialog.dismiss();
                        bidList.remove(position);
                        notifyDataSetChanged();
                    } else {

                    }
                } catch (Exception e) {

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
        return bidList == null?0:bidList.size();
    }

    public class MyBidsViewHolder extends RecyclerView.ViewHolder {

        AdapterShipBidsBinding binding;
        public MyBidsViewHolder(@NonNull AdapterShipBidsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}
