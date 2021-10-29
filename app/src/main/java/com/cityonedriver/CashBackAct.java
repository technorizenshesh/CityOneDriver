package com.cityonedriver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.cityonedriver.adapters.AdapterPoints;
import com.cityonedriver.databinding.ActivityCashBackBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.models.ModelReferrals;
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

public class CashBackAct extends AppCompatActivity {

    Context mContext = CashBackAct.this;
    ActivityCashBackBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cash_back);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
        itit();
    }

    private void itit() {

        getAllPoints();

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.swipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllPoints();
            }
        });

    }

    private void getAllPoints() {
        ProjectUtil.showProgressDialog(mContext, false, getString(R.string.please_wait));

        HashMap<String, String> paramHash = new HashMap<>();
        paramHash.put("my_referral_code", modelLogin.getResult().getMy_referral_code());

        Log.e("my_referral_code","my_referral_code = " + modelLogin.getResult().getMy_referral_code());

        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        Call<ResponseBody> call = api.getReferralPOints(paramHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                try {
                    String stringResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(stringResponse);

                        if (jsonObject.getString("status").equals("1")) {

                            Log.e("asfddasfasdf", "response = " + stringResponse);
                            ModelReferrals modelReferrals = new Gson().fromJson(stringResponse, ModelReferrals.class);

                            AdapterPoints adapterPoints = new AdapterPoints(mContext, modelReferrals.getResult());
                            binding.rvPoints.setAdapter(adapterPoints);

                            binding.tvPoints.setText("You have " + modelReferrals.getReferral_code_point() + " points");

                        } else {
                            binding.tvPoints.setText("You have " + 0 + " points");
                        }

                    } catch (Exception e) {
                        binding.tvPoints.setText("You have " + 0 + "points");
                        Log.e("Exception", "Exception = " + e.getMessage());
                        e.printStackTrace();
                    }

                    // Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception", "Exception = " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
                Log.e("Exception", "Throwable = " + t.getMessage());
                binding.swipLayout.setRefreshing(false);
            }

        });
    }


}