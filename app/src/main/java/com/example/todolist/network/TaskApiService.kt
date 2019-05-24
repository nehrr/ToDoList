package com.example.android.todolist.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://beta.todoist.com/API/v8/"
private const val TOKEN = "9e0eb8daac9d2aa47841a15003d56c30c6f4549e";

private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

val okHttpClient =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .build()
            chain.proceed(newRequest)
        }.build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()

val taskService: TaskApiService by lazy { retrofit.create(TaskApiService::class.java) }

interface TaskApiService {

    @GET("tasks")
    @Headers("Content-Type: application/json")
    fun getTasks():
            Deferred<List<Task>>

    @POST("tasks")
    @Headers("Content-Type: application/json")
    fun addTask(@Body task: Task):
            Deferred<Task>

    @DELETE("tasks/{id}")
    fun deleteTask(@Path("id") id: String):
            Deferred<Task>

    @POST("tasks/{id}/close")
    fun closeTask(@Path("id") id: String): Deferred<Response<ResponseBody>>

    @POST("tasks/{id}/reopen")
    fun openTask(@Path("id") id: String): Deferred<Response<ResponseBody>>

    //@POST()

    @FormUrlEncoded
    @POST("https://todoist.com/API/v8.1/items/get_completed")
    suspend fun getCompletedTasks(
        @Field("offset") offset: Int = 0,
        @Field("project_id") projectId: Long = 2210626252 // id de votre projet par défaut "inbox"
    ): List<Task>?
}
