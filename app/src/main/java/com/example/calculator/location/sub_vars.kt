package com.example.calculator.location

//class location info
data class info(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float,
    val time: Long,
    val net_type: String? = null,
    val signal_lvl: String? = null
)

//map for translate code networkType to string
val networkType = mapOf(
    0 to "undefined",
    1 to "2G (GPRS)",
    2 to "2G (EDGE)",
    3 to "3G (UMTS)",
    4 to "2G (CDMA)",
    5 to "3G (EV-DO)",
    6 to "3G (EV-DO)",
    7 to "2G (1xRTT)",
    8 to "3G (HSDPA)",
    9 to "3G (HSUPA)",
    10 to "3G (HSPA)",
    11 to "2G (iDEN)",
    12 to "3G (EV-DO)",
    13 to "4G (LTE)",
    14 to "3G (eHRPD)",
    15 to "3G (HSPA+)",
    16 to "2G (GSM)",
    17 to "3G (TD-SCDMA)",
    18 to "Wi-Fi",
    19 to "4G+",
    20 to "5G"
)

//map for translate code signal lvl to string
val signal_level = mapOf(
    0 to "No signal",
    1 to "Bad",
    2 to "No good",
    3 to "Good",
    4 to "Great"
)