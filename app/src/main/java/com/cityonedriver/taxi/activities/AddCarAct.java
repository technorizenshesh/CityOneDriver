package com.cityonedriver.taxi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.cityonedriver.R;
import com.cityonedriver.SignUpActivity;
import com.cityonedriver.databinding.ActivityAddCarBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipReqActivity;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.taxi.adapters.AdapterCarTypes;
import com.cityonedriver.taxi.models.ModelCarTypes;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.Compress;
import com.cityonedriver.utils.MyService;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.gson.Gson;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCarAct extends AppCompatActivity {

    private static final int PERMISSION_ID = 101;
    Context mContext = AddCarAct.this;
    ActivityAddCarBinding binding;
    private String carId = null;
    File carImageFile = null;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_car);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);;

        init();

    }

    private void init() {

        getAllCarTypes();

        binding.ivCarImage.setOnClickListener(v -> {
            if(checkPermissions()) {
                final PickImageDialog dialog = PickImageDialog.build(new PickSetup());
                dialog.setOnPickCancel(new IPickCancel() {
                    @Override
                    public void onCancelClick() {
                        dialog.dismiss();
                    }
                }).setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult r) {
                        if (r.getError() == null) {

                            String path = r.getPath();
                            carImageFile = new File(path);

                            Compress.get(mContext).setQuality(40)
                                    .execute(new Compress.onSuccessListener() {
                                        @Override
                                        public void response(boolean status, String message, File file) {
                                            Log.e("kjsgdfjklgdkjasf","file = " + file.length()/1024 + "kb      "+ message );
                                            carImageFile = file;
                                            binding.ivCarImage.setImageURI(r.getUri());
                                        }
                                    }).CompressedImage(path);

                        } else {
                            // Handle possible errors
                            // TODO: do what you have to do with r.getError();
                            Toast.makeText(mContext, r.getError().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                }).show(AddCarAct.this);
            } else {
                requestPermissions();
            }
        });

        binding.btSubmit.setOnClickListener(v -> {
            if(carImageFile == null) {
                Toast.makeText(mContext,getString(R.string.please_upload_car_image), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(binding.etCarModel.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(binding.etCarNumber.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(binding.etCarBrand.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show();
            } else if(binding.spYearOfMenu.getSelectedItemPosition() == 0) {
                Toast.makeText(mContext, getString(R.string.please_select_manufactured_year), Toast.LENGTH_SHORT).show();
            } else {

                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("car_type_id",carId);
                hashMap.put("brand",binding.etCarBrand.getText().toString().trim());
                hashMap.put("car_model",binding.etCarModel.getText().toString().trim());
                hashMap.put("year_of_manufacture",binding.spYearOfMenu.getSelectedItem().toString().trim());
                hashMap.put("car_number",binding.etCarNumber.getText().toString().trim());
                hashMap.put("user_id",modelLogin.getResult().getId());

                HashMap<String,File> fileHashMap = new HashMap<>();
                fileHashMap.put("car_image",carImageFile);

                addCar(hashMap,fileHashMap);

            }
        });

    }

    private void getAllCarTypes() {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        Call<ResponseBody> call = api.getCarTypesApi();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                ProjectUtil.pauseProgressDialog();

                Log.e("kghkljsdhkljf","response = " + response);

                try {
                    String stringResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringResponse);

                    Log.e("kjagsdkjgaskjd","stringResponse = " + response);
                    Log.e("kjagsdkjgaskjd","stringResponse = " + stringResponse);

                    if (jsonObject.getString("status").equals("1")) {

                        ModelCarTypes modelTaxiType = new Gson().fromJson(stringResponse, ModelCarTypes.class);

                        AdapterCarTypes adapterCarTypes = new AdapterCarTypes(mContext, modelTaxiType.getResult());
                        binding.spCarTypes.setAdapter(adapterCarTypes);

                        binding.spCarTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                carId = modelTaxiType.getResult().get(position).getId();
                                Log.e("asdkshdkjasd","carId = " + carId);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}

                        });

                    } else {
                        // Toast.makeText(mContext, getString(R.string.no_chat_found), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
            }

        });

    }

    private void addCar(HashMap<String,String> hashMap,HashMap<String,File> fileHashMap) {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);

        Log.e("sdfsfsfsdfs","hashMap = " + hashMap.toString());

        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        AndroidNetworking.upload(AppConstant.BASE_URL + "update_vehicle")
                .addMultipartParameter(hashMap)
                .addMultipartFile(fileHashMap)
                .setPriority(Priority.HIGH)
                .setTag("update_vehicle")
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

                                sharedPref.setUserDetails(AppConstant.USER_DETAILS,modelLogin);

                                Log.e("sdfsfsfsdfs","response = " + response);

                                ContextCompat.startForegroundService(mContext,new Intent(getApplicationContext(), MyService.class));

                                startActivity(new Intent(mContext, TaxiHomeAct.class));
                                finish();

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


    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions (
                this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE },
                PERMISSION_ID
        );
    }


}