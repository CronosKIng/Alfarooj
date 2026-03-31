package com.alfarooj.timetable.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        User user = userList.get(pos);
        String dept = user.getDepartmentDisplay();

        holder.tvFullName.setText("Name: " + user.getFullName());
        holder.tvUsername.setText("Username: " + user.getUsername());
        holder.tvPassword.setText("Password: ******");
        holder.tvDepartment.setText("Department: " + dept);
        holder.tvRole.setText("Role: " + user.getRole());

        // Show password when eye button clicked
        holder.btnShowPassword.setOnClickListener(v -> {
            Toast.makeText(context, "Password for " + user.getUsername() + " is: " + user.getPassword(),
                    Toast.LENGTH_LONG).show();
        });

        holder.btnDelete.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(context);
            if (db.deleteUser(user.getId())) {
                userList.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
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
        ImageButton btnShowPassword;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnShowPassword = itemView.findViewById(R.id.btnShowPassword);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
