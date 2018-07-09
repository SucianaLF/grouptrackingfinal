package com.example.sucianalf.grouptracking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtUserName,edtEmail,edtTlp,edtLocation,edtPassword,edtConfirmPassword;
    private SessionManager session;
    private Button btnEditProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtUserName=(EditText)findViewById(R.id.fullName);
        edtEmail=(EditText)findViewById(R.id.userEmailId);
        edtTlp=(EditText)findViewById(R.id.mobileNumber);
        edtLocation=(EditText)findViewById(R.id.location);
        edtPassword=(EditText)findViewById(R.id.password);
        btnEditProfil = findViewById(R.id.editProfilBtn);

        session = new SessionManager(getApplicationContext());
        edtUserName.setText(session.getUsername());
        edtEmail.setText(session.getEmail());
        edtLocation.setText(session.getAlamat());
        edtTlp.setText(session.getNoTelp());
        edtPassword.setText(session.getPassword());
        Log.d("TAG", "getPassword: " + session.getPassword());
//        Glide.with(this).load(session.getImage()).placeholder(R.drawable.user).into(avatar);

        btnEditProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfil();
            }
        });
    }

    private void editProfil(){
        final String username=edtUserName.getText().toString();
        final String email=edtEmail.getText().toString();
        final String tlp=edtTlp.getText().toString();
        final String location=edtLocation.getText().toString();
        final String password=edtPassword.getText().toString();

        String tag_string_req = "req_register";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Url.FunctionName.EDIT_PROFIL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TAG", "Edit user Response >>>>>>>>>" + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status").toString().trim();
                    if(status.equals("Success")){
                        Log.d("masuk ke get value", " >>>>>>>>> OK!");
                        Toast.makeText(getApplicationContext(), "Profile successfully Updated!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(
                                getApplicationContext(),
                                ProfileActivity.class);
                        startActivity(intent);
//                        finish();
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
                Log.d("TAG", "Registration New Contact Error >>>>>>>>> " + error.getMessage());
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
                params.put("no_telp", tlp);
                params.put("password", password);
                params.put("alamat", location);
                Log.d("param response >>>>>>",params.toString());
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


}