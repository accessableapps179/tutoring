// app/src/main/java/com/marketplace/api/RetrofitClient.kt
package com.marketplace.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {

    private const val EMULATOR_BASE    = "10.0.2.2"
    private const val REAL_DEVICE_BASE = "192.168.0.95"

    private var useRealDevice = false
    private var token: String = ""

    fun setUseRealDevice(value: Boolean) {
        useRealDevice = value
        android.util.Log.d("RETROFIT", "Switching to ${if (value) REAL_DEVICE_BASE else EMULATOR_BASE}")
        rebuildClients()
    }

    fun setToken(newToken: String) {
        token = newToken
        rebuildClients()
    }

    fun getHost(): String = if (useRealDevice) REAL_DEVICE_BASE else EMULATOR_BASE

    fun getLobbyUrl(roomId: String)      = "ws://${getHost()}:8080/lobby/$roomId"
    fun getWebSocketUrl(roomId: String)  = "ws://${getHost()}:8080/signal/$roomId"
    fun getCallStatusUrl(roomId: String) = "http://${getHost()}:8080/call-status/$roomId"

    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val request = if (token.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private fun buildClient() = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private fun buildRetrofit(): Retrofit {
        val baseUrl = if (useRealDevice) "http://$REAL_DEVICE_BASE:8080/" else "http://$EMULATOR_BASE:8080/"
        android.util.Log.d("RETROFIT", "Building retrofit with $baseUrl")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(buildClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private var retrofit = buildRetrofit()

    private fun rebuildClients() {
        retrofit          = buildRetrofit()
        _teacherApi       = retrofit.create(TeacherApi::class.java)
        _authApi          = retrofit.create(AuthApi::class.java)
        _bookingApi       = retrofit.create(BookingApi::class.java)
        _availabilityApi  = retrofit.create(AvailabilityApi::class.java)
        _contactApi       = retrofit.create(ContactApi::class.java)
        _messageApi       = retrofit.create(MessageApi::class.java)
        _paymentCardApi   = retrofit.create(PaymentCardApi::class.java)
        _trialResultApi   = retrofit.create(TrialResultApi::class.java)
        _ledgerApi        = retrofit.create(LedgerApi::class.java)
        _adminApi         = retrofit.create(AdminApi::class.java)
        _callStatusApi    = retrofit.create(CallStatusApi::class.java)
    }

    private var _teacherApi:      TeacherApi      = retrofit.create(TeacherApi::class.java)
    private var _authApi:         AuthApi         = retrofit.create(AuthApi::class.java)
    private var _bookingApi:      BookingApi      = retrofit.create(BookingApi::class.java)
    private var _availabilityApi: AvailabilityApi = retrofit.create(AvailabilityApi::class.java)
    private var _contactApi:      ContactApi      = retrofit.create(ContactApi::class.java)
    private var _messageApi:      MessageApi      = retrofit.create(MessageApi::class.java)
    private var _paymentCardApi:  PaymentCardApi  = retrofit.create(PaymentCardApi::class.java)
    private var _trialResultApi:  TrialResultApi  = retrofit.create(TrialResultApi::class.java)
    private var _ledgerApi:       LedgerApi       = retrofit.create(LedgerApi::class.java)
    private var _adminApi:        AdminApi        = retrofit.create(AdminApi::class.java)
    private var _callStatusApi:   CallStatusApi   = retrofit.create(CallStatusApi::class.java)

    val teacherApi:      TeacherApi      get() = _teacherApi
    val authApi:         AuthApi         get() = _authApi
    val bookingApi:      BookingApi      get() = _bookingApi
    val availabilityApi: AvailabilityApi get() = _availabilityApi
    val contactApi:      ContactApi      get() = _contactApi
    val messageApi:      MessageApi      get() = _messageApi
    val paymentCardApi:  PaymentCardApi  get() = _paymentCardApi
    val trialResultApi:  TrialResultApi  get() = _trialResultApi
    val ledgerApi:       LedgerApi       get() = _ledgerApi
    val adminApi:        AdminApi        get() = _adminApi
    val callStatusApi:   CallStatusApi   get() = _callStatusApi
}