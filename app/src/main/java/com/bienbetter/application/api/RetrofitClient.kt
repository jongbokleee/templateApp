import com.bienbetter.application.api.HospitalApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://apis.data.go.kr/openapi/service/rest/HmcSearchService/"

    private val gson: Gson = GsonBuilder()
        .setLenient()  // ✅ Gson을 더 유연하게 설정
        .create()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: HospitalApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))  // ✅ setLenient() 적용된 Gson 사용
            .build()

        retrofit.create(HospitalApiService::class.java)
    }
}
