package com.cityonedriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.activities.ShipReqActivity;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.taxi.activities.AddCarAct;
import com.cityonedriver.taxi.activities.TaxiHomeAct;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.InternetConnection;
import com.cityonedriver.utils.MyService;
import com.cityonedriver.utils.SharedPref;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class SplashActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Context mContext = SplashActivity.this;
    final static int SPLASH_TIME_OUT = 2000;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPref = SharedPref.getInstance(mContext);
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && locationManager.isProviderEnabled (
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions (
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                processNextActivity();
            }
        }
    }

    private void processNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPref.getBooleanValue(AppConstant.IS_REGISTER)) {
                    modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
                    ContextCompat.startForegroundService(mContext,new Intent(getApplicationContext(),
                            MyService.class));
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
                    }
                } else {
                    startActivity(new Intent(mContext,WelcomeActivity.class));
                    finish();
                }
            }
        } , SPLASH_TIME_OUT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(InternetConnection.checkConnection(mContext)){
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    processNextActivity();
                } else {
                    Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                    EnableGPSAutoMatically();
                }
            } else {
                requestPermissions();
            }
        } else {

        }

    }

    private void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // toast("Success");
                            Log.e("dskgfhsdfsdf","Success");
                            Toast.makeText(SplashActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            //toast("GPS is not on");
                            Log.e("dskgfhsdfsdf","No GPS");
                            Toast.makeText(SplashActivity.this, "GPS is not on", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(SplashActivity.this, 1000);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // toast("Setting change not allowed");

                            Toast.makeText(SplashActivity.this, "Setting change not allowed", Toast.LENGTH_SHORT).show();
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}