package com.alfarooj.timetable.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.R;
import com.alfarooj.timetable.adapters.LogAdapter;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SessionManager session;
    private List<AttendanceLog> logList = new ArrayList<>();
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        session = new SessionManager(this);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(TranslationHelper.translateTextDirect("My History"));
        }

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
        translateAllUIElements();
    }

    private void loadHistory() {
        tvEmpty.setVisibility(View.GONE);
        
        ApiClient.getApiService().getAttendanceLogs(null).enqueue(new Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                   Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    logList.clear();
                    // Filter kwa mtumiaji wa sasa
                    String currentUsername = session.getUsername();
                    for (AttendanceLog log : response.body().getLogs()) {
                        if (log.getUsername().equals(currentUsername)) {
                            logList.add(log);
                        }
                    }
                    adapter = new LogAdapter(logList, HistoryActivity.this, null);
                    recyclerView.setAdapter(adapter);
                    tvEmpty.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
