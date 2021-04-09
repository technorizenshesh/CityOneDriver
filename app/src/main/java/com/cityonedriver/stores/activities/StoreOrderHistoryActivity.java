package com.cityonedriver.stores.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityStoreOrderHistoryBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.stores.adapters.AdapterStoreOrders;
import com.cityonedriver.stores.models.ModelStoreOrders;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreOrderHistoryActivity extends AppCompatActivity {

    Context mContext = StoreOrderHistoryActivity.this;
    ActivityStoreOrderHistoryBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_store_order_history);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        init();

    }

    private void init() {

        binding.swipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStpreOrders();
            }
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        getStpreOrders();

    }

    private void getStpreOrders() {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> paramsHash = new HashMap<>();
        paramsHash.put("user_id",modelLogin.getResult().getId());

        Call<ResponseBody> call = api.deliveredApiCall(paramsHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        ModelStoreOrders modelStoreOrders = new Gson().fromJson(responseString, ModelStoreOrders.class);

                        AdapterStoreOrders adapterStoreOrders = new AdapterStoreOrders(mContext,modelStoreOrders.getResult());
                        binding.rvOrders.setAdapter(adapterStoreOrders);

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
                binding.swipLayout.setRefreshing(false);
            }

        });

    }

}