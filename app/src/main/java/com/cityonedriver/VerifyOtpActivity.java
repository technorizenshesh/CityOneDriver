package com.cityonedriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.cityonedriver.databinding.ActivityVerifyOtpBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipReqActivity;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.MyService;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class VerifyOtpActivity extends AppCompatActivity {

    Context mContext = VerifyOtpActivity.this;
    ActivityVerifyOtpBinding binding;
    String mobile="";
    String id;
    HashMap<String,String> paramHash;
    HashMap<String,String> fileParamHash;
    private FirebaseAuth mAuth;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_verify_otp);
        sharedPref = SharedPref.getInstance(mContext);
        mAuth = FirebaseAuth.getInstance();

        paramHash = (HashMap<String, String>) getIntent().getSerializableExtra("resgisterHashmap");
        fileParamHash = (HashMap<String, String>) getIntent().getSerializableExtra("fileHashmap");
        mobile = getIntent().getStringExtra("mobile");

        init();

        sendVerificationCode();

    }

    private void init() {

        binding.next.setOnClickListener(v -> {
            if(TextUtils.isEmpty(binding.etOtp.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_otp), Toast.LENGTH_SHORT).show();
            } else {
                ProjectUtil.showProgressDialog(mContext,true,getString(R.string.please_wait));
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, binding.etOtp.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }
        });

        binding.tvResend.setOnClickListener(v -> {
            sendVerificationCode();
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

    }

    private void sendVerificationCode() {

        binding.tvVerifyText.setText("We have send you an SMS on " + mobile + " with 6 digit verification code.");

        new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText("" + millisUntilFinished/1000);
                binding.tvResend.setEnabled(false);
            }

            @Override
            public void onFinish() {
                binding.tvResend.setText(mContext.getString(R.string.resend));
                binding.tvResend.setEnabled(true);
            }
        }.start();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile.replace(" ",""), // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        VerifyOtpActivity.this.id = id;
                        Toast.makeText(mContext, getString(R.string.enter_6_digit_code), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        ProjectUtil.pauseProgressDialog();
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        ProjectUtil.pauseProgressDialog();
                        Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }); // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            ProjectUtil.pauseProgressDialog();
                            // Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
                            signUpApi();

                        } else {
                            ProjectUtil.pauseProgressDialog();
                            Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {}

                        }
                    }
                });

    }

    private void signUpApi() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);

        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        AndroidNetworking.upload(AppConstant.BASE_URL + "signup")
                .addMultipartParameter(paramHash)
                .addMultipartFile(fileParamHash)
                .setPriority(Priority.HIGH)
                .setTag("signup")
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        ProjectUtil.pauseProgressDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.getString("status").equals("1")) {

                                ModelLogin modelLogin = new Gson().fromJson(response, ModelLogin.class);

                                Log.e("zdgfxsdgfxdg","response = " + response);

                                sharedPref.setBooleanValue(AppConstant.IS_REGISTER,true);
                                sharedPref.setUserDetails(AppConstant.USER_DETAILS,modelLogin);

                                ContextCompat.startForegroundService(mContext,new Intent(getApplicationContext(), MyService.class));

                                if(AppConstant.RES_DRIVER.equals(modelLogin.getResult().getType())) {
                                    startActivity(new Intent(mContext, StoreOrdersActivity.class));
                                    finish();
                                } else if(AppConstant.SH_DRIVER.equals(modelLogin.getResult().getType())) {
                                    startActivity(new Intent(mContext, ShipReqActivity.class));
                                    finish();
                                } else {

                                }
                            } else {
                                // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Exception","Exception = " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        ProjectUtil.pauseProgressDialog();
                    }

                });

    }

}