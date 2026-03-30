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
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        Button btnDelete = new Button(view.getContext());
        btnDelete.setText("Delete");
        return new ViewHolder(view, btnDelete);
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
        
        holder.text1.setText(user.getFullName() + " (" + user.getUsername() + ")");
        holder.text2.setText("Department: " + departmentDisplay + " | Role: " + user.getRole());

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
        TextView text1, text2;
        Button btnDelete;

        public ViewHolder(View itemView, Button btnDelete) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
            this.btnDelete = btnDelete;
        }
    }
}
