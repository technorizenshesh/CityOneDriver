package com.cityonedriver.shipping.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityMyTransportBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.adapters.AdapterMyTransport;
import com.cityonedriver.shipping.adapters.AdapterShipRequest;
import com.cityonedriver.shipping.models.ModelMyTransports;
import com.cityonedriver.shipping.models.ModelShipRequest;
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

public class MyDeliveryShipActivity extends AppCompatActivity {

    Context mContext = MyDeliveryShipActivity.this;
    ActivityMyTransportBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    ModelMyTransports modelShipRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_transport);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        init();

    }

    private void init() {

        binding.swipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyTransport();
            }
        });

        getMyTransport();

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getIntent() != null) {
                getMyTransportNew();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter("devship"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void getMyTransportNew(){
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("user_id",modelLogin.getResult().getId());

        Call<ResponseBody> call = api.getMyTransportApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        modelShipRequest = new Gson().fromJson(responseString, ModelMyTransports.class);

                        AdapterMyTransport adapterShipRequest = new AdapterMyTransport(mContext,modelShipRequest.getResult());
                        binding.rvRequest.setAdapter(adapterShipRequest);

                        Log.e("responseStringdsfs","responseString = " + responseString);

                    } else {
                        Toast.makeText(mContext, getString(R.string.no_request_found), Toast.LENGTH_SHORT).show();
                        AdapterShipRequest adapterShipRequest = new AdapterShipRequest(mContext,null);
                        binding.rvRequest.setAdapter(adapterShipRequest);
                        // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    binding.swipLayout.setRefreshing(false);
                    Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void getMyTransport() {
        ProjectUtil.showProgressDialog(mContext,true,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("user_id",modelLogin.getResult().getId());

        Call<ResponseBody> call = api.getMyTransportApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        modelShipRequest = new Gson().fromJson(responseString, ModelMyTransports.class);

                        AdapterMyTransport adapterShipRequest = new AdapterMyTransport(mContext,modelShipRequest.getResult());
                        binding.rvRequest.setAdapter(adapterShipRequest);

                        Log.e("responseStringdsfs","responseString = " + responseString);

                    } else {
                        Toast.makeText(mContext, getString(R.string.no_request_found), Toast.LENGTH_SHORT).show();
                        AdapterShipRequest adapterShipRequest = new AdapterShipRequest(mContext,null);
                        binding.rvRequest.setAdapter(adapterShipRequest);
                        // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    binding.swipLayout.setRefreshing(false);
                    Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
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