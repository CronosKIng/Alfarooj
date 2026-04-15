package com.alfarooj.timetable.api;

import com.alfarooj.timetable.models.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("translate")
    Call<TranslateResponse> translateText(@Body TranslateRequest request);

    @POST("validate_location")
    Call<LocationResponse> validateLocation(@Body LocationRequest request);

    @POST("attendance")
    Call<AttendanceResponse> recordAttendance(@Body AttendanceRequest request);

    @GET("attendance_logs")
    Call<AttendanceLogsResponse> getAttendanceLogs(@Query("department") String department);

    @GET("today_attendance")
    Call<AttendanceLogsResponse> getTodayAttendance();

    @GET("attendance_by_date")
    Call<AttendanceLogsResponse> getAttendanceByDate(@Query("date") String date);

    @DELETE("delete_attendance/{id}")
    Call<SimpleResponse> deleteAttendanceLog(@Path("id") int logId);

    @GET("users")
    Call<UsersResponse> getUsers();

    @POST("create_user")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);

    @DELETE("delete_user/{id}")
    Call<SimpleResponse> deleteUser(@Path("id") int userId);

    @POST("update_user_department")
    Call<SimpleResponse> updateUserDepartment(@Body UpdateDepartmentRequest request);

    @GET("languages")
    Call<LanguagesResponse> getLanguages();
}
