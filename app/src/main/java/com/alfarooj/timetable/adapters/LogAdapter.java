package com.alfarooj.timetable.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.models.AttendanceLog;
import java.util.ArrayList;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private ArrayList<AttendanceLog> logList;

    public LogAdapter(ArrayList<AttendanceLog> logList) {
        this.logList = logList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AttendanceLog log = logList.get(position);
        
        String departmentDisplay = "";
        switch(log.getDepartment()) {
            case "kitchen":
                departmentDisplay = "Kitchen";
                break;
            case "waiter":
                departmentDisplay = "Waiter";
                break;
            case "delivery":
                departmentDisplay = "Delivery";
                break;
            case "manager":
                departmentDisplay = "Manager";
                break;
            default:
                departmentDisplay = log.getDepartment();
        }
        
        holder.text1.setText(log.getEventName() + " - " + log.getFullName() + " (" + departmentDisplay + ")");
        holder.text2.setText(log.getTimestamp() + " | " + log.getLocation());
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
