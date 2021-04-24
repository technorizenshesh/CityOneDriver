package com.cityonedriver.taxi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityTripHistoryBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.taxi.adapters.AdapterTripHistory;
import com.cityonedriver.taxi.models.ModelTripHistory;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripHistoryAct extends AppCompatActivity {

    Context mContext = TripHistoryAct.this;
    ActivityTripHistoryBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    int position;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_trip_history);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
        
        init();
        
    }

    private void init() {
        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        getActiveBookings();

        TabLayout.Tab activeTab = binding.tabLayout.newTab();
        activeTab.setText("Active");
        binding.tabLayout.addTab(activeTab);

        TabLayout.Tab finishTab = binding.tabLayout.newTab();
        finishTab.setText("Finished");
        binding.tabLayout.addTab(finishTab);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    position = 1;
                    getActiveBookings();
                } else {
                    position = 1;
                    getFinishedBookings();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.swipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(position == 0) {
                    getActiveBookings();
                } else if(position == 1) {
                    getFinishedBookings();
                }
            }
        });

    }

    private void getActiveBookings() {
        ProjectUtil.showProgressDialog(mContext,false, getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        Log.e("sfddsfdsfdsf","user_id = " + modelLogin.getResult().getId());

        HashMap<String,String> params = new HashMap<>();
        params.put("user_id",modelLogin.getResult().getId());
        params.put("type","DRIVER");

        Log.e("hjadkjshakjdhkjas","params = " + params);

        Call<ResponseBody> call = api.getActiveBooking(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);

                Log.e("kghkljsdhkljf","response = " + response);

                try {
                    String stringResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringResponse);

                    Log.e("kjagsdkjgaskjd","stringResponse = " + response);
                    Log.e("kjagsdkjgaskjd","stringResponse = " + stringResponse);

                    if (jsonObject.getString("status").equals("1")) {

                        ModelTripHistory modelTripHistory = new Gson().fromJson(stringResponse,ModelTripHistory.class);

                        AdapterTripHistory adapterTripHistory = new AdapterTripHistory(mContext,modelTripHistory.getResult(),"Active");
                        binding.rvHistory.setAdapter(adapterTripHistory);
                    } else {
                        AdapterTripHistory adapterTripHistory = new AdapterTripHistory(mContext,null,"Active");
                        binding.rvHistory.setAdapter(adapterTripHistory);
                        Toast.makeText(mContext, getString(R.string.no_trip_avail), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                Log.e("kjagsdkjgaskjd","stringResponse = " + t.getMessage());
            }

        });
    }

    private void getFinishedBookings() {
        ProjectUtil.showProgressDialog(mContext,false, getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        Log.e("sfddsfdsfdsf","user_id = " + modelLogin.getResult().getId());

        HashMap<String,String> params = new HashMap<>();
        params.put("user_id",modelLogin.getResult().getId());
        params.put("type","DRIVER");

        Log.e("hjadkjshakjdhkjas","params = " + params);

        Call<ResponseBody> call = api.getFinishedBooking(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);

                Log.e("kghkljsdhkljf","response = " + response);

                try {
                    String stringResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringResponse);

                    Log.e("kjagsdkjgaskjd","stringResponse = " + response);
                    Log.e("kjagsdkjgaskjd","stringResponse = " + stringResponse);

                    if (jsonObject.getString("status").equals("1")) {

                        ModelTripHistory modelTripHistory = new Gson().fromJson(stringResponse,ModelTripHistory.class);

                        AdapterTripHistory adapterTripHistory = new AdapterTripHistory(mContext,modelTripHistory.getResult(),"Finish");
                        binding.rvHistory.setAdapter(adapterTripHistory);
                    } else {
                        AdapterTripHistory adapterTripHistory = new AdapterTripHistory(mContext,null,"Finish");
                        binding.rvHistory.setAdapter(adapterTripHistory);
                        Toast.makeText(mContext, getString(R.string.no_trip_avail), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                Log.e("kjagsdkjgaskjd","stringResponse = " + t.getMessage());
            }

        });
    }

}