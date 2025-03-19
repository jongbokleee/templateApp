package com.bienbetter.application.api

import com.bienbetter.application.model.HospitalResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HospitalApiService {
    @GET("getRegnHmcList")
    fun getHospitals(
        @Query("hmcNm") hmcNm: String?,
        @Query("locAddr") locAddr: String?,
        @Query("siDoCd") siDoCd: Int?,
        @Query("siGunGuCd") siGunGuCd: Int?,
        @Query("serviceKey", encoded = true) serviceKey: String = "",
        @Query("_type") type: String = "json"
    ): Call<HospitalResponse>
}
