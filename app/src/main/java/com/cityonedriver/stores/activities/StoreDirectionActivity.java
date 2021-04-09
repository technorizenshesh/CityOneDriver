package com.cityonedriver.stores.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cityonedriver.R;
import com.cityonedriver.databinding.ActivityStoreDirectionBinding;
import com.cityonedriver.utils.DataParser;
import com.cityonedriver.utils.ProjectUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class StoreDirectionActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    Context mContext = StoreDirectionActivity.this;
    ActivityStoreDirectionBinding binding;
    SupportMapFragment mapFragment;
    GoogleMap GMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    MarkerOptions originDirectionLatlng, destinationDirectionLatlng;
    LatLng originLatLon,destiLatLon;
    String orginLat,originLon,originAdd,destiLat,destiLon,destiAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_store_direction);

        orginLat = getIntent().getStringExtra("orginLat");
        originLon = getIntent().getStringExtra("originLon");
        originAdd = getIntent().getStringExtra("originAdd");
        destiLat = getIntent().getStringExtra("destiLat");
        destiLon = getIntent().getStringExtra("destiLon");
        destiAdd = getIntent().getStringExtra("destiAdd");

        originLatLon  = new LatLng(Double.parseDouble(orginLat),Double.parseDouble(originLon));
        destiLatLon  = new LatLng(Double.parseDouble(destiLat),Double.parseDouble(destiLon));

        originDirectionLatlng = new MarkerOptions().position(originLatLon).title(originAdd);
        destinationDirectionLatlng = new MarkerOptions().position(destiLatLon).title(destiAdd);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        init();

    }

    private void init() {

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(StoreDirectionActivity.this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GMap = googleMap;

        googleMap.addMarker(originDirectionLatlng);

        googleMap.addMarker(destinationDirectionLatlng);

        ArrayList<LatLng> listLatLon = new ArrayList<>();
        listLatLon.add(originDirectionLatlng.getPosition());
        listLatLon.add(destinationDirectionLatlng.getPosition());

        zoomRoute(GMap,listLatLon);

        drawRoute();

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

    private void drawRoute() {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(originDirectionLatlng.getPosition(),
                destinationDirectionLatlng.getPosition());

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

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

    /**
     * A method to download json data from url
     */
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

    /**
     * A class to parse the JSON format
     */
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

            // Drawing polyline in the Google Map
            if (points.size() != 0)
                GMap.addPolyline(lineOptions);
        }

    }


}