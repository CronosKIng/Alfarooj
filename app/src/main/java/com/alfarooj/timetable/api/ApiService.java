package com.alfarooj.timetable.api;

import com.alfarooj.timetable.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {
    
    // 1. LOGIN
    @POST("/api/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    // 2. VALIDATE LOCATION
    @POST("/api/validate_location")
    Call<LocationResponse> validateLocation(@Body LocationRequest request);
    
    // 3. RECORD ATTENDANCE (Sign In/Out)
    @POST("/api/attendance")
    Call<AttendanceResponse> recordAttendance(@Body AttendanceRequest request);
    
    // 4. CREATE USER
    @POST("/api/create_user")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);
    
    // 5. GET ALL USERS
    @GET("/api/users")
    Call<UsersResponse> getUsers();
    
    // 6. DELETE USER
    @DELETE("/api/delete_user/{id}")
    Call<DeleteUserResponse> deleteUser(@Path("id") int userId);
    
    // 7. GET ATTENDANCE LOGS
    @GET("/api/attendance_logs")
    Call<AttendanceLogsResponse> getAttendanceLogs(@Query("department") String department);
    
    // 8. GET TODAY'S ATTENDANCE
    @GET("/api/today_attendance")
    Call<AttendanceLogsResponse> getTodayAttendance();
    
    // 9. TRANSLATE SINGLE TEXT
    @POST("/api/translate")
    Call<TranslateResponse> translateText(@Body TranslateRequest request);
    
    // 10. BATCH TRANSLATE
    @POST("/api/batch_translate")
    Call<BatchTranslateResponse> batchTranslate(@Body BatchTranslateRequest request);
    
    // 11. GET SUPPORTED LANGUAGES
    @GET("/api/languages")
    Call<LanguagesResponse> getLanguages();
    
    // 12. HEALTH CHECK
    @GET("/api/health")
    Call<HealthResponse> healthCheck();
}
