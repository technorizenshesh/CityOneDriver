package com.cityonedriver.taxi.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.cityonedriver.LoginActivity;
import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityTaxiHomeBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.cityonedriver.utils.dialog.NewRequestDialog;
import com.github.angads25.toggle.LabeledSwitch;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import org.json.JSONObject;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TaxiHomeAct extends
        AppCompatActivity
        implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 101;
    Context mContext = TaxiHomeAct.this;
    ActivityTaxiHomeBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    Marker currentLocMarker;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 3000;
    private long FASTEST_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_taxi_home);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        if(getIntent().getStringExtra("object") != null) {
            NewRequestDialog.getInstance().Request(TaxiHomeAct.this,getIntent().getStringExtra("object"));
        }

        init();

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("object") != null) {
                NewRequestDialog.getInstance().Request(TaxiHomeAct.this,intent.getStringExtra("object"));
            }
        }
    };

    private void onlineOfflineStatus(String status) {
        ProjectUtil.showProgressDialog(mContext,true,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("user_id",modelLogin.getResult().getId());
        param.put("status",status);

        Call<ResponseBody> call = api.onlineOfflineStatusApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                Log.e("xjgxkjdgvxsd","response = " + response);
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {
                        if(status.equals("online")) {
                            binding.switch4.setOn(true);
                        } else {
                            binding.switch4.setOn(false);
                        }
                    } else {

                    }

                } catch (Exception e) {
                    Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Exception","Exception = " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("onFailure","onFailure = " + t.getMessage());
            }
        });
    }

    private void init() {

        onlineOfflineStatus("online");

        binding.switch4.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(LabeledSwitch labeledSwitch, boolean isOn) {
                if(isOn) {
                    onlineOfflineStatus("online");
                } else {
                    onlineOfflineStatus("offline");
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(TaxiHomeAct.this);

        fetchLocation();

        startLocationUpdates();

        binding.setDriver(modelLogin.getResult());
        binding.navItems.setUser(modelLogin.getResult());

        binding.navItems.tvTripHistory.setOnClickListener(v -> {
            startActivity(new Intent(mContext,TripHistoryAct.class));
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        Log.e("sdfsdfdsf","modelLogin.getResult() = " + modelLogin.getResult().getImage());

        binding.ivMenu.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.navItems.tvLogout.setOnClickListener(v -> {
            logoutAppDialog();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

    }

    private void logoutAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.logout_text))
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.yes),new DialogInterface.OnClickListener() {
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter("Job_Status_Action"));
        if(currentLocation != null) {
            fetchLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission (
                mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (
                mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TaxiHomeAct.this, new String[]
                            { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.e("ivCurrentLocation", "location = " + location);
                    setCurrentLocationMap(currentLocation);
                } else {
                    startLocationUpdates();
                    Log.e("ivCurrentLocation", "location = " + location);
                }
            }
        });

    }

    private void setCurrentLocationMap(Location currentLocation) {
        if (map != null) {
            if(currentLocMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top))
                        .title(ProjectUtil.getCompleteAddressString(mContext, currentLocation.getLatitude(), currentLocation.getLongitude()));
                currentLocMarker = map.addMarker(markerOptions);
                currentLocMarker.setRotation(currentLocation.getBearing());
                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                // Toast.makeText(mContext, "marker added if ", Toast.LENGTH_SHORT).show();
            } else {
                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                currentLocMarker.setPosition(latLng);
                currentLocMarker.setRotation(currentLocation.getBearing());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                // Toast.makeText(mContext, "Location update else ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        Log.e("hdasfkjhksdf", "Location = ");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null) {
                            Log.e("hdasfkjhksdf", "Location = " + locationResult.getLastLocation());
                            currentLocation = locationResult.getLastLocation();
                            setCurrentLocationMap(currentLocation);
                        } else {
                            fetchLocation();
                        }
                    }
                },
                Looper.myLooper());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

}