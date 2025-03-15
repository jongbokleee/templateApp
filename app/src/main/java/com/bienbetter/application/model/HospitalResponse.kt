package com.bienbetter.application.model

import com.google.gson.annotations.SerializedName

data class HospitalResponse(
    @SerializedName("response") val response: ResponseBody
)

data class ResponseBody(
    @SerializedName("body") val body: Body
)

data class Body(
    @SerializedName("items") val items: Items
)

data class Items(
    @SerializedName("item") val item: List<HospitalItem>
)

data class HospitalItem(
    @SerializedName("hmcNm") val hmcNm: String,  // 병원 이름
    @SerializedName("hmcTelNo") val hmcTelNo: String, // 병원 전화번호
    @SerializedName("locAddr") val locAddr: String, // 병원 주소
    @SerializedName("siDoCd") val siDoCd: Int,  // 시도 코드
    @SerializedName("siGunGuCd") val siGunGuCd: Int, // 시군구 코드
    @SerializedName("ykindnm") val ykindnm: String // 병원 종류
)
