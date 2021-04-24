package com.cityonedriver.taxi.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityTrackTaxiBinding;
import com.cityonedriver.databinding.TaxiPaymentDialogBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.taxi.adapters.AdapterCarTypes;
import com.cityonedriver.taxi.models.ModelCarTypes;
import com.cityonedriver.taxi.models.ModelTaxiBookingDetail;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.App;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.DrawPollyLine;
import com.cityonedriver.utils.LatLngInterpolator;
import com.cityonedriver.utils.MarkerAnimation;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ng.max.slideview.SlideView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TrackTaxiAct extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 101;
    Context mContext = TrackTaxiAct.this;
    ActivityTrackTaxiBinding binding;
    GoogleMap map;
    private String request_id;
    Vibrator vibrator;
    private PolylineOptions lineOptions;
    private LatLng pickUpLatLng, dropOffLatLng;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    Marker currentLocMarker;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 3000;
    private long FASTEST_INTERVAL = 2000;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    AlertDialog.Builder builder1;
    String driverStatus = "",UserId="",UserName="",UserImage="",mobile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_track_taxi);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        
        if (getIntent() != null)
            request_id = getIntent().getStringExtra("request_id");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fetchLocation();
        startLocationUpdates();

        init();
        
    }

    //    BroadcastReceiver locationReciever = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if(intent.getStringExtra("lat") != null) {
//                currentLatLng = new LatLng(Double.parseDouble(intent.getStringExtra("lat"))
//                        ,Double.parseDouble(intent.getStringExtra("lon")));
//                if(carMarker != null) {
//                    MarkerAnimation.animateMarkerToGB(carMarker, currentLatLng,
//                            new LatLngInterpolator.Spherical());
//                    animateCamera(currentLatLng);
//                } else {
//                    if(map!=null) {
//                        carMarker.setPosition(currentLatLng);
//                        carMarker = map.addMarker(new MarkerOptions().title("Car")
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top)));
//                        MarkerAnimation.animateMarkerToGB(carMarker, currentLatLng,
//                                new LatLngInterpolator.Spherical());
//                    }
//                }
//            }
//        }
//    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras() != null) {
                Toast.makeText(context, getString(R.string.cancel_by_user), Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(mContext,TaxiHomeAct.class));
            }
        }
    };

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
                binding.tvAddress.setText(ProjectUtil.getCompleteAddressString(mContext, currentLocation.getLatitude(), currentLocation.getLongitude()));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                // Toast.makeText(mContext, "marker added if ", Toast.LENGTH_SHORT).show();
            } else {
                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                currentLocMarker.setPosition(latLng);
                currentLocMarker.setRotation(currentLocation.getBearing());
                binding.tvAddress.setText(ProjectUtil.getCompleteAddressString(mContext, currentLocation.getLatitude(), currentLocation.getLongitude()));
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

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission (
                mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (
                mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TrackTaxiAct.this, new String[]
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
    
    private void animateCamera(@NonNull LatLng location) {
        //  LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(map != null)
        map.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(location)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter("cancel_by_user"));
        // registerReceiver(locationReciever,new IntentFilter("data_update_location1"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        // unregisterReceiver(locationReciever);
    }

    private void init() {

        binding.ivCancel.setOnClickListener(v -> {
            cancelByDriverDialog();
        });

        binding.ivChat.setOnClickListener(v -> {
            startActivity(new Intent(mContext,TaxiChatingActivity.class)
                    .putExtra("sender_id",modelLogin.getResult().getId())
                    .putExtra("receiver_id",UserId)
                    .putExtra("name",UserName)
                    .putExtra("request_id",request_id)
            );
        });

        binding.icCall.setOnClickListener(v -> {
            if(mobile != null) callUser(mobile);
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.tvTitle.setText(getString(R.string.pick_up_passenger));

        binding.ivNavigate.setOnClickListener(v -> {
            // navigateToMyLocation();
            fetchLocation();
        });

        binding.btnArrived.setOnClickListener(v -> {
            DriverArrivedDialog();
        });

        binding.slideViewBegin.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                DriverChangeStatus(request_id, "Start");
            }
        });

        binding.slideViewEnd.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                DriverChangeStatus(request_id, "Finish");
            }
        });

    }

    private void cancelByDriverDialog() {
        builder1 = new AlertDialog.Builder(TrackTaxiAct.this);
        builder1.setMessage(getResources()
                .getString(R.string.are_your_sure_you_want_to_cancel_the_trip));
        builder1.setCancelable(false);

        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        cancelByDriverApi();
                    }
                });

        builder1.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void DriverArrivedDialog() {

        builder1 = new AlertDialog.Builder(TrackTaxiAct.this);
        builder1.setMessage(getResources()
                .getString(R.string.are_you_sure_you_have_arrived_at_pickup_location_of_passenger));
        builder1.setCancelable(false);

        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        DriverChangeStatus(request_id, "Arrived");
                    }
                });

        builder1.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    private void callUser(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + number)); //change the number.
        startActivity(callIntent);
    }

    private void cancelByDriverApi() {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("driver_id",modelLogin.getResult().getId());
        param.put("request_id", request_id);
        param.put("status", "Cancel_by_driver");

        Call<ResponseBody> call = api.acceptCancelRequestForTaxi(param);

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

                        ModelTaxiBookingDetail data = new Gson().fromJson(stringResponse,ModelTaxiBookingDetail.class);

                        driverStatus = data.getResult().getStatus();
                        Log.e("kjagsdkjgaskjd","status = " + driverStatus);

                        binding.btnArrived.setVisibility(View.GONE);
                        binding.btnBegin.setVisibility(View.GONE);
                        binding.btnEnd.setVisibility(View.GONE);

                        startActivity(new Intent(mContext,TaxiHomeAct.class));
                        finish();
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

    private void DriverChangeStatus(String request_id, String status) {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("driver_id",modelLogin.getResult().getId());
        param.put("request_id", request_id);
        param.put("status", status);

        Call<ResponseBody> call = api.acceptCancelRequestForTaxi(param);

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

                        ModelTaxiBookingDetail data = new Gson().fromJson(stringResponse,ModelTaxiBookingDetail.class);

                        driverStatus = data.getResult().getStatus();
                        Log.e("kjagsdkjgaskjd","status = " + driverStatus);
                        if (driverStatus.equals("Arrived")) {
                            binding.btnArrived.setVisibility(View.GONE);
                            binding.btnBegin.setVisibility(View.VISIBLE);
                        } else if (driverStatus.equals("Start")) {
                            binding.btnArrived.setVisibility(View.GONE);
                            binding.btnBegin.setVisibility(View.GONE);
                            binding.btnEnd.setVisibility(View.VISIBLE);
                            binding.tvTitle.setText("Trip start");
                        } else if (driverStatus.equals("Finish")) {
                            binding.ivCancel.setVisibility(View.GONE);
                            binding.tvTitle.setText("Trip finish");
                            binding.btnEnd.setVisibility(View.GONE);
//                            startActivity(new Intent(TrackAct.this, PaymentSummary.class).putExtra("request_id",request_id)
//                                    .putExtra("user_id",UserId).putExtra("userName",UserName));
//                            finish();
                        }
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

//    private void navigateToMyLocation() {
//        if(currentLatLng == null) {
//            map.addMarker(new MarkerOptions().position(new LatLng(currentLatLng.latitude,
//                    currentLatLng.longitude)).title("My Location")
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top)));
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(new LatLng(currentLatLng.latitude, currentLatLng.longitude))));
//            binding.tvAddress.setText(ProjectUtil.getCompleteAddressString(mContext,currentLatLng.latitude,
//                    currentLatLng.longitude));
//        } else {
//            carMarker.setPosition(currentLatLng);
//            MarkerAnimation.animateMarkerToGB(carMarker, currentLatLng,
//                    new LatLngInterpolator.Spherical());
//            animateCamera(currentLatLng);
//        }
//    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // setCurrentLoc();
            }
        }
    }

    private void setCurrentLoc() {
        DrawPolyLine();
    }

    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 150;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    private void DrawPolyLine() {
        DrawPollyLine.get(this).setOrigin(pickUpLatLng)
                .setDestination(dropOffLatLng).execute(new DrawPollyLine.onPolyLineResponse() {
            @Override
            public void Success(ArrayList<LatLng> latLngs) {
                // map.clear();
                lineOptions = new PolylineOptions();
                lineOptions.addAll(latLngs);
                lineOptions.width(10);
                lineOptions.color(R.color.black);
                ArrayList<LatLng> latlonList = new ArrayList<>();
                latlonList.add(pickUpLatLng);
                latlonList.add(dropOffLatLng);
                latlonList.add(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
                zoomRoute(map,latlonList);
                setPickDropMarker();
            }
        });
    }

    private void setPickDropMarker() {
        if (map != null) {
            // map.clear();
            if (lineOptions != null)
                map.addPolyline(lineOptions);
            if (pickUpLatLng != null) {
                String pickAdd = ProjectUtil.getCompleteAddressString(mContext,pickUpLatLng.latitude,pickUpLatLng.longitude);
                MarkerOptions pickMarker = new MarkerOptions().title("PickUp Passenger Address : \n" + pickAdd)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))
                        .position(pickUpLatLng);
                map.addMarker(pickMarker);
                // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(PickUpLatLng)));
            }
            if (dropOffLatLng != null) {
                String dropAdd = ProjectUtil.getCompleteAddressString(mContext,dropOffLatLng.latitude,dropOffLatLng.longitude);
                MarkerOptions dropMarker = new MarkerOptions().title("Drop Passenger Address : \n" + dropAdd)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                        .position(dropOffLatLng);
                map.addMarker(dropMarker);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        getBookingDetail(request_id);
    }

    private void getBookingDetail(String request_id) {

        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        HashMap<String,String> param = new HashMap<>();
        param.put("request_id",request_id);

        Call<ResponseBody> call = api.bookingDetails(param);

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

                        ModelTaxiBookingDetail data = new Gson().fromJson(stringResponse,ModelTaxiBookingDetail.class);

                        driverStatus = data.getResult().getStatus();

                        Log.e("driverStatus","driverStatus = " + driverStatus);

                        if (driverStatus.equals("Arrived")) {
                            binding.btnArrived.setVisibility(View.GONE);
                            binding.btnBegin.setVisibility(View.VISIBLE);
                        } else if (driverStatus.equals("Start")) {
                            binding.btnArrived.setVisibility(View.GONE);
                            binding.btnBegin.setVisibility(View.GONE);
                            binding.btnEnd.setVisibility(View.VISIBLE);
                            binding.tvTitle.setText("Trip start");
                        } else if (driverStatus.equals("Finish")) {
                            binding.tvTitle.setText("Trip finish");
                            binding.btnEnd.setVisibility(View.GONE);
                            binding.ivCancel.setVisibility(View.GONE);
                            openPaymentSummaryDialog(data);
//                            startActivity(new Intent(TrackAct.this, PaymentSummary.class)
//                            .putExtra("request_id",request_id)
//                                    .putExtra("user_id",UserId).putExtra("userName",UserName));
//                            finish();
                        }

                        UserId = data.getResult().getUserDetails().getId();
                        UserName = data.getResult().getUserDetails().getUserName();
                        UserImage = data.getResult().getUserDetails().getImage();
                        mobile = data.getResult().getUserDetails().getMobile();
                        binding.tvName.setText(UserName);

                        try {
                            Picasso.get().load(UserImage)
                                    .placeholder(R.drawable.default_profile_icon)
                                    .error(R.drawable.default_profile_icon).into(binding.ivUserPropic);
                        } catch (Exception e){

                        }

                        pickUpLatLng = new LatLng(Double.parseDouble(data.getResult().getPicuplat()), Double.parseDouble(data.getResult().getPickuplon()));
                        dropOffLatLng = new LatLng(Double.parseDouble(data.getResult().getDroplat()), Double.parseDouble(data.getResult().getDroplon()));

                        if (checkPermissions()) {
                            if (isLocationEnabled()) {
                                Log.e("dropOffLatLng","pickUpLatLng = " + pickUpLatLng);
                                Log.e("dropOffLatLng","dropOffLatLng = " + dropOffLatLng);
                                setCurrentLoc();
                            } else {
                                Toast.makeText(TrackTaxiAct.this, "Turn on location", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        } else {
                            requestPermissions();
                        }

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

    private void openPaymentSummaryDialog(ModelTaxiBookingDetail data) {

        Dialog dialog = new Dialog(mContext, WindowManager.LayoutParams.MATCH_PARENT);
        TaxiPaymentDialogBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext)
        ,R.layout.taxi_payment_dialog,null,false);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.setPayment(data.getResult());
        dialogBinding.tvEstiPrice.setText(AppConstant.DOLLAR + data.getResult().getEstimateChargeAmount());

        dialogBinding.ivBack.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogBinding.btOk.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(mContext,TaxiHomeAct.class));
            finishAffinity();
        });

        dialog.show();

    }

}