package com.alfarooj.timetable.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private ArrayList<User> userList;
    private Context context;
    private OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onUserDeleted();
    }

    public UserAdapter(ArrayList<User> userList, Context context, OnUserDeleteListener listener) {
        this.userList = userList;
        this.context = context;
        this.deleteListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        
        String departmentDisplay = "";
        switch(user.getDepartment()) {
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
                departmentDisplay = user.getDepartment();
        }
        
        holder.tvFullName.setText("Name: " + user.getFullName());
        holder.tvUsername.setText("Username: " + user.getUsername());
        holder.tvPassword.setText("Password: ******");
        holder.tvDepartment.setText("Department: " + departmentDisplay);
        holder.tvRole.setText("Role: " + user.getRole());

        holder.btnDelete.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(context);
            if (db.deleteUser(user.getId())) {
                userList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                if (deleteListener != null) deleteListener.onUserDeleted();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUsername, tvPassword, tvDepartment, tvRole;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
