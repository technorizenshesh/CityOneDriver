package com.cityonedriver.taxi.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.cityonedriver.R;
import com.cityonedriver.databinding.AdapterTripHistoryBinding;
import com.cityonedriver.databinding.TaxiHistoryPaymentSummDialogBinding;
import com.cityonedriver.databinding.TaxiPaymentDialogBinding;
import com.cityonedriver.taxi.activities.TaxiHomeAct;
import com.cityonedriver.taxi.activities.TrackTaxiAct;
import com.cityonedriver.taxi.models.ModelTaxiBookingDetail;
import com.cityonedriver.taxi.models.ModelTripHistory;
import com.cityonedriver.utils.AppConstant;

import java.util.ArrayList;

public class AdapterTripHistory extends RecyclerView.Adapter<AdapterTripHistory.MyTripView> {

    Context mContext;
    ArrayList<ModelTripHistory.Result> tripHisList;
    String status;

    public AdapterTripHistory(Context mContext, ArrayList<ModelTripHistory.Result> tripHisList,String status) {
        this.mContext = mContext;
        this.tripHisList = tripHisList;
        this.status = status;
    }

    @NonNull
    @Override
    public MyTripView onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {
        AdapterTripHistoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext)
                , R.layout.adapter_trip_history,parent,false);
        return new MyTripView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTripView holder, int position) {

        ModelTripHistory.Result data = tripHisList.get(position);

        holder.binding.tvDateTime.setText(data.getPicklaterdate());
        holder.binding.tvFrom.setText(data.getPicuplocation());
        holder.binding.tvDestination.setText(data.getDropofflocation());
        holder.binding.tvStatus.setText(data.getStatus());

        if("Finish".equals(status)) {
            holder.binding.btPaySum.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btPaySum.setVisibility(View.GONE);
        }

        holder.binding.GoDetail.setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext,TrackTaxiAct.class)
            .putExtra("request_id",data.getId())
            );
        });

        holder.binding.btPaySum.setOnClickListener(v -> {
            openPaymentSummaryDialog(data);
        });

    }

    private void openPaymentSummaryDialog(ModelTripHistory.Result data) {

        Dialog dialog = new Dialog(mContext, WindowManager.LayoutParams.MATCH_PARENT);
        TaxiHistoryPaymentSummDialogBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext)
                ,R.layout.taxi_history_payment_summ_dialog,null,false);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.setPayment(data);
        dialogBinding.tvEstiPrice.setText(AppConstant.DOLLAR + data.getEstimate_charge_amount());

        dialogBinding.ivBack.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogBinding.btOk.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();

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
        return tripHisList == null?0:tripHisList.size();
    }

    public class MyTripView extends RecyclerView.ViewHolder {

        AdapterTripHistoryBinding binding;
        public MyTripView(@NonNull AdapterTripHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }


}
