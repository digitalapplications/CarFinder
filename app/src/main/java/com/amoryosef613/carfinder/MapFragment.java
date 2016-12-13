package com.amoryosef613.carfinder;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amoryosef613.carfinder.maproute.DirectionsJSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

/**
 * Created by VIJESH on 11/15/2016.
 */
public class MapFragment extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        View.OnClickListener {

    MediaPlayer mediaPlayer = new MediaPlayer();


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    String TAG = "MAPFRAGMENT";
    SharedPreferences preferences;
    GPSTracker mGPS;
    NetworkDetector networkDetector;
    Button buttonViewLocation, buttonViewreturn;
    ArrayList<LatLng> markerPoints;
    LocationRequest mLocationRequest;
    Marker currLocationMarker;
    LatLng latLng;
    //Our Map
    private GoogleMap mMap;
    /// draw map route
    //To store longitude and latitude from map
    private double longitude;
    private double latitude;
    //Buttons
    private ImageButton buttonView;
    //Google ApiClient
    private GoogleApiClient googleApiClient;
    private boolean isRouteClick = false;
    private boolean isFirstTime = true;
    private boolean isGranted;

    //boolean isRouteDraw = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_map_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ActivityManager am = (ActivityManager) getApplication().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        componentInfo.getPackageName();
        mapFragment.getMapAsync(this);

        // Initializing
        markerPoints = new ArrayList<LatLng>();
        markerPoints.clear();

        isGranted = PreferenceManager.getDefaultSharedPreferences(MapFragment.this).getBoolean("permission_granted", false);

        preferences = getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);

        //Initializing views and adding onclick listeners

        buttonView = (ImageButton) findViewById(R.id.buttonView);
        buttonViewLocation = (Button) findViewById(R.id.buttonViewLocation);
        buttonViewreturn = (Button) findViewById(R.id.buttonViewreturn);

        buttonViewLocation.setOnClickListener(this);
        buttonView.setOnClickListener(this);
        buttonViewreturn.setOnClickListener(this);

        mGPS = new GPSTracker(MapFragment.this);
        networkDetector = new NetworkDetector(MapFragment.this);
        if (!networkDetector.isConnectingToInternet()) {
            networkDetector.showAlertDialog(MapFragment.this, "Internet Error!!", "Please check internet connection", false);
        } else {
            if (!mGPS.GPS_Status()) {
                mGPS.showSettingsAlert();
            }
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                       Time.mediaPlayer.stop();
                        Time.mediaPlayer.release();

                        //timmerr=false;

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    try {
                        Time.mediaPlayer.stop();
                        Time.mediaPlayer.release();
                        //timmerr=false;

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null && !googleApiClient.isConnected())
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void moveMapLastLocation(LatLng latLng) {
        //Creating a LatLng Object to store Coordinates
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Last Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        currLocationMarker = mMap.addMarker(markerOptions);
        //zoom to current position:
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        Log.i(TAG, "moveMapLastLocation : " + markerPoints.size());
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isFirstTime) {
            isFirstTime = false;
            loadMaponLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonView:
                Intent intentName = new Intent(getApplicationContext(), Time.class);
                startActivityForResult(intentName, 100);
                break;
            case R.id.buttonViewLocation:
                Toast.makeText(MapFragment.this, "Location saved successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("LATITUTE", String.valueOf(latitude));
                editor.putString("LONGITUTE", String.valueOf(longitude));

//                editor.putString("LATITUTE", String.valueOf(22.7255));
//                editor.putString("LONGITUTE", String.valueOf(75.8886));

                editor.apply();


                break;
            case R.id.buttonViewreturn:
                isRouteClick = true;
                drawRoute(latLng.latitude, latLng.longitude);
                break;
            default:
                break;
        }
    }

    private void drawRoute(final double lati_tute, final double longi_tute) {
        double desLat = Double.parseDouble(preferences.getString("LATITUTE", "0.0"));
        double desLng = Double.parseDouble(preferences.getString("LONGITUTE", "0.0"));
        LatLng desLatLng = new LatLng(desLat, desLng);
        LatLng sourceLatLng = new LatLng(lati_tute, longi_tute);
        if (markerPoints.size() >= 2) {
            markerPoints.set(1, sourceLatLng);
        } else {
            markerPoints.add(sourceLatLng);
        }
        markerPoints.set(0, desLatLng);

        Log.i(TAG, "LL :" + lati_tute + " LL : " + longi_tute);
        if (desLat != 0.0 && desLng != 0.0) {
            //Creating a location object
            if (desLat != lati_tute && desLng != longi_tute) {
                Toast.makeText(MapFragment.this, "Route has been drawn between your current and last location", Toast.LENGTH_LONG).show();
                LatLng latLng = new LatLng(lati_tute, longi_tute);
                Log.i(TAG, "buttonViewreturn : " + markerPoints.size());
                if (markerPoints.size() == 2) {

                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
            } else {
                Toast.makeText(MapFragment.this, "Current and destination location are same", Toast.LENGTH_LONG).show();
                mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(sourceLatLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currLocationMarker = mMap.addMarker(markerOptions);

                //zoom to current position:
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(sourceLatLng).zoom(15).build();

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

            }


        } else {
            //Creating a location object
            Log.i(TAG, "destination not found");
            Toast.makeText(MapFragment.this, "destination not found", Toast.LENGTH_LONG).show();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

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

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
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
            Log.d("Exception while url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initializing googleapi client
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isGranted) {
                Log.i("checkLocationPermission", "open opup");
            } else {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //place marker at current position
        if (location != null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                if (markerPoints.size()>=2)
//                    {
//                        markerPoints.set(1,latLng);
//                    }
//                    else
//                    {
//                        markerPoints.add(latLng);
//                    }
//                mMap.clear();
//                if (isRouteClick){
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//
//                    Log.i(TAG, "onLocationChanged : "+markerPoints.size());
//                    if (markerPoints.size()>=2)
//                    {
//                        markerPoints.set(1,latLng);
//                    }
//                    else
//                    {
//                        markerPoints.add(latLng);
//                    }
//                    drawRoute(latitude,longitude);
//
//                }else {
//                    mMap.clear();
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(latLng);
//                    markerOptions.title("Current Position");
//                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                    currLocationMarker = mMap.addMarker(markerOptions);
//
//                    //zoom to current position:
//                    CameraPosition cameraPosition = new CameraPosition.Builder()
//                            .target(latLng).zoom(15).build();
//
//                    mMap.animateCamera(CameraUpdateFactory
//                            .newCameraPosition(cameraPosition));
//                }

        } else {
            Toast.makeText(this, "Location not getting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (googleApiClient != null && !googleApiClient.isConnected()) {
                googleApiClient.connect();
            } else {
                mMap.clear();
                loadMaponLocation();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= 21) { //Is the user running Lollipop or above?
            finishAndRemoveTask(); //If yes, run the new fancy function to end the app and remove it from the Task Manager.
            System.exit(0);
        } else {
            finish(); //If not, then just end the app (without removing the task completely).
            System.exit(0);
        }
    }

    private void loadMaponLocation() {
        if (mMap != null) {
            mMap.clear();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isGranted) {
                Log.i("checkLocationPermission", "open opup");
            } else {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (mLastLocation != null) {
                    //place marker at current position
                    mMap.clear();
                    if (currLocationMarker != null) {
                        currLocationMarker.remove();
                    }
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();

                    latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    currLocationMarker = mMap.addMarker(markerOptions);
                    //zoom to current position:
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng).zoom(15).build();

                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));

                    Log.i(TAG, "loadMaponLocation if : " + markerPoints.size());

                    if (markerPoints.size() >= 1) {
                        markerPoints.set(0, latLng);
                    } else {
                        markerPoints.add(latLng);
                    }
                }
                mLocationRequest = new LocationRequest();
//                mLocationRequest.setInterval(5000); //5 seconds
//                mLocationRequest.setFastestInterval(3000); //3 seconds

                mLocationRequest.setInterval(60000); //10 minute
                mLocationRequest.setFastestInterval(30000); //5 minute
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            }
        } else {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mLastLocation != null) {
                //place marker at current position
                mMap.clear();
                if (currLocationMarker != null) {
                    currLocationMarker.remove();
                }
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();

                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currLocationMarker = mMap.addMarker(markerOptions);
                //zoom to current position:
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng).zoom(15).build();

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

                Log.i(TAG, "loadMaponLocation else : " + markerPoints.size());
                if (markerPoints.size() >= 1) {
                    Log.i(TAG, "if exist loadMaponLocation");
                    markerPoints.set(0, latLng);
                } else {
                    Log.i(TAG, "else loadMaponLocation");
                    markerPoints.add(latLng);
                }
            }
            mLocationRequest = new LocationRequest();

//            mLocationRequest.setInterval(5000); //5 seconds
//            mLocationRequest.setFastestInterval(3000); //3 seconds

            mLocationRequest.setInterval(60000); //10 minute
            mLocationRequest.setFastestInterval(30000); //5 minute

            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            mMap.clear();
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);

                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.BLUE);
            }
            for (int i = 0; i < points.size(); i++) {
                if (i > 0 && i < points.size() - 1)
                    continue;
                LatLng mlatlon = points.get(i);
                LatLng position = new LatLng(mlatlon.latitude, mlatlon.longitude);
                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(position);
                if (i == 0) {
                    markerOptions1.title("Current Position");
                } else {
                    markerOptions1.title("Last position");
                }
                markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currLocationMarker = mMap.addMarker(markerOptions1);

                //zoom to current position:
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(15).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }


            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }
}
