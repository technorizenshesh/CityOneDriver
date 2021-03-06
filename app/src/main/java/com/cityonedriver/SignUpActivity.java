package com.cityonedriver;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.cityonedriver.databinding.ActivitySignUpBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipReqActivity;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.taxi.activities.AddCarAct;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.Compress;
import com.cityonedriver.utils.MyService;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public class SignUpActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 101;
    private static final int PERMISSION_ID = 44;
    Context mContext = SignUpActivity.this;
    ActivitySignUpBinding binding;
    private LatLng latLng;
    private String registerId;
    File fileImage, doc1Img, doc2Img;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
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

                    }

                });

        if (!Places.isInitialized()) {
            Places.initialize(mContext, getString(R.string.places_api_key));
        }

        init();

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
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE },
                PERMISSION_ID
        );
    }

    private void init() {

        binding.ivProfile.setOnClickListener(v -> {
            if (checkPermissions()) {
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
                            fileImage = new File(path);

                            Compress.get(mContext).setQuality(70)
                                    .execute(new Compress.onSuccessListener() {
                                        @Override
                                        public void response(boolean status, String message, File file) {
                                            Log.e("kjsgdfjklgdkjasf", "file = " + file.length() / 1024 + "kb      " + message);
                                            fileImage = file;
                                            binding.ivProfile.setImageURI(r.getUri());
                                        }
                                    }).CompressedImage(path);

                        } else {
                            // Handle possible errors
                            // TODO: do what you have to do with r.getError();
                            Toast.makeText(mContext, r.getError().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                }).show(SignUpActivity.this);
            } else {
                requestPermissions();
            }
        });

        binding.ivDoc1.setOnClickListener(v -> {
            if (checkPermissions()) {
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
                            doc1Img = new File(path);

                            Compress.get(mContext).setQuality(70)
                                    .execute(new Compress.onSuccessListener() {
                                        @Override
                                        public void response(boolean status, String message, File file) {
                                            Log.e("kjsgdfjklgdkjasf", "file = " + file.length() / 1024 + "kb      " + message);
                                            doc1Img = file;
                                            binding.ivDoc1.setImageURI(r.getUri());
                                        }
                                    }).CompressedImage(path);

                        } else {
                            // Handle possible errors
                            // TODO: do what you have to do with r.getError();
                            Toast.makeText(mContext, r.getError().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                }).show(SignUpActivity.this);
            } else {
                requestPermissions();
            }
        });

        binding.ivDoc2.setOnClickListener(v -> {
            if (checkPermissions()) {
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
                            doc2Img = new File(path);

                            Compress.get(mContext).setQuality(70)
                                    .execute(new Compress.onSuccessListener() {
                                        @Override
                                        public void response(boolean status, String message, File file) {
                                            Log.e("kjsgdfjklgdkjasf", "file = " + file.length() / 1024 + "kb      " + message);
                                            doc2Img = file;
                                            binding.ivDoc2.setImageURI(r.getUri());
                                        }
                                    }).CompressedImage(path);

                        } else {
                            // Handle possible errors
                            // TODO: do what you have to do with r.getError();
                            Toast.makeText(mContext, r.getError().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                }).show(SignUpActivity.this);
            } else {
                requestPermissions();
            }
        });

        binding.etAddress.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(mContext, LoginActivity.class));
        });

        binding.btSignUp.setOnClickListener(v -> {
            startActivity(new Intent(mContext, VerifyOtpActivity.class));
        });

        binding.btSignUp.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.etUsername.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_username), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.etEmail.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_email_add), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.etPhone.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_phone_add), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.etAddress.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_select_add), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.etLandmark.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enterlandmark_add), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.pass.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_pass), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(binding.confirmPass.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_enter_conf_pass), Toast.LENGTH_SHORT).show();
            } else if (!(binding.pass.getText().toString().trim().length() > 4)) {
                Toast.makeText(mContext, getString(R.string.password_validation_text), Toast.LENGTH_SHORT).show();
            } else if (!(binding.pass.getText().toString().trim().equals(binding.confirmPass.getText().toString().trim()))) {
                Toast.makeText(mContext, getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
            } else if (!ProjectUtil.isValidEmail(binding.etEmail.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            } else if (!validateUsing_libphonenumber(binding.etPhone.getText().toString().replace(" ", "")
                    , binding.ccp.getSelectedCountryCode())) {
                Toast.makeText(mContext, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
            } else if (binding.spDriverType.getSelectedItemPosition() == 0) {
                Toast.makeText(mContext, getString(R.string.please_select_driver_type), Toast.LENGTH_SHORT).show();
            } else if (fileImage == null) {
                Toast.makeText(mContext, getString(R.string.please_upload_profile_image), Toast.LENGTH_SHORT).show();
            } else if (doc1Img == null || doc2Img == null) {
                Toast.makeText(mContext, getString(R.string.please_upload_both_doc), Toast.LENGTH_SHORT).show();
            } else {
                if (TextUtils.isEmpty(binding.etReferralCode.getText().toString().trim())) {
                    HashMap<String, String> params = new HashMap<>();

                    params.put("user_name", binding.etUsername.getText().toString().trim());
                    params.put("email", binding.etEmail.getText().toString().trim());
                    params.put("mobile", binding.etPhone.getText().toString().trim());
                    params.put("address", binding.etAddress.getText().toString().trim());
                    params.put("land_mark", binding.etLandmark.getText().toString().trim());
                    params.put("lat", String.valueOf(latLng.latitude));
                    params.put("lon", String.valueOf(latLng.longitude));
                    params.put("register_id", registerId);
                    params.put("referral_code", binding.etReferralCode.getText().toString().trim());
                    params.put("password", binding.pass.getText().toString().trim());

                    String type = "";

                    if (binding.spDriverType.getSelectedItemPosition() == 1) {
                        type = AppConstant.RES_DRIVER;
                        params.put("type", AppConstant.RES_DRIVER);
                    } else if (binding.spDriverType.getSelectedItemPosition() == 2) {
                        type = AppConstant.SH_DRIVER;
                        params.put("type", AppConstant.SH_DRIVER);
                    } else {
                        type = AppConstant.TAXI_DRIVER;
                        params.put("type", AppConstant.TAXI_DRIVER);
                    }

                    HashMap<String, File> fileHashMap = new HashMap<>();
                    fileHashMap.put("image", fileImage);
                    fileHashMap.put("document1", doc1Img);
                    fileHashMap.put("document2", doc2Img);

                    Log.e("fsfdsfdsfs", "image = " + fileImage);
                    Log.e("fsfdsfdsfs", "document1 = " + doc1Img);
                    Log.e("fsfdsfdsfs", "document2 = " + doc2Img);

                    String mobileWithCounCode = (binding.ccp.getSelectedCountryCodeWithPlus()
                            + binding.etPhone.getText().toString().trim())
                            .replace(" ", "");

                    startActivity(new Intent(mContext, VerifyOtpActivity.class)
                            .putExtra("resgisterHashmap", params)
                            .putExtra("fileHashmap", fileHashMap)
                            .putExtra("mobile", mobileWithCounCode)
                    );
                } else {
                    checkReferCode(binding.etReferralCode.getText().toString().trim());
                }

            }
        });

    }

    private void checkReferCode(String code) {
        ProjectUtil.showProgressDialog(mContext, false, getString(R.string.please_wait));

        HashMap<String, String> paramHash = new HashMap<>();
        paramHash.put("referral_code", code);

        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        Call<ResponseBody> call = api.checkReferApiCall(paramHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String stringResponse = response.body().string();

                    try {

                        JSONObject jsonObject = new JSONObject(stringResponse);

                        Log.e("afafasfasf", "stringResponse = " + stringResponse);

                        if (jsonObject.getString("status").equals("1")) {

                            HashMap<String, String> params = new HashMap<>();

                            params.put("user_name", binding.etUsername.getText().toString().trim());
                            params.put("email", binding.etEmail.getText().toString().trim());
                            params.put("mobile", binding.etPhone.getText().toString().trim());
                            params.put("address", binding.etAddress.getText().toString().trim());
                            params.put("land_mark", binding.etLandmark.getText().toString().trim());
                            params.put("lat", String.valueOf(latLng.latitude));
                            params.put("lon", String.valueOf(latLng.longitude));
                            params.put("register_id", registerId);
                            params.put("referral_code", binding.etReferralCode.getText().toString().trim());
                            params.put("password", binding.pass.getText().toString().trim());

                            String type = "";

                            if (binding.spDriverType.getSelectedItemPosition() == 1) {
                                type = AppConstant.RES_DRIVER;
                                params.put("type", AppConstant.RES_DRIVER);
                            } else if (binding.spDriverType.getSelectedItemPosition() == 2) {
                                type = AppConstant.SH_DRIVER;
                                params.put("type", AppConstant.SH_DRIVER);
                            } else {
                                type = AppConstant.TAXI_DRIVER;
                                params.put("type", AppConstant.TAXI_DRIVER);
                            }

                            HashMap<String, File> fileHashMap = new HashMap<>();
                            fileHashMap.put("image", fileImage);
                            fileHashMap.put("document1", doc1Img);
                            fileHashMap.put("document2", doc2Img);

                            Log.e("fsfdsfdsfs", "image = " + fileImage);
                            Log.e("fsfdsfdsfs", "document1 = " + doc1Img);
                            Log.e("fsfdsfdsfs", "document2 = " + doc2Img);

                            String mobileWithCounCode = (binding.ccp.getSelectedCountryCodeWithPlus()
                                    + binding.etPhone.getText().toString().trim())
                                    .replace(" ", "");

                            startActivity(new Intent(mContext, VerifyOtpActivity.class)
                                    .putExtra("resgisterHashmap", params)
                                    .putExtra("fileHashmap", fileHashMap)
                                    .putExtra("mobile", mobileWithCounCode)
                            );

                        } else {
                            Toast.makeText(mContext, "Your referral code is wrong", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();

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

//    private void signUpApi(HashMap<String,String> paramHash,HashMap<String,File> fileParamHash) {
//
//        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
//                .connectTimeout(120, TimeUnit.SECONDS)
//                .readTimeout(120, TimeUnit.SECONDS)
//                .writeTimeout(120, TimeUnit.SECONDS)
//                .build();
//
//        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);
//
//        Log.e("fsfdsfdsfs","paramHash = " + paramHash);
//
//        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
//        AndroidNetworking.upload(AppConstant.BASE_URL + "signup")
//                .addMultipartParameter(paramHash)
//                .addMultipartFile(fileParamHash)
//                .setPriority(Priority.HIGH)
//                .setTag("signup")
//                .build()
//                .getAsString(new StringRequestListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        ProjectUtil.pauseProgressDialog();
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//
//                            if(jsonObject.getString("status").equals("1")) {
//
//                                ModelLogin modelLogin = new Gson().fromJson(response, ModelLogin.class);
//
//                                Log.e("zdgfxsdgfxdg","response = " + response);
//
////                                sharedPref.setBooleanValue(AppConstant.IS_REGISTER,true);
////                                sharedPref.setUserDetails(AppConstant.USER_DETAILS,modelLogin);
////
////                                ContextCompat.startForegroundService(mContext,new Intent(getApplicationContext(), MyService.class));
////
////                                if(AppConstant.RES_DRIVER.equals(modelLogin.getResult().getType())) {
////                                    startActivity(new Intent(mContext, StoreOrdersActivity.class));
////                                    finish();
////                                } else if(AppConstant.SH_DRIVER.equals(modelLogin.getResult().getType())) {
////                                    startActivity(new Intent(mContext, ShipReqActivity.class));
////                                    finish();
////                                } else if(AppConstant.TAXI_DRIVER.equals(modelLogin.getResult().getType())) {
////                                    startActivity(new Intent(mContext, AddCarAct.class));
////                                    finish();
////                                }
//                            } else {
//                                // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
//                            }
//
//                        } catch (Exception e) {
//                            Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            Log.e("Exception","Exception = " + e.getMessage());
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        ProjectUtil.pauseProgressDialog();
//                    }
//
//                });
//
//    }

    private void signUpApi(
            String user_name,
            String email,
            String mobile,
            String address,
            String land_mark,
            String lat,
            String lon,
            String register_id,
            String password,
            String type,
            File image,
            File doc1,
            File doc2) {
        ProjectUtil.showProgressDialog(mContext, false, getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        MultipartBody.Part requestImagePart = prepareMultipartImage(image, "image");
        MultipartBody.Part doc1ImagePart = prepareMultipartImage(doc1, "document1");
        MultipartBody.Part doc2ImagePart = prepareMultipartImage(doc2, "document2");

        Call<ResponseBody> call = api.signUpApiCall(
                prepareParamRequestBody(user_name),
                prepareParamRequestBody(email),
                prepareParamRequestBody(mobile),
                prepareParamRequestBody(address),
                prepareParamRequestBody(land_mark),
                prepareParamRequestBody(lat),
                prepareParamRequestBody(lon),
                prepareParamRequestBody(register_id),
                prepareParamRequestBody(password),
                prepareParamRequestBody(type),
                requestImagePart, doc1ImagePart, doc2ImagePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String responseString = response.body().string();
                    Log.e("sdfasfasfasdfas", "responseString = " + responseString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
                Log.e("sdfasfasfasdfas", "Exception = " + t.getMessage());
            }
        });

    }

    private RequestBody prepareParamRequestBody(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value);
    }

    private MultipartBody.Part prepareMultipartImage(File imageFile, String key) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        return MultipartBody.Part.createFormData(key, imageFile.getName(), requestBody);
    }

    private boolean validateUsing_libphonenumber(String phNumber, String code) {

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(code));
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
        } catch (Exception e) {
            System.err.println(e);
        }

        boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
        if (isValid) {
            String internationalFormat = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            // Toast.makeText(this, "Phone Number is Valid " + internationalFormat, Toast.LENGTH_LONG).show();
            return true;
        } else {
            // Toast.makeText(this, "Phone Number is Invalid " + phoneNumber, Toast.LENGTH_LONG).show();
            return false;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                latLng = place.getLatLng();
                try {
                    String addresses = ProjectUtil.getCompleteAddressString(mContext, place.getLatLng().latitude, place.getLatLng().longitude);
                    binding.etAddress.setText(addresses);
                } catch (Exception e) {
                }
            }

        }

    }


}