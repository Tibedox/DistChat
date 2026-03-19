package ru.samsung.distchat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyApi {
    @GET("chat.php")
    Call<List<DataFromDB>> sendQuery(@Query("q") String query);

    @GET("chat.php")
    Call<List<DataFromDB>> sendQuery(@Query("q") String query,
                                     @Query("name") String name,
                                     @Query("message") String message);
}
