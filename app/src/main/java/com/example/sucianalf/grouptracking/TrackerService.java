package com.example.sucianalf.grouptracking;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.sucianalf.grouptracking.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.sucianalf.grouptracking.URL.Url;

import java.util.Random;

public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();
    SessionManager session;
    public String username;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        session = new SessionManager(getApplicationContext());
        buildNotification();
        requestLocationUpdates();
    }

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_gps);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    private void requestLocationUpdates() {
        // Functionality coming next step

        LocationRequest request = new LocationRequest();
        request.setInterval(60000);
        request.setFastestInterval(30000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "location update " + location);
                        if (session.getUsername() == null || session.getUsername() == "")
                            username = "";
                        else
                        {
                            String userLocation = ""+location.getLatitude()+","+location.getLongitude();
                            username = session.getUsername();
                            String requestURL = Url.FunctionName.UPDATE_LOC + username+ "/lokasi/" + userLocation;
                            Log.d(TAG, "JSON Request: "+requestURL);

                            JsonObjectRequest jsonObjReqOrigin = new JsonObjectRequest(Request.Method.GET, requestURL,
                                    null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String getStatus = response.getString("status").toString();
                                        String getMessages = response.getString("message").toString();

                                        if(getStatus.trim().equals("Success"))
                                        {
                                            Random ran = new Random();
                                            int x = ran.nextInt(6) + 5;
                                            Intent intent = new Intent("intentKey");
                                            intent.putExtra("serviceTrack", "update");
                                            LocalBroadcastManager.getInstance(TrackerService.this).sendBroadcast(intent);
                                        }
                                        else if(getStatus.trim().equals("Failed"))
                                            Toast.makeText(TrackerService.this, getMessages, Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    VolleyLog.d(TAG, "Error: " + error.getMessage());
//                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            AppController.getInstance().addToRequestQueue(jsonObjReqOrigin);
                        }
                    }
                }
            }, null);
        }
    }

}