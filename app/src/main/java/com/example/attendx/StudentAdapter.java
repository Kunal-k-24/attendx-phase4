package com.example.attendx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;

public class StudentAdapter extends BaseAdapter {
    private Context context;
    private List<Student> studentList;

    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    @Override
    public int getCount() {
        return studentList.size();
    }

    @Override
    public Object getItem(int position) {
        return studentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.student_list_item, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.studentName);
        TextView enrollText = convertView.findViewById(R.id.studentEnroll);
        CheckBox selectCheckbox = convertView.findViewById(R.id.selectCheckbox);

        Student student = studentList.get(position);
        nameText.setText(student.getName());
        enrollText.setText(student.getEnrollmentNo());
        selectCheckbox.setChecked(student.isSelected());

        selectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> student.setSelected(isChecked));

        return convertView;
    }
}
