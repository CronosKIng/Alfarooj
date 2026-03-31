package com.alfarooj.timetable.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private ArrayList<AttendanceLog> logList;

    public LogAdapter(ArrayList<AttendanceLog> logList) {
        this.logList = logList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
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
        
        holder.tvFullName.setText("Name: " + log.getFullName());
        holder.tvUsername.setText("Username: " + log.getUsername());
        holder.tvEvent.setText("Event: " + log.getEventName());
        holder.tvDepartment.setText("Department: " + departmentDisplay);
        holder.tvTime.setText("Time: " + log.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUsername, tvEvent, tvDepartment, tvTime;
        
        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEvent = itemView.findViewById(R.id.tvEvent);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
