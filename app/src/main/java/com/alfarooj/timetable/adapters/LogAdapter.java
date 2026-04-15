package com.alfarooj.timetable.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.R;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.TranslationHelper;
import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private List<AttendanceLog> logs;
    private Context context;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(AttendanceLog log);
    }

    public LogAdapter(List<AttendanceLog> logs, Context context, OnDeleteClickListener listener) {
        this.logs = logs != null ? logs : new ArrayList<>();
        this.context = context;
        this.deleteClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AttendanceLog log = logs.get(position);
        
        holder.tvFullName.setText(log.getFullName());
        holder.tvUsername.setText("@" + log.getUsername());
        holder.tvEvent.setText(TranslationHelper.translateTextDirect(log.getEventName()));
        holder.tvDepartment.setText(log.getDepartment() != null ? log.getDepartment() : "");
        holder.tvTimestamp.setText(log.getTimestamp());
        
        // Comment
        String comment = log.getComment();
        if (comment != null && !comment.isEmpty()) {
            holder.tvComment.setText("💬 " + comment);
            holder.tvComment.setVisibility(View.VISIBLE);
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }
        
        // Order Type
        String orderType = log.getOrderType();
        if (orderType != null && !orderType.isEmpty()) {
            String displayOrder = orderType.equals("pickup") ? "📦 Pickup" : 
                                  orderType.equals("dropoff") ? "✅ Dropoff" : orderType;
            holder.tvOrderType.setText(displayOrder);
            holder.tvOrderType.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderType.setVisibility(View.GONE);
        }
        
        // Delete button
        if (holder.btnDelete != null && deleteClickListener != null) {
            holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(log));
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUsername, tvEvent, tvDepartment, tvComment, tvOrderType, tvTimestamp;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEvent = itemView.findViewById(R.id.tvEvent);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvOrderType = itemView.findViewById(R.id.tvOrderType);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
