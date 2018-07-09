package com.example.sucianalf.grouptracking;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import com.example.sucianalf.grouptracking.Adapter.PlaceAutocompleteAdapter;
import com.example.sucianalf.grouptracking.Model.MarkerObject;
import com.example.sucianalf.grouptracking.Model.PlaceInfo;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.sucianalf.grouptracking.AppController.getContext;

/**
 * Created by User on 10/2/2017.
 */

public class DisplayNavigation extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "DisplayNavigation";
    private static final String BASE_URL = "https://sucianalf.web.id/index.php/";
    private static final String BASE_CONTROLLER = "api/new/dijkstra/shortestPath/";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private Boolean mLocationPermissionsGranted = false;
    private AutoCompleteTextView mSearchText;
    private FloatingActionButton btnSet;
    private FloatingActionButton btnRemove;
    private ImageView mGps;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    public Marker mMarker;
    public Marker marker;

    public ArrayList<MarkerObject> getMarker = new ArrayList<>();
    public ArrayList<MarkerObject> getMemberMarker = new ArrayList<>();
    public ArrayList<MarkerObject> getDijkstra = new ArrayList<>();
    public double deviceLat, deviceLng, searchLat, searchLng;
    public String requestURL, getStatus, getMessage, getPlace, setDestinasi;
    public int groupID, serviceTrack;
    public MarkerOptions options = new MarkerOptions();
    private SessionManager session;
    private Bitmap bitmap;
    PolylineOptions line = new PolylineOptions();
    EncodedPolyline points;
    List<LatLng> path = new ArrayList();
    List<com.google.maps.model.LatLng> coords;
    HashMap<String, Marker> params = new HashMap<>();

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.display_navigation);
        mSearchText = findViewById(R.id.input_search);
        btnSet = findViewById(R.id.setDestination);
        btnRemove = findViewById(R.id.removeDestination);
        mGps = findViewById(R.id.ic_gps);
        getLocationPermission();
        session = new SessionManager(getApplicationContext());
        Bundle extras = getIntent().getExtras();
        groupID = extras.getInt("groupID");
        //        requestLocationUpdates();
        LocalBroadcastManager.getInstance(DisplayNavigation.this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentKey"));
        serviceTrack =  extras.getInt("serviceTrack");

        try {
            URL url = new URL(session.getImage());
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("serviceTrack");
            if (message != "")
            {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DisplayNavigation.this);
                try{
                    if(mLocationPermissionsGranted){
                        final Task location = mFusedLocationProviderClient.getLastLocation();
                        location.addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    Location currentLocation = (Location) task.getResult();
                                    if (currentLocation != null)
                                        getMember();
                                }
                            }
                        });
                    }
                }catch (SecurityException e) {
                    Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
                }
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
            checkDestination();
        }
    }

    private void init(){
        Log.d(TAG, "init: initializing");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)
                {

                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();
    }

//    private void requestLocationUpdates() {
//        LocationRequest request = new LocationRequest();
//        request.setInterval(10000);
//        request.setFastestInterval(15000);
//        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
//        int permission = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        if (permission == PackageManager.PERMISSION_GRANTED) {
//            client.requestLocationUpdates(request, new LocationCallback() {
//                @Override
//                public void onLocationResult(LocationResult locationResult) {
//                    Location location = locationResult.getLastLocation();
//                    if (location != null) {
//                        getMember();
//                    }
//                }
//            }, null);
//        }
//    }

    private void getMember()
    {
        String urlJSON = Url.FunctionName.MEMBER_MARKER+groupID+"/username/"+session.getUsername();
//        Log.d(TAG, "urlRequest: "+urlJSON);
        JsonObjectRequest jsonObjReqOrigin = new JsonObjectRequest(Request.Method.GET, urlJSON,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    getStatus = response.getString("status").toString();
                    getMessage = response.getString("message").toString();

                    if(getStatus.trim().equals("Succes")){
                        JSONArray member = response.getJSONArray("result");
                        for (int i=0; i < member.length(); i++){
                            String user = member.getJSONObject(i).getString("username").toString();
                            String koordinat = member.getJSONObject(i).getString("koordinat").toString();
                            String time = member.getJSONObject(i).getString("timestamp").toString();
                            String image = member.getJSONObject(i).getString("image_user").toString();
                            getMemberMarker.add(new MarkerObject(user, koordinat, time, image));
                        }

                        if (getMemberMarker.size() > 1 && getMemberMarker != null)
                        {
                            for (MarkerObject get : getMemberMarker)
                            {
                                marker = params.get(get.getId());
                                if (marker != null)
                                    marker.remove();

                                String[] latlong =  get.getValue().split(",");
                                double latitude = Double.parseDouble(latlong[0]);
                                double longitude = Double.parseDouble(latlong[1]);

                                options.position(new LatLng(latitude, longitude));
                                options.title("" + get.getId());
                                options.snippet("Last Update: "+get.getTime());
                                final String urlimg = get.getImage().toString();
                                Glide.with(getApplicationContext())
                                        .load(get.getImage()).asBitmap().fitCenter()
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {

                                            }
                                        });
                                try {
                                    URL url = new URL(get.getImage());
                                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                    options.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(bitmap)));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                marker = mMap.addMarker(options);
                                params.put(get.getId(), marker);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReqOrigin);
    }

    private Bitmap getMarkerBitmapFromView(Bitmap bitmap) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_mask, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageBitmap(bitmap);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public void buttonClickSetDest(View v)
    {
        setDestination();
        Intent intent = new Intent(DisplayNavigation.this, ListGroupActivity.class);
        startActivity(intent);
    }

    public void buttonClickremoveDest(View v)
    {
        removeDestination();
        Intent intent = new Intent(DisplayNavigation.this, ListGroupActivity.class);
        startActivity(intent);
    }

    public void setDestination()
    {
        setDestinasi = ""+searchLat +","+ searchLng;
        if (setDestinasi == null || getPlace == null)
            Toast.makeText(getApplicationContext(), "Destinasi Tujuan tidak ditemukan. Harap lakukan pencarian.", Toast.LENGTH_LONG).show();
        else
        {
            String tag_string_req = "set_destination";
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    Url.FunctionName.SET_DEST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        String status = jObj.getString("status").toString().trim();
                        if(status.equals("Success")){
                            String message = jObj.getString("message");
                            Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_LONG).show();
                        } else {
                            String errorMsg = jObj.getString("message");
                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("groupID", ""+groupID);
                    params.put("lokasi", setDestinasi);
                    params.put("username", session.getUsername());
                    params.put("place", getPlace);
                    return params;
                }
            };
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    public void removeDestination()
    {
        String tag_string_req = "remove_destination";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Url.FunctionName.REMOVE_DEST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status").toString().trim();
                    if(status.equals("Success")){
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("groupID", ""+groupID);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkDestination() {
        String urlJSON = Url.FunctionName.CHECK_DEST+groupID;
        Log.d(TAG, "getURL: "+urlJSON);
        JsonObjectRequest jsonObjReqOrigin = new JsonObjectRequest(Request.Method.GET, urlJSON,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    getStatus = response.getString("status").toString();
                    getMessage = response.getString("message").toString();

                    if(getStatus.trim().equals("Success"))
                    {
                        JSONArray member = response.getJSONArray("result");
                        String[] latlong =  member.getJSONObject(0).getString("LatLng").toString().split(",");
                        final String set_by = member.getJSONObject(0).getString("set_by").toString();
                        final String place_name = member.getJSONObject(0).getString("place_name").toString();
                        final String set_time = member.getJSONObject(0).getString("set_time").toString();

                        double latitude = Double.parseDouble(latlong[0]);
                        double longitude = Double.parseDouble(latlong[1]);

                        requestURL = BASE_URL+BASE_CONTROLLER+"user/user/deviceLoc/"+deviceLat+","+deviceLng+
                                "/searchLoc/"+latitude+","+longitude;
                        mSearchText.setVisibility(View.GONE);
                        if (set_by.trim().equals(session.getUsername()))
                            btnRemove.setVisibility(View.VISIBLE);
                        else
                            btnRemove.setVisibility(View.GONE);

                        btnSet.setVisibility(View.GONE);

                        Log.d(TAG, "JSON Request: "+requestURL);
                        JsonObjectRequest jsonObjReqOrigin = new JsonObjectRequest(Request.Method.GET, requestURL,
                                null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String getToast = "Shortest Path Found: ";
                                    getStatus = response.getString("status").toString();
                                    getMessage = response.getString("message").toString();

                                    if(getStatus.trim().equals("Success")){
                                        mMap.clear();

                                        JSONArray marker = response.getJSONArray("markerData");
                                        JSONArray dijkstra = response.getJSONArray("markerData");
                                        getMarker.clear();
                                        getDijkstra.clear();

                                        for (int i=0; i < marker.length(); i++){
                                            String id = marker.getJSONObject(i).getString("id").toString();
                                            String value = marker.getJSONObject(i).getString("value").toString();
                                            String weight = marker.getJSONObject(i).getString("weight").toString();
                                            String polyLine = marker.getJSONObject(i).getString("polyline").toString();
                                            getMarker.add(new MarkerObject(id, value, Integer.parseInt(weight), polyLine));
                                        }

                                        for (int i=0; i < dijkstra.length(); i++){
                                            String id = dijkstra.getJSONObject(i).getString("id").toString();
                                            String value = dijkstra.getJSONObject(i).getString("value").toString();
                                            String weight = dijkstra.getJSONObject(i).getString("weight").toString();
                                            getDijkstra.add(new MarkerObject(id, value, Integer.parseInt(weight)));
                                        }

                                        if (getMarker.size() > 1 && getMarker != null) {
                                            int i = 1;
                                            int sumTotal = 0;
                                            int max = getMarker.size();

                                            line = new PolylineOptions();
                                            if (path != null || path.size() > 1)
                                                path.clear();

                                            for (MarkerObject get : getMarker) {
                                                String[] latlong =  get.getValue().split(",");
                                                double latitude = Double.parseDouble(latlong[0]);
                                                double longitude = Double.parseDouble(latlong[1]);

                                                options.position(new LatLng(latitude, longitude));
                                                options.title("" + get.getId());
                                                sumTotal = sumTotal+get.getWeight();

                                                if (get.getWeight() == 0)
                                                {
                                                    if (get.getId().trim().equals("V0"))
                                                        options.snippet("Weight: 0, Value: 0");
                                                    else
                                                        options.snippet("");
                                                }
                                                else {
                                                    if (i < max)
                                                        options.snippet("Weight: " + get.getWeight() + ", Value: " + sumTotal);
                                                    else
                                                        options.snippet("Weight: " + get.getWeight() + ", Value: " + sumTotal +
                                                                "\n Place: " + place_name + "\n Dest By: " + set_by +
                                                                "\n Time: " + set_time);
                                                }
                                                if (i < max)
                                                {
                                                    if (get.getPolyLine().trim().equals(""))
                                                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                                    else
                                                    {
                                                        if (get.getId().trim().equals("V0"))
                                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                                        else
                                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                    }
                                                }
                                                else
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                                points = new EncodedPolyline(get.getPolyLine());
                                                if (i < max)
                                                {
                                                    if (points != null) {
                                                        coords  = points.decodePath();
                                                        for (com.google.maps.model.LatLng coord1 : coords) {
                                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                                        }
                                                    }
                                                }
                                                i++;
                                                mMarker = mMap.addMarker(options);

                                                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                                    @Override
                                                    public View getInfoWindow(Marker arg0) {
                                                        return null;
                                                    }

                                                    @Override
                                                    public View getInfoContents(Marker marker) {
                                                        Context mContext = getApplicationContext();
                                                        LinearLayout info = new LinearLayout(mContext);
                                                        info.setOrientation(LinearLayout.VERTICAL);
                                                        TextView title = new TextView(mContext);
                                                        title.setTextColor(Color.BLACK);
                                                        title.setGravity(Gravity.CENTER);
                                                        title.setTypeface(null, Typeface.BOLD);
                                                        title.setText(marker.getTitle());
                                                        TextView snippet = new TextView(mContext);
                                                        snippet.setTextColor(Color.BLACK);
                                                        snippet.setText(marker.getSnippet());
                                                        info.addView(title);
                                                        info.addView(snippet);
                                                        return info;
                                                    }
                                                });
                                            }

                                            if (path.size() > 0) {
                                                line.addAll(path).color(Color.BLUE).width(5);
                                                mMap.addPolyline(line);
                                            }
                                        }

                                        if (getDijkstra.size() > 1 && getDijkstra != null) {
                                            int jumlah = 0;
                                            int j = 1;
                                            int max = getDijkstra.size();

                                            for (MarkerObject get : getDijkstra) {
                                                jumlah = jumlah+get.getWeight();
                                                if (j < max)
                                                {
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                    getToast +=" "+get.getId()+" ("+jumlah+") =>";
                                                }
                                                else
                                                {
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                                    getToast +=" "+get.getId()+" ("+jumlah+")";
                                                }
                                                j++;
                                            }
                                            Toast.makeText(DisplayNavigation.this, ""+getToast, Toast.LENGTH_LONG).show();
                                        }
                                    } else if(getStatus.trim().equals("Failed")){
                                        Toast.makeText(DisplayNavigation.this, ""+getToast, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.d(TAG, "Error: " + error.getMessage());
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        AppController.getInstance().addToRequestQueue(jsonObjReqOrigin);
                    }
                    else
                    {
                        mSearchText.setVisibility(View.VISIBLE);
                        btnRemove.setVisibility(View.GONE);
                        btnSet.setVisibility(View.VISIBLE);
                        Toast.makeText(DisplayNavigation.this, getMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReqOrigin);
    }

    private void geoLocate(){
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(DisplayNavigation.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation == null)
                                Toast.makeText(DisplayNavigation.this, "Unable to get current location. " +
                                        "Make sure your device location is on and click location button.", Toast.LENGTH_LONG).show();
                            else
                            {
                                getMember();
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,"My Location");
                                deviceLat = currentLocation.getLatitude();
                                deviceLng = currentLocation.getLongitude();
                            }
                        }
                        else
                            Toast.makeText(DisplayNavigation.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);
            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(DisplayNavigation.this);
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = DisplayNavigation.this.getCurrentFocus();
        if (v != null) {
            DisplayNavigation.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                places.release();
                return;
            }
            final Place place = places.get(0);
            mPlace = new PlaceInfo();

            try{
                mPlace.setName(place.getName().toString());
                getPlace = place.getName().toString();
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setId(place.getId());
                mPlace.setLatlng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri());

                searchLat = place.getLatLng().latitude;
                searchLng = place.getLatLng().longitude;
                requestURL = BASE_URL+BASE_CONTROLLER+"user/user/deviceLoc/"+deviceLat+","+deviceLng+"/searchLoc/"+searchLat+","+searchLng;
                Log.d(TAG, "JSON Request: "+requestURL);

                JsonObjectRequest jsonObjReqOrigin = new JsonObjectRequest(Request.Method.GET, requestURL,
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String getToast = "";
                            getStatus = response.getString("status").toString();
                            getMessage = response.getString("message").toString();

                            if(getStatus.trim().equals("Success")){
                                mMap.clear();

                                JSONArray marker = response.getJSONArray("markerData");
                                JSONArray dijkstra = response.getJSONArray("markerData");
                                getMarker.clear();
                                getDijkstra.clear();

                                for (int i=0; i < marker.length(); i++){
                                    String id = marker.getJSONObject(i).getString("id").toString();
                                    String value = marker.getJSONObject(i).getString("value").toString();
                                    String weight = marker.getJSONObject(i).getString("weight").toString();
                                    String polyLine = marker.getJSONObject(i).getString("polyline").toString();
                                    getMarker.add(new MarkerObject(id, value, Integer.parseInt(weight), polyLine));
                                }

                                for (int i=0; i < dijkstra.length(); i++){
                                    String id = dijkstra.getJSONObject(i).getString("id").toString();
                                    String value = dijkstra.getJSONObject(i).getString("value").toString();
                                    String weight = dijkstra.getJSONObject(i).getString("weight").toString();
                                    getDijkstra.add(new MarkerObject(id, value, Integer.parseInt(weight)));
                                }

                                if (getMarker.size() > 1 && getMarker != null) {
                                    int i = 1;
                                    int sumTotal = 0;
                                    int max = getMarker.size();

                                    line = new PolylineOptions();
                                    if (path != null || path.size() > 1)
                                        path.clear();

                                    for (MarkerObject get : getMarker) {
                                        String[] latlong =  get.getValue().split(",");
                                        double latitude = Double.parseDouble(latlong[0]);
                                        double longitude = Double.parseDouble(latlong[1]);

                                        options.position(new LatLng(latitude, longitude));
                                        options.title("" + get.getId());
                                        sumTotal = sumTotal+get.getWeight();

                                        if (get.getWeight() == 0)
                                        {
                                            if (get.getId().trim().equals("V0"))
                                                options.snippet("Weight: 0, Value: 0");
                                            else
                                                options.snippet("");
                                        }
                                        else {
                                            if (i < max)
                                                options.snippet("Weight: " + get.getWeight() + ", Value: " + sumTotal);
                                            else
                                                options.snippet("Weight: " + get.getWeight() + ", Value: " + sumTotal +
                                                        "\n Place: " + getPlace);
                                        }
                                        if (i < max)
                                        {
                                            if (get.getPolyLine().trim().equals(""))
                                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                            else
                                            {
                                                if (get.getId().trim().equals("V0"))
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                                else
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                            }
                                        }
                                        else
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                        points = new EncodedPolyline(get.getPolyLine());
                                        if (i < max)
                                        {
                                            if (points != null) {
                                                coords  = points.decodePath();
                                                for (com.google.maps.model.LatLng coord1 : coords) {
                                                    path.add(new LatLng(coord1.lat, coord1.lng));
                                                }
                                            }
                                        }
                                        i++;
                                        mMarker = mMap.addMarker(options);

                                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                            @Override
                                            public View getInfoWindow(Marker arg0) {
                                                return null;
                                            }

                                            @Override
                                            public View getInfoContents(Marker marker) {
                                                Context mContext = getApplicationContext();
                                                LinearLayout info = new LinearLayout(mContext);
                                                info.setOrientation(LinearLayout.VERTICAL);
                                                TextView title = new TextView(mContext);
                                                title.setTextColor(Color.BLACK);
                                                title.setGravity(Gravity.CENTER);
                                                title.setTypeface(null, Typeface.BOLD);
                                                title.setText(marker.getTitle());
                                                TextView snippet = new TextView(mContext);
                                                snippet.setTextColor(Color.BLACK);
                                                snippet.setText(marker.getSnippet());
                                                info.addView(title);
                                                info.addView(snippet);
                                                return info;
                                            }
                                        });
                                    }

                                    if (path.size() > 0) {
                                        line.addAll(path).color(Color.BLUE).width(5);
                                        mMap.addPolyline(line);
                                    }
                                }

                                if (getDijkstra.size() > 1 && getDijkstra != null) {
                                    int jumlah = 0;
                                    int j = 1;
                                    int max = getDijkstra.size();

                                    for (MarkerObject get : getDijkstra) {
                                        jumlah = jumlah+get.getWeight();
                                        if (j < max)
                                        {
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                            getToast +=" "+get.getId()+" ("+jumlah+") =>";
                                        }
                                        else
                                        {
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                            getToast +=" "+get.getId()+" ("+jumlah+")";
                                        }
                                        j++;
                                    }
                                    Toast.makeText(DisplayNavigation.this, "Shortest Path Found: "+getToast, Toast.LENGTH_LONG).show();
                                }
                            }else if(getStatus.trim().equals("Failed")){
                                Toast.makeText(DisplayNavigation.this, ""+getMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                AppController.getInstance().addToRequestQueue(jsonObjReqOrigin);
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(searchLat,
                    searchLng), DEFAULT_ZOOM, mPlace);
            mPlace.setLatlng(place.getLatLng());
            places.release();
        }
    };
}
