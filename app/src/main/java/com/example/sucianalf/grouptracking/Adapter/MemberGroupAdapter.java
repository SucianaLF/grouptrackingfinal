package com.example.sucianalf.grouptracking.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sucianalf.grouptracking.Model.DataGroupMember;
import com.example.sucianalf.grouptracking.Model.Object_Mhsw_Dosen;
import com.example.sucianalf.grouptracking.R;

import java.util.List;

/**
 * Created by PERSONAL on 6/11/2018.
 */

public class MemberGroupAdapter  extends BaseAdapter{
    private Activity activity;
    private LayoutInflater inflater;
    private List<DataGroupMember>  members;

    public MemberGroupAdapter (Activity activity,List<DataGroupMember> items ){
        this.activity = activity;
        this.members= items;
    }
    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(inflater==null)
            inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView==null)
            convertView=inflater.inflate(R.layout.list_group_member,null);
        TextView txtIDGroup= convertView.findViewById(R.id.idGroupMember);
        TextView txtGroupName= convertView.findViewById(R.id.groupMemberName);
        ImageView imageUser = convertView.findViewById(R.id.icon);

        DataGroupMember memberGroupData = members.get(position);
        txtGroupName.setText(memberGroupData.getMemberName());
        txtIDGroup.setText(memberGroupData.getMemberID());
        Glide.with(activity).load(memberGroupData.getMemberPhoto()).placeholder(R.drawable.user).into(imageUser);

        return convertView;

    }
}
