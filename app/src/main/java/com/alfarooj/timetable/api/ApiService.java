package com.alfarooj.timetable.api;

import com.alfarooj.timetable.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {
    
    @POST("/api/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("/api/validate_location")
    Call<LocationResponse> validateLocation(@Body LocationRequest request);
    
    @POST("/api/attendance")
    Call<AttendanceResponse> recordAttendance(@Body AttendanceRequest request);
    
    @POST("/api/create_user")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);
    
    @GET("/api/users")
    Call<UsersResponse> getUsers();
    
    @DELETE("/api/delete_user/{id}")
    Call<DeleteUserResponse> deleteUser(@Path("id") int userId);
    
    @POST("/api/update_user_department")
    Call<UpdateDepartmentResponse> updateUserDepartment(@Body UpdateDepartmentRequest request);
    
    @GET("/api/attendance_logs")
    Call<AttendanceLogsResponse> getAttendanceLogs(@Query("department") String department);
    
    @GET("/api/today_attendance")
    Call<AttendanceLogsResponse> getTodayAttendance();
    
    @POST("/api/translate")
    Call<TranslateResponse> translateText(@Body TranslateRequest request);
    
    @POST("/api/batch_translate")
    Call<BatchTranslateResponse> batchTranslate(@Body BatchTranslateRequest request);
    
    @GET("/api/languages")
    Call<LanguagesResponse> getLanguages();
    
    @GET("/api/health")
    Call<HealthResponse> healthCheck();
}
