package com.example.sucianalf.grouptracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.sucianalf.grouptracking.URL.Url;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtUserName,edtEmail,edtTlp,edtLocation,edtPassword,edtConfirmPassword;
    private Button btnSignUp;
    private TextView txtAlready;
    private String TAG = SignUpActivity.class.getSimpleName();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public double deviceLat, deviceLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initComponent();
//        getLocationPermission();
        //getDeviceLocation();
        txtAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                    @Override
                    public void idsAvailable(String userId, String registrationId) {
                        if (registrationId != null){
                            registerUser(userId);
                        }else{
                            Toast.makeText(getApplicationContext(), "ID ONE SIGNAL KOSONG, RESTART APLIKASI SEKARANG", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
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
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }


//    private void getDeviceLocation(){
//        Log.d(TAG, "getDeviceLocation: getting the devices current location");
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        try{
//            if(mLocationPermissionsGranted){
//                final Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()){
//                            Log.d(TAG, "onComplete: found location!");
//                            Location currentLocation = (Location) task.getResult();
//                            Log.d(TAG, "origin latlng" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() );
//                            deviceLat = currentLocation.getLatitude();
//                            deviceLng = currentLocation.getLongitude();
//                        }else{
//                            Log.d(TAG, "onComplete: current location is null");
//                            Toast.makeText(SignUpActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        }catch (SecurityException e){
//            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
//        }
//    }
    private void initComponent(){
        edtUserName=(EditText)findViewById(R.id.fullName);
        edtEmail=(EditText)findViewById(R.id.userEmailId);
        edtTlp=(EditText)findViewById(R.id.mobileNumber);
        edtLocation=(EditText)findViewById(R.id.location);
        edtPassword=(EditText)findViewById(R.id.password);
        edtConfirmPassword=(EditText)findViewById(R.id.confirmPassword);
        btnSignUp=(Button)findViewById(R.id.signUpBtn);
        txtAlready=(TextView)findViewById(R.id.already_user);
    }

    private void registerUser(final String oneSignalID){

        final String username=edtUserName.getText().toString();
        final String email=edtEmail.getText().toString();
        final String tlp=edtTlp.getText().toString();
        final String location=edtLocation.getText().toString();
        final String passowrd=edtPassword.getText().toString();
        final String confirmPassowrd=edtConfirmPassword.getText().toString();
        final String koordinat = String.valueOf(deviceLat) + "," + String.valueOf(deviceLng);

        if(passowrd.equalsIgnoreCase(confirmPassowrd)||passowrd==confirmPassowrd){
            if(username.equalsIgnoreCase("")||username==null){
                Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show();
            }else{
                if(email.equalsIgnoreCase("")||email==null){
                    Toast.makeText(this, "Email harus diisi", Toast.LENGTH_SHORT).show();
                }else{
                    if(tlp.equalsIgnoreCase("")||tlp==null){
                        Toast.makeText(this, "Phone harus diisi", Toast.LENGTH_SHORT).show();
                    }else{

                        if(passowrd.equalsIgnoreCase("")||confirmPassowrd==null){
                            Toast.makeText(this, "password/confirm password harus diisi", Toast.LENGTH_SHORT).show();

                        }else{
                            String tag_string_req = "req_register";
                            StringRequest strReq = new StringRequest(Request.Method.POST,
                                    Url.FunctionName.REGISTER_NEW_USER, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "Adding New user Response >>>>>>>>>" + response.toString());
                                    try {
                                        JSONObject jObj = new JSONObject(response);
                                        String status = jObj.getString("status").toString().trim();
                                        if(status.equals("Success")){
                                            Log.d("masuk ke get value", " >>>>>>>>> OK!");
                                            Toast.makeText(getApplicationContext(), "you successfully registered!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(
                                                    getApplicationContext(),
                                                    MainActivity.class);
                                            startActivity(intent);
                                            finish();
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
                                    Log.d(TAG, "Registration New Contact Error >>>>>>>>> " + error.getMessage());
                                    Toast.makeText(getApplicationContext(),
                                            error.getMessage(), Toast.LENGTH_LONG).show();
                                    error.printStackTrace();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    // Posting params to register url
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("username", username);
                                    params.put("email", email);
                                    params.put("alamat", location);
                                    params.put("tlp", tlp);
                                    params.put("password", confirmPassowrd);
                                    params.put("oneSignalID", oneSignalID);
                                    params.put("koordinat", koordinat);
                                    Log.d("param response >>>>>>",params.toString());
                                    return params;
                                }
                            };
                            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                        }
                    }
                }
            }
        }else{
            Toast.makeText(this, "Password tidak sama, harap diisi dengan benar dan tidak boleh kosong", Toast.LENGTH_SHORT).show();
        }
    }
}
