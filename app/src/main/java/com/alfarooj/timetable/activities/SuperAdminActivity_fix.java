    private void loadHistoryByDepartment(String department) {
        String title = "";
        switch(department) {
            case "kitchen": title = TranslationHelper.translateTextDirect("Kitchen History"); break;
            case "waiter": title = TranslationHelper.translateTextDirect("Waiter History"); break;
            case "delivery": title = TranslationHelper.translateTextDirect("Delivery History"); break;
            case "manager": title = TranslationHelper.translateTextDirect("Manager History"); break;
        }
        final String finalTitle = title;
        setTitle(finalTitle);
        
        ApiClient.getApiService().getAttendanceLogs(department)
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        if (logList.isEmpty()) {
                            Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("No ") + finalTitle + " " + TranslationHelper.translateTextDirect("records found"), Toast.LENGTH_SHORT).show();
                        }
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Failed to load history"), Toast.LENGTH_SHORT).show();
                }
            });
    }
