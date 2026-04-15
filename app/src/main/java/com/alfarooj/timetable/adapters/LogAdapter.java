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
        
        holder.tvUsername.setText(log.getUsername());
        holder.tvFullName.setText(log.getFullName());
        holder.tvEvent.setText(TranslationHelper.translateTextDirect(log.getEventName()));
        holder.tvTimestamp.setText(log.getTimestamp());
        
        // Comment na OrderType zinaweza kuwa null
        try {
            String comment = log.getComment();
            if (comment != null && !comment.isEmpty()) {
                holder.tvComment.setText("💬 " + comment);
                holder.tvComment.setVisibility(View.VISIBLE);
            } else {
                holder.tvComment.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.tvComment.setVisibility(View.GONE);
        }
        
        try {
            String orderType = log.getOrderType();
            if (orderType != null && !orderType.isEmpty()) {
                holder.tvOrderType.setText("📦 " + orderType);
                holder.tvOrderType.setVisibility(View.VISIBLE);
            } else {
                holder.tvOrderType.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.tvOrderType.setVisibility(View.GONE);
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(log);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvFullName, tvEvent, tvTimestamp, tvComment, tvOrderType;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEvent = itemView.findViewById(R.id.tvEvent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvOrderType = itemView.findViewById(R.id.tvOrderType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
