package com.example.randomshakerimage

data class MyMemoryResponse(
    val responseData: ResponseData,
    val quotaFinished: Boolean,
    val mtLangSupported: String?,
    val responseDetails: String,
    val responseStatus: Int,
    val responderId: String?,
    val exception_code: String?,
    val matches: List<Match>
)

data class ResponseData(
    val translatedText: String,
    val match: Double
)

data class Match(
    val id: String,
    val segment: String,
    val translation: String,
    val source: String,
    val target: String,
    val quality: Int,
    val reference: String?,
    val usage_count: Int,
    val subject: String,
    val created_by: String,
    val last_updated_by: String,
    val create_date: String,
    val last_update_date: String,
    val match: Double,
    val penalty: Int
)

//{
//    "responseData": {
//    "translatedText": "Ciao"
//},
//    "quotaFinished": false
//}