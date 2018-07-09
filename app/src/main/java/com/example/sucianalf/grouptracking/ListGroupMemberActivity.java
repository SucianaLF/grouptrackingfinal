package com.example.sucianalf.grouptracking;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sucianalf.grouptracking.Adapter.MemberGroupAdapter;
import com.example.sucianalf.grouptracking.Model.DataGroupMember;
import com.example.sucianalf.grouptracking.URL.Url;
import com.example.sucianalf.grouptracking.util.SessionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ListGroupMemberActivity extends AppCompatActivity {
    public Bundle getBundle;
    private ListView list;
    private TextView txtKosong;
    private ImageView imgUser;
    private SessionManager sessionManager;
    List<DataGroupMember> groupMemberList = new ArrayList<>();
    MemberGroupAdapter memberGroupAdapter;
    private  String TAG = ListGroupMemberActivity.class.getSimpleName();
    private String namaGroup,idGroup,memberName ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_group_member);
        getBundle = getIntent().getExtras();
        sessionManager = new SessionManager(getApplicationContext());
        idGroup= getBundle.getString("idGroup");
        namaGroup= getBundle.getString("namaGroup");
        memberGroupAdapter = new MemberGroupAdapter (ListGroupMemberActivity.this,groupMemberList);
        list = findViewById(R.id.list_group_member);
        txtKosong= findViewById(R.id.kosong);
        imgUser = findViewById(R.id.icon);
        groupMemberList.clear();
        list.setAdapter(memberGroupAdapter);
        memberGroupAdapter.notifyDataSetChanged();
        initToobar();
        getMembers(idGroup);
    }

    private void initToobar(){
        Toolbar addToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(addToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("LIST MEMBER");
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void getMembers(final String idGroup){
        JsonObjectRequest prosesRequest = new JsonObjectRequest(Url.FunctionName.SELECT_RELATED_GROUP_MEMBER+idGroup+"/username/"+sessionManager.getUsername(),null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status").toString().trim();
                    if(status.equals("Success")){
                        JSONArray data = response.getJSONArray("member_detail");
                        for(int i=0; i<data.length(); i++){
                            try {
                                DataGroupMember dataGroupMember= new DataGroupMember();
                                JSONObject obj = data.getJSONObject(i);
                                dataGroupMember.setMemberID(obj.getString("id_anggota"));
                                dataGroupMember.setMemberName(obj.getString("id_user"));
                                dataGroupMember.setMemberPhoto(obj.getString("image_user"));
                                // menambah item ke array
                                groupMemberList.add(dataGroupMember);
                            }
                            catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                    }else{
                        txtKosong.setText("Tidak ada member");
                        txtKosong.setVisibility(View.VISIBLE);
                        list.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // notifikasi adanya perubahan data pada adapter
                memberGroupAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

            }
        });
//         Adding request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(prosesRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_member_group, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    public void initAdd(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_member);
        Button btCancel = dialog.findViewById(R.id.dialog_cancel);
        Button btOk = dialog.findViewById(R.id.dialog_ok);
        final EditText edtMemberName = dialog.findViewById(R.id.InputNamaGroup);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memberName= edtMemberName.getText().toString();
                if(memberName.equalsIgnoreCase("")||memberName==null){
                    Toast.makeText(getApplicationContext(), "nama group tidak boleh kosong", Toast.LENGTH_SHORT).show();
                  }else{
                    addNewGroupMember(memberName);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void deleteGroup(String user_id, String id_group){
        JsonObjectRequest prosesRequest = new JsonObjectRequest(Url.FunctionName.DELETE_GROUP_MEMBER+id_group+"/username/"+user_id,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String getStatus = response.getString("status").toString().trim();
                    String getMessage = response.getString("message").toString().trim();

                    if(getStatus.equals("Success")){
                        Toast.makeText(ListGroupMemberActivity.this, ""+getMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ListGroupMemberActivity.this, ""+getMessage, Toast.LENGTH_LONG).show();
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
    private void addNewGroupMember(final String memberName )
    {
        String tag_string_req = "req_register";
        JsonObjectRequest prosesRequest = new JsonObjectRequest(Url.FunctionName.INSERT_NEW_GROUP_MEMBER+idGroup+"/user/"+memberName,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status").toString().trim();
                    if(status.equals("Success")){
                        String message = response.getString("message");
                        Toast.makeText(getApplicationContext(), ""+message.toString(), Toast.LENGTH_LONG).show();

                        // Launch All lead activity
                        Intent intent = new Intent(
                                getApplicationContext(),
                                ListGroupActivity.class);
                        startActivity(intent);
                        finish();
                    } else if(status.equals("Failed")){
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
        AppController.getInstance().addToRequestQueue(prosesRequest, tag_string_req);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            initAdd();
        }else if(id==R.id.action_map){
            Intent intent = new Intent(ListGroupMemberActivity.this,DisplayNavigation.class);
            intent.putExtra("groupID", Integer.parseInt(idGroup));
            startActivity(intent);
        }

        else if(id == R.id.action_delete) {
            deleteGroup(sessionManager.getUsername(), idGroup);
            Intent intent = new Intent(ListGroupMemberActivity.this, ListGroupActivity.class);
            startActivity(intent);
        }
        else if(id == android.R.id.home){
            ListGroupMemberActivity.this.finish();
        }
        // todo: goto back activity from here


        return super.onOptionsItemSelected(item);
    }

}
