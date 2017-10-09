package com.dev.jvh.golfcourseexercise;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FetchDataTask task;
    private JSONArray courses;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fetchData();
    }

    private void fetchData() {
        task = new FetchDataTask();
        task.execute("http://ptm.fi/materials/golfcourses/golf_courses.json");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private class FetchDataTask extends AsyncTask<String,Void,JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                try {
                    courses = jsonObject.getJSONArray("courses");
                    for (int i=0;i < courses.length();i++) {
                        JSONObject hs = courses.getJSONObject(i);
                        LatLng ICT = new LatLng(
                                Double.parseDouble(hs.getString("lat")),
                                Double.parseDouble(hs.getString("lng")));
                        boundsBuilder.include(ICT);
                        MarkerOptions options = new MarkerOptions()
                                .position(ICT)
                                .title(hs.getString("course"))
                                .snippet(new StringBuilder()
                                        .append(hs.getString("address")).append("\n")
                                        .append(hs.getString("phone")).append("\n")
                                        .append(hs.getString("email")).append("\n")
                                        .append(hs.getString("web"))
                                        .toString());
                        switch (hs.getString("type"))
                        {
                            case "Kulta":
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                break;
                            case "Kulta/Etu":
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                break;
                            default:
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }
                        mMap.addMarker(options);
                    }
                    LatLngBounds bounds = boundsBuilder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,50));
                    mMap.setInfoWindowAdapter(new MyInfoWindow(getLayoutInflater()));
                } catch (JSONException e) {
                    Log.e("JSON", "Error getting data.");
                }

        }
    }

    private class MyInfoWindow implements GoogleMap.InfoWindowAdapter {
        private LayoutInflater inflater;

        public MyInfoWindow(LayoutInflater layoutInflater) {
            this.inflater = layoutInflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = inflater.inflate(R.layout.info_window,null);
            TextView infoTitle = (TextView) view.findViewById(R.id.infoTitle);
            TextView infoContent = (TextView) view.findViewById(R.id.infoContent);
            infoTitle.setText(marker.getTitle());
            infoContent.setText(marker.getSnippet());
            return view;
        }
    }
}
