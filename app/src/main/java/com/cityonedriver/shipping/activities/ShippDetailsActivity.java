package com.cityonedriver.shipping.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityShippDetailsBinding;
import com.cityonedriver.databinding.AddBidDialogBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.shipping.adapters.AdapterShipBids;
import com.cityonedriver.shipping.models.ModelShipBid;
import com.cityonedriver.shipping.models.ModelShipDetail;
import com.cityonedriver.utils.Api;
import com.cityonedriver.utils.ApiFactory;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.DataParser;
import com.cityonedriver.utils.ProjectUtil;
import com.cityonedriver.utils.SharedPref;
import com.cityonedriver.utils.onDateSetListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    Context mContext = ShippDetailsActivity.this;
    ActivityShippDetailsBinding binding;
    String parcelId = "";
    GoogleMap gMap;
    SupportMapFragment mapFragment;
    MarkerOptions originOption,dropOffOption;
    LatLng originLatLng,dropLatLng;
    SharedPref sharedPref;
    ModelLogin modelLogin;
    long pickUpDateMilli = 0;
    AdapterShipBids adapterShipBids;
    ModelShipBid modelShipBid;
    boolean orderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_shipp_details);
        parcelId = getIntent().getStringExtra("parcelid");
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        orderType = getIntent().getBooleanExtra("type",false);

        if(orderType) {
            binding.rvBids.setVisibility(View.GONE);
            binding.btBidNow.setVisibility(View.GONE);
        } else {
            binding.rvBids.setVisibility(View.VISIBLE);
            binding.btBidNow.setVisibility(View.VISIBLE);
        }

        init();

        shipDetailApi();
        getAllBidsNotLoader();
    }

    private void init() {

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ShippDetailsActivity.this);

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btBidNow.setOnClickListener(v -> {
            openAddBid();
        });

    }

    private void openAddBid() {

        Dialog dialog = new Dialog(mContext, WindowManager.LayoutParams.MATCH_PARENT);
        AddBidDialogBinding dialogBinding = DataBindingUtil
                .inflate(LayoutInflater.from(mContext),R.layout.add_bid_dialog,null,false);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.ivBack.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogBinding.etPickDate.setOnClickListener(v -> {
            ProjectUtil.DatePicker(0,mContext, new onDateSetListener() {
                @Override
                public void SelectedDate(String data) {
                    pickUpDateMilli = ProjectUtil.getTimeInMillSec(data);
                    dialogBinding.etDropDate.setText("");
                    dialogBinding.etPickDate.setText(data);
                }
            });
        });

        dialogBinding.etDropDate.setOnClickListener(v -> {
            ProjectUtil.DatePicker(pickUpDateMilli,mContext, new onDateSetListener() {
                @Override
                public void SelectedDate(String data) {
                    pickUpDateMilli = ProjectUtil.getTimeInMillSec(data);
                    dialogBinding.etDropDate.setText(data);
                }
            });
        });

        dialogBinding.btApply.setOnClickListener(v -> {
            if(TextUtils.isEmpty(dialogBinding.etPrice.getText().toString().trim())){
                Toast.makeText(mContext, getString(R.string.please_enter_price), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(dialogBinding.etPickDate.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_select_pickup_date), Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(dialogBinding.etDropDate.getText().toString().trim())) {
                Toast.makeText(mContext, getString(R.string.please_select_dropoff_date), Toast.LENGTH_SHORT).show();
            } else {
                 addBidApi(dialogBinding.etPrice.getText().toString().trim()
                ,dialogBinding.etPickDate.getText().toString().trim(),
                        dialogBinding.etDropDate.getText().toString().trim(),
                        dialogBinding.etComment.getText().toString().trim(),dialog);
            }
        });

        dialog.show();

    }

    private void getAllBidsNotLoader() {
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> param = new HashMap<>();
        param.put("shipping_id",parcelId);

        Call<ResponseBody> call = api.getBidApiCall(param);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String stringRes = response.body().string();
                    JSONObject jsonObject = new JSONObject(stringRes);
                    if(jsonObject.getString("status").equals("1")) {
                        modelShipBid = new Gson().fromJson(stringRes,ModelShipBid.class);
                        adapterShipBids = new AdapterShipBids(mContext,modelShipBid.getResult());
                        binding.rvBids.setHasFixedSize(true);
                        binding.rvBids.setAdapter(adapterShipBids);
                    } else {}
                } catch (Exception e) {}
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
            }

        });
    }

    private void addBidApi(String price, String pickDate, String dropDate, String comment,Dialog dialog) {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);

        HashMap<String,String> paramsHash = new HashMap<>();
        paramsHash.put("driver_id",modelLogin.getResult().getId());
        paramsHash.put("price",price);
        paramsHash.put("pick_date",pickDate);
        paramsHash.put("drop_date",dropDate);
        paramsHash.put("comment",comment);
        paramsHash.put("shipping_id",parcelId);

        Log.e("paramsHash","paramsHash = " + paramsHash.toString());

       Call<ResponseBody> call = api.addBidApiCall(paramsHash);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {
                        Log.e("responseStringfdgdg","responseString = " + responseString);
                        Toast.makeText(mContext, getString(R.string.success), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        getAllBidsNotLoader();
                    } else {}

                } catch (Exception e) {
                    // Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Exception","Exception = " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                ProjectUtil.pauseProgressDialog();
            }

        });

    }

    private void shipDetailApi() {
        ProjectUtil.showProgressDialog(mContext,false,getString(R.string.please_wait));
        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        HashMap<String,String> params = new HashMap<>();
        params.put("shipping_id",parcelId);

        Log.e("shipping_id","shipping_id = " + parcelId);

        Call<ResponseBody> call = api.getShipDetailApiCall(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ProjectUtil.pauseProgressDialog();
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);

                    if(jsonObject.getString("status").equals("1")) {

                        ModelShipDetail modelShipDetail = new Gson().fromJson(responseString,ModelShipDetail.class);
                        binding.setShip(modelShipDetail.getResult());

                        originLatLng = new LatLng(Double.parseDouble(modelShipDetail.getResult().getPickup_lat())
                                ,Double.parseDouble(modelShipDetail.getResult().getPickup_lon()));

                        dropLatLng = new LatLng(Double.parseDouble(modelShipDetail.getResult().getDrop_lat())
                                ,Double.parseDouble(modelShipDetail.getResult().getDrop_lon()));

                        originOption = new MarkerOptions().position(originLatLng).title(modelShipDetail.getResult().getPickup_location());
                        dropOffOption = new MarkerOptions().position(dropLatLng).title(modelShipDetail.getResult().getDrop_location());

                        drawRoute();

                        Log.e("responseString","responseString = " + responseString);

                    } else {
                        // Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
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

    private void drawRoute() {
        gMap.addMarker(originOption);
        gMap.addMarker(dropOffOption);

        ArrayList<LatLng> listLatLon = new ArrayList<>();
        listLatLon.add(originOption.getPosition());
        listLatLon.add(dropOffOption.getPosition());

        zoomRoute(gMap,listLatLon);
        drawDirection();
    }

    private void drawDirection() {
        String url = getDirectionsUrl(originLatLng,dropLatLng);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //setting transportation mode
        String mode = "mode=driving";

        String sensor = "mode=driving";

        // Building the parameters to the web service
        // String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.places_api_key);

        return url;

    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
                // Log.e("aslfkghfjasasfd","data = " + data);
                JSONObject jsonObject = new JSONObject(data);

                JSONArray array = jsonObject.getJSONArray("routes");

                JSONObject routes = array.getJSONObject(0);

                JSONArray legs = routes.getJSONArray("legs");

                JSONObject steps = legs.getJSONObject(0);

                JSONObject distance = steps.getJSONObject("distance");

                /* Log.e("Distance", distance.toString());
                   Log.e("Distance",distance.getString("text")); */

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            return data;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("aslfkghfjasasfd","result = " + result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ProjectUtil.pauseProgressDialog();
            ArrayList points = new ArrayList();
            PolylineOptions lineOptions = new PolylineOptions();

            for (int i = 0; i < result.size(); i++) {

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);

            }

            if(points.size() != 0) {
                gMap.addPolyline(lineOptions);
            }

        }

    }

}