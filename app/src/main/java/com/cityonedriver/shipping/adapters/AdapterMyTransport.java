package com.cityonedriver.shipping.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterMyTransportBinding;
import com.cityonedriver.databinding.BidDetailDialogBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipChatingActivity;
import com.cityonedriver.shipping.activities.ShippDetailsActivity;
import com.cityonedriver.shipping.models.ModelMyTransports;
import com.cityonedriver.shipping.models.ModelShipBid;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterMyTransport extends RecyclerView.Adapter<AdapterMyTransport.MyBidsViewHolder> {

    Context mContext;
    ArrayList<ModelMyTransports.Result> bidList;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    public AdapterMyTransport(Context mContext, ArrayList<ModelMyTransports.Result> bidList) {
        this.mContext = mContext;
        this.bidList = bidList;
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
    }

    @NonNull
    @Override
    public AdapterMyTransport.MyBidsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterMyTransportBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext)
                , R.layout.adapter_my_transport,parent,false);
        return new AdapterMyTransport.MyBidsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMyTransport.MyBidsViewHolder holder, int position) {
        ModelMyTransports.Result data = bidList.get(position);

        holder.binding.setParcel(data);

        holder.binding.btParcel.setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, ShippDetailsActivity.class)
               .putExtra("parcelid",data.getShipping_id())
               .putExtra("type",true)
            );
        });

        if(data.getBid_status().equals("Accept")) {
            holder.binding.llUpdate.setVisibility(View.VISIBLE);
            holder.binding.btChat.setVisibility(View.VISIBLE);
            holder.binding.tvStatusText.setText(mContext.getString(R.string.update_pickup_text));
        } else if(data.getBid_status().equals("Pickup")) {
            holder.binding.btChat.setVisibility(View.VISIBLE);
            holder.binding.llUpdate.setVisibility(View.VISIBLE);
            holder.binding.tvStatusText.setText(mContext.getString(R.string.update_delivered_text));
        } else if(data.getBid_status().equals("Delivered")) {
            holder.binding.llUpdate.setVisibility(View.GONE);
            holder.binding.btChat.setVisibility(View.VISIBLE);
        } else if(data.getBid_status().equals("Cancel")) {
            holder.binding.btChat.setVisibility(View.GONE);
            holder.binding.llUpdate.setVisibility(View.GONE);
        } else if(data.getBid_status().equals("Pending")) {
            holder.binding.btChat.setVisibility(View.GONE);
            holder.binding.llUpdate.setVisibility(View.GONE);
        }

        holder.binding.btChat.setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, ShipChatingActivity.class)
                    .putExtra("sender_id",modelLogin.getResult().getId())
                    .putExtra("receiver_id",data.getUser_id())
                    .putExtra("name",data.getUser_name())
            );
        });

        holder.binding.btUpdate.setOnClickListener(v -> {
            if(data.getBid_status().equals("Accept")) {
                updateStatusByDricer("Pickup",data.getId(),data);
            } else if(data.getBid_status().equals("Pickup")) {
                updateStatusByDricer("Delivered",data.getId(),data);
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void updateStatusByDricer(String status,String bidId,ModelMyTransports.Result data) {
        ProjectUtil.showProgressDialog(mContext,false,mContext.getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("status",status);
        param.put("bid_id",bidId);

        Call<ResponseBody> call = api.driverShipStatusChangeApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String stringRes = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringRes);
                    if(jsonObject.getString("status").equals("1")) {
                        Log.e("stringRes","stringRes = " + stringRes);
                        JSONObject jsonResult = jsonObject.getJSONObject("result");
                        data.setBid_status(jsonResult.getString("status"));
                        notifyDataSetChanged();
                    } else {}
                } catch (Exception e) {}
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
            }
        });
    }

    private void openRemoveCancelBidDialog(ModelShipBid.Result data, int position) {
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
            dialog.dismiss();
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

        AdapterMyTransportBinding binding;

        public MyBidsViewHolder(@NonNull AdapterMyTransportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}
