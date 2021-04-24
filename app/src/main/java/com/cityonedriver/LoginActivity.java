package com.cityonedriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.databinding.ActivityLoginBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipReqActivity;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.taxi.activities.AddCarAct;
import com.cityonedriver.taxi.activities.TaxiHomeAct;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.MyService;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    Context mContext = LoginActivity.this;
    ActivityLoginBinding binding;
    SharedPref sharedPref;
    String registerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        sharedPref = SharedPref.getInstance(mContext);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (!task.isSuccessful()) {
                            return;
                        }

                        String token = task.getResult().getToken();
                        registerId = token;
                        Log.e("dsadasd","asdasd = " + registerId);
                    }

                });

        init();

    }

    private void init() {

        binding.tvForogtPassword.setOnClickListener(v -> {
            startActivity(new Intent(mContext,ForgotPasswordActivity.class));
        });

        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(mContext,SignUpActivity.class));
            finish();
        });

        binding.btLogin.setOnClickListener(v -> {
            if(TextUtils.isEmpty(binding.etEmail.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_email_add), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(binding.etPass.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_pass), Toast.LENGTH_SHORT).show();
            } else {
                loginApiCall();
            }
        });

    }

    private void loginApiCall() {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));

        HashMap<String,String> paramHash = new HashMap<>();
        paramHash.put("email",binding.etEmail.getText().toString().trim());
        paramHash.put("password",binding.etPass.getText().toString().trim());
        paramHash.put("lat","");
        paramHash.put("lon","");
        paramHash.put("register_id",registerId);

        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        Call<ResponseBody> call = api.loginApiCall(paramHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        ModelLogin modelLogin = new Gson().fromJson(responseString, ModelLogin.class);

                        sharedPref.setBooleanValue(AppConstant.IS_REGISTER,true);
                        sharedPref.setUserDetails(AppConstant.USER_DETAILS,modelLogin);

                        ContextCompat.startForegroundService(mContext,new Intent(getApplicationContext(), MyService.class));
                        if(AppConstant.RES_DRIVER.equals(modelLogin.getResult().getType())) {
                            startActivity(new Intent(mContext, StoreOrdersActivity.class));
                            finish();
                        } else if(AppConstant.SH_DRIVER.equals(modelLogin.getResult().getType())) {
                            startActivity(new Intent(mContext, ShipReqActivity.class));
                            finish();
                        } else if(AppConstant.TAXI_DRIVER.equals(modelLogin.getResult().getType())) {
                            if("0".equals(modelLogin.getResult().getCar_type_id())) {
                                startActivity(new Intent(mContext, AddCarAct.class));
                                finish();
                            } else {
                                startActivity(new Intent(mContext, TaxiHomeAct.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
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