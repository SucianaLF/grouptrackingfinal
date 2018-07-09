package com.example.sucianalf.grouptracking;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sucianalf.grouptracking.Adapter.GroupAdapter;
import com.example.sucianalf.grouptracking.Model.Object_Mhsw_Dosen;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListGroupActivity extends AppCompatActivity {
    public Bundle getBundle;
    private ListView list;
    private TextView txtKosong;
    List<Object_Mhsw_Dosen> groupList = new ArrayList<>();
    GroupAdapter groupAdapter;
    EditText namaGroup;
    private String TAG = ListGroupActivity.class.getSimpleName();
    private String groupName = "";
    private SessionManager session;
    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_group);
        getBundle = getIntent().getExtras();
        session = new SessionManager(getApplicationContext());

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
        }

        groupAdapter = new GroupAdapter(ListGroupActivity.this, groupList);
        list = findViewById(R.id.list_group);
        txtKosong = findViewById(R.id.kosong);
        groupList.clear();
        list.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
        getGroup(session.getUsername());
        initToobar();

        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String idGroup = ((TextView) view.findViewById(R.id.idGroup)).getText().toString();
                        final String namaGroup = ((TextView) view.findViewById(R.id.groupName)).getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString("idGroup", idGroup);
                        bundle.putString("namaGroup", namaGroup);
                        Intent intent = new Intent(getApplicationContext(), ListGroupMemberActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
        );
    }

    private void startTrackerService() {
        session.getUsername();
        startService(new Intent(this, TrackerService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 3
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } else {
            Toast.makeText(this, "Izinkan aplikasi untuk melanjutkan penggunaan",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void initToobar() {
        Toolbar addToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(addToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("LIST GROUP");
    }

    private void getGroup(final String username) {
        String urlRequest = Url.FunctionName.SELECT_RELATED_GROUP + "username/" + username;
        Log.i(TAG, "urlRequest: " + urlRequest);
        JsonObjectRequest prosesRequest = new JsonObjectRequest(urlRequest,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status").toString().trim();
                    if(status.equals("Success")){
                        JSONArray data = response.getJSONArray("group_detail");
                        for (int i = 0; i < data.length(); i++) {
                            try {
                                Object_Mhsw_Dosen historyData = new Object_Mhsw_Dosen();
                                JSONObject obj = data.getJSONObject(i);
                                historyData.setGroupID(obj.getString("id_grup"));
                                historyData.setNama(obj.getString("nama_grup"));
                                groupList.add(historyData);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        txtKosong.setText("Belum ada group");
                        txtKosong.setVisibility(View.VISIBLE);
                        list.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // notifikasi adanya perubahan data pada adapter
                groupAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void initAdd() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_group);
        Button btCancel = (Button) dialog.findViewById(R.id.dialog_cancel);
        Button btOk = (Button) dialog.findViewById(R.id.dialog_ok);
        namaGroup = dialog.findViewById(R.id.InputNamaGroup);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.getUsername().equalsIgnoreCase("") || session.getUsername() == null) {
                    Toast.makeText(ListGroupActivity.this, "username tidak tercatat di session ketika login", Toast.LENGTH_SHORT).show();
                } else {
                    groupName = namaGroup.getText().toString();
                    if (groupName.equalsIgnoreCase("") || groupName == null) {
                        Toast.makeText(ListGroupActivity.this, "nama group tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    } else {
                        addNewGroup(groupName);
                    }
                }
            }
        });
        dialog.show();
    }

    private void addNewGroup(final String groupName) {
        String tag_string_req = "insert_new_group";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Url.FunctionName.INSERT_NEW_GROUP, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Adding New user Response >>>>>>>>>" + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status").toString().trim();
                    if(status.equals("Success")){
                        Log.d("masuk ke get value", " >>>>>>>>> OK!");
                        String message = jObj.getString("message");
                        Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_LONG).show();
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
                params.put("username", session.getUsername());
                params.put("namagrup", groupName);
                Log.d("param response >>>>>>",params.toString());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {

            initAdd();
        } else if (id == R.id.action_logout) {
            session.setLogin(false);
            Toast.makeText(getApplicationContext(), "Logout Successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ListGroupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_profile) {

            Intent intent = new Intent(ListGroupActivity.this, ProfileActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


}
