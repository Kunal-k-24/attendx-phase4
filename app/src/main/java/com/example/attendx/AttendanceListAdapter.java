package com.example.attendx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AttendanceListAdapter extends ArrayAdapter<String> {

    public AttendanceListAdapter(Context context, List<String> studentList) {
        super(context, 0, studentList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_attendance, parent, false);
        }

        TextView studentNameText = convertView.findViewById(R.id.studentNameText);
        studentNameText.setText(getItem(position));

        return convertView;
    }
}
