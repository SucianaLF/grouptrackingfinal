package com.example.sucianalf.grouptracking;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sucianalf.grouptracking.Model.Object_Mhsw_Dosen;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {


    private String TAG = ProfileActivity.class.getSimpleName();
    private TextView user, email, alamat, no_telp;
    private ImageView avatar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initToobar();
        user = findViewById(R.id.user);
        email = findViewById(R.id.email);
        alamat = findViewById(R.id.alamat);
        no_telp = findViewById(R.id.tlp);
        avatar = findViewById(R.id.avatar);

        session = new SessionManager(getApplicationContext());
        user.setText(session.getUsername());
        email.setText(session.getEmail());
        alamat.setText(session.getAlamat());
        no_telp.setText(session.getNoTelp());
        Glide.with(this).load(session.getImage()).placeholder(R.drawable.user).into(avatar);

    }

    private void initToobar() {
        Toolbar addToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(addToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Profile");
//        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);


        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            ProfileActivity.this.finish();
        }else if(id == R.id.action_edit){
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));

        }
        // todo: goto back activity from here


        return super.onOptionsItemSelected(item);
    }

//    private void getProfil(final String username) {
//        String urlRequest = Url.FunctionName.GET_PROFIL + username;
//        Log.i(TAG, "urlRequest: " + urlRequest);
//        JsonObjectRequest prosesRequest = new JsonObjectRequest(urlRequest,null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//
//                    String status = response.getString("status").toString().trim();
//                    if(status.equals("Success")){
//                        JSONArray data = response.getJSONArray("profil");
//                            try {
//                                JSONObject obj = data.getJSONObject(0);
//                                String username = obj.getString("username").toString();
//                                user.setText(username);
//                            } catch (JSONException ex) {
//                                ex.printStackTrace();
//                            }
//                    } else {
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//
//            }
//        });
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        requestQueue.add(prosesRequest);
//    }
}
