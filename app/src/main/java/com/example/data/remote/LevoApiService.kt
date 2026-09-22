package com.example.data.remote

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class DeliveryDto(
    val id: String,
    val senderName: String,
    val senderAddress: String,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val packageDescription: String,
    val packageWeightKg: Double,
    val deliveryFee: Double,
    val paymentMethod: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val confirmationProof: String? = null
)

@JsonClass(generateAdapter = true)
data class StatusUpdateDto(
    val status: String,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ConfirmDeliveryDto(
    val proof: String,
    val confirmedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class PushTokenRegistrationDto(
    val courierId: String,
    val token: String,
    val deviceModel: String = "Android Device"
)

@JsonClass(generateAdapter = true)
data class BackendHealthDto(
    val status: String = "ok",
    val app: String = "Levo Backend",
    val timestamp: Long = System.currentTimeMillis()
)

interface LevoApiService {
    @GET("api/health")
    suspend fun checkHealth(): Response<BackendHealthDto>

    @GET("api/deliveries")
    suspend fun getDeliveries(): Response<List<DeliveryDto>>

    @POST("api/deliveries")
    suspend fun createDelivery(@Body delivery: DeliveryDto): Response<DeliveryDto>

    @PUT("api/deliveries/{id}/status")
    suspend fun updateDeliveryStatus(
        @Path("id") id: String,
        @Body body: StatusUpdateDto
    ): Response<DeliveryDto>

    @POST("api/deliveries/{id}/confirm")
    suspend fun confirmDelivery(
        @Path("id") id: String,
        @Body body: ConfirmDeliveryDto
    ): Response<DeliveryDto>

    @POST("api/courier/push-token")
    suspend fun registerPushToken(
        @Body body: PushTokenRegistrationDto
    ): Response<Map<String, String>>
}

object NetworkClient {
    private var currentBaseUrl: String = "http://10.0.2.2:3000/"
    private var apiService: LevoApiService? = null

    fun getApiService(baseUrl: String = currentBaseUrl): LevoApiService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (apiService == null || currentBaseUrl != sanitizedUrl) {
            currentBaseUrl = sanitizedUrl
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            apiService = retrofit.create(LevoApiService::class.java)
        }
        return apiService!!
    }
}
