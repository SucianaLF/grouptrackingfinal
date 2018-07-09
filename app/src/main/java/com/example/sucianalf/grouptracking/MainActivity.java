package com.example.sucianalf.grouptracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    EditText username, password;
    TextView txtCreateAccount;
    private static final String TAG = "MyActivity";
    private Bundle bundle;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtCreateAccount=findViewById(R.id.createAccount);
        username = findViewById(R.id.login_emailid);
        password = findViewById(R.id.login_password);
        session = new SessionManager(getApplicationContext());

        if(session.isLoggedIn()){
            Intent intent = new Intent(
                    getApplicationContext(),
                    ListGroupActivity.class);
            startActivity(intent);
            finish();
        }

        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getApplicationContext(),
                        SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public void buttonClickFunction(View v)
    {
        requestLogin();
    }

    public void SignUp(View v)
    {
        Intent intent = new Intent(getApplicationContext(), SignUp.class);
        startActivity(intent);
    }

    public void requestLogin()
    {
        final String uname = username.getText().toString();
        final String pass = password.getText().toString();

        String urlRequest = Url.FunctionName.LOGIN+"username/"+uname+"/password/"+pass;
        Log.i(TAG, "urlRequest: "+urlRequest);
        JsonObjectRequest prosesRequest = new JsonObjectRequest(urlRequest,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status").trim();
                    if(status.equals("Success")){
                        JSONArray data = response.getJSONArray("userDetail");
                        try {
                            JSONObject obj = data.getJSONObject(0);
                            String username = obj.getString("username").toString();
                            String email = obj.getString("email").toString();
                            String no_telp = obj.getString("no_telp").toString();
                            String alamat = obj.getString("alamat").toString();
                            String image = obj.getString("image_user").toString();
                            String password = obj.getString("password").toString();

                            session.setAlamat(alamat);
                            session.setUsername(username);
                            session.setEmail(email);
                            session.setNoTelp(no_telp);
                            session.setImage(image);
                            session.setPassword(password);

                            Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                            session.setLogin(true);
                            Intent intent = new Intent(getApplicationContext(), ListGroupActivity.class);
                            bundle = new Bundle();
                            bundle.putString("username", uname);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }

                    }else{
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(prosesRequest);
    }
}
