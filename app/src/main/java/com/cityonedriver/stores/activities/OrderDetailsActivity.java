package com.cityonedriver.stores.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityOrderDetailsBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.stores.models.ModelStoreOrders;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    Context mContext = OrderDetailsActivity.this;
    ActivityOrderDetailsBinding binding;
    ModelStoreOrders.Result data;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_order_details);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        data = (ModelStoreOrders.Result) getIntent().getSerializableExtra("data");
        binding.setDetails(data);

        init();

    }

    private void init() {

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        if("Pickup".equals(data.getStatus())) {
            binding.spUpdateStatus.setSelection(1);
        } else if("Delivered".equals(data.getStatus())) {
            binding.spUpdateStatus.setSelection(2);
        }

        binding.tvCustomerLoc.setOnClickListener(v -> {

            sharedPref = SharedPref.getInstance(mContext);
            modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

            Log.e("orginLatorginLat","modelLogin.getResult().getLat() = " + modelLogin.getResult().getLat());
            Log.e("orginLatorginLat","modelLogin.getResult().getLon() = " + modelLogin.getResult().getLon());
            Log.e("orginLatorginLat","modelLogin.getResult().getAddress() = " + modelLogin.getResult().getAddress());

            Log.e("orginLatorginLat","destiLat = " + data.getLat());
            Log.e("orginLatorginLat","destiLon = " + data.getLon());
            Log.e("orginLatorginLat","destiAdd = " + data.getAddress());

            startActivity(new Intent(mContext,StoreDirectionActivity.class)
            .putExtra("orginLat",modelLogin.getResult().getLat())
            .putExtra("originLon",modelLogin.getResult().getLon())
            .putExtra("originAdd",modelLogin.getResult().getAddress())
            .putExtra("destiLat",data.getLat())
            .putExtra("destiLon",data.getLon())
            .putExtra("destiAdd",data.getAddress())
            );
        });

        binding.tvStoreLoc.setOnClickListener(v -> {

            sharedPref = SharedPref.getInstance(mContext);
            modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

            Log.e("orginLatorginLat","modelLogin.getResult().getLat() = " + modelLogin.getResult().getLat());
            Log.e("orginLatorginLat","modelLogin.getResult().getLon() = " + modelLogin.getResult().getLon());
            Log.e("orginLatorginLat","modelLogin.getResult().getAddress() = " + modelLogin.getResult().getAddress());

            startActivity(new Intent(mContext,StoreDirectionActivity.class)
            .putExtra("orginLat",modelLogin.getResult().getLat())
            .putExtra("originLon",modelLogin.getResult().getLon())
            .putExtra("originAdd",modelLogin.getResult().getAddress())
            .putExtra("destiLat",data.getRestaurant_lat())
            .putExtra("destiLon",data.getRestaurant_lon())
            .putExtra("destiAdd",data.getRestaurant_address())
            );
        });

        binding.btUpdate.setOnClickListener(v -> {
            if(binding.spUpdateStatus.getSelectedItemPosition() == 0) {
                Toast.makeText(mContext, getString(R.string.please_update_dev_status), Toast.LENGTH_SHORT).show();
            } else {
                if(binding.spUpdateStatus.getSelectedItemPosition() == 1) {
                    acceptOrderApi("Pickup");
                } else {
                    acceptOrderApi("Delivered");
                }
            }
        });

    }

    private void acceptOrderApi(String status) {

        ProjectUtil.showProgressDialog(mContext,false,mContext.getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> paramsHash = new HashMap<>();
        paramsHash.put("status",status);
        paramsHash.put("order_id",data.getId());
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
                        finishAffinity();
                        startActivity(new Intent(mContext,StoreOrdersActivity.class));

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

}