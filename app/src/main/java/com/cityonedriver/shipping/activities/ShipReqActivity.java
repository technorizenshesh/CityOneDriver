package com.cityonedriver.shipping.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.LoginActivity;
import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityShipReqBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.adapters.AdapterShipRequest;
import com.cityonedriver.shipping.models.ModelShipRequest;
import com.cityonedriver.stores.activities.ChangePassActivity;
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

public class ShipReqActivity extends AppCompatActivity {

    Context mContext = ShipReqActivity.this;
    ActivityShipReqBinding binding;
    ModelShipRequest modelShipRequest;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_ship_req);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.navItems.setUser(modelLogin.getResult());
    }

    private void init() {

        binding.ivMenu.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.navItems.tvMyTransport.setOnClickListener(v -> {
            startActivity(new Intent(mContext, MyDeliveryShipActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        binding.navItems.tvMessages.setOnClickListener(v -> {
            startActivity(new Intent(mContext, ShipChatListActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        binding.navItems.tvLogout.setOnClickListener(v -> {
            logoutAppDialog();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        binding.navItems.tvChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(mContext, ChangePassActivity.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        getAllShipRequest();

        binding.swipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllShipRequest();
            }
        });

        binding.tvLogout.setOnClickListener(v -> {
            logoutAppDialog();
        });

    }

    private void logoutAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.logout_text))
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPref.clearAllPreferences();
                        Intent loginscreen = new Intent(mContext, LoginActivity.class);
                        loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        NotificationManager nManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
                        nManager.cancelAll();
                        startActivity(loginscreen);
                        finishAffinity();
                    }
                }).setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getAllShipRequest() {
        ProjectUtil.showProgressDialog(mContext,true,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        Log.e("sdfsdfdsfdsvf","user_id = " + modelLogin.getResult().getId());

        HashMap<String,String> param = new HashMap<>();
        param.put("user_id",modelLogin.getResult().getId());

        Call<ResponseBody> call = api.getAllShRequestApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                binding.swipLayout.setRefreshing(false);
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        modelShipRequest = new Gson().fromJson(responseString, ModelShipRequest.class);

                        AdapterShipRequest adapterShipRequest = new AdapterShipRequest(mContext,modelShipRequest.getResult());
                        binding.rvRequest.setAdapter(adapterShipRequest);

                        Log.e("sdfsdfdsfdsvf","responseString = " + responseString);

                    } else {
                        Toast.makeText(mContext, getString(R.string.no_request_found), Toast.LENGTH_SHORT).show();
                        AdapterShipRequest adapterShipRequest = new AdapterShipRequest(mContext,null);
                        binding.rvRequest.setAdapter(adapterShipRequest);
                        // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    //Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void exitAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.close_app_text))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitAppDialog();
    }

}