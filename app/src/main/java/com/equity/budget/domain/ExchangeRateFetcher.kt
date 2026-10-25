package com.equity.budget.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ExchangeRateFetcher {

    data class RatesResult(
        val usdToRub: Double,
        val eurToRub: Double,
        val source: String
    )

    suspend fun fetchCurrentRates(): Result<RatesResult> = withContext(Dispatchers.IO) {
        // Try CBR API first (official Russian Central Bank daily feed)
        val cbrResult = fetchFromCbr()
        if (cbrResult.isSuccess) {
            return@withContext cbrResult
        }

        // Fallback to open exchange rate API
        fetchFromOpenErApi()
    }

    private fun fetchFromCbr(): Result<RatesResult> {
        return try {
            val url = URL("https://www.cbr-xml-daily.ru/daily_json.js")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "EquityBudget/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                conn.disconnect()

                val json = JSONObject(response)
                val valute = json.getJSONObject("Valute")
                val usdVal = valute.getJSONObject("USD").getDouble("Value")
                val eurVal = valute.getJSONObject("EUR").getDouble("Value")

                Result.success(RatesResult(usdToRub = usdVal, eurToRub = eurVal, source = "cbr.ru"))
            } else {
                conn.disconnect()
                Result.failure(Exception("HTTP error ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFromOpenErApi(): Result<RatesResult> {
        return try {
            val url = URL("https://open.er-api.com/v6/latest/USD")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "EquityBudget/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                conn.disconnect()

                val json = JSONObject(response)
                val rates = json.getJSONObject("rates")
                val usdToRub = rates.getDouble("RUB")
                val usdToEur = rates.getDouble("EUR")
                val eurToRub = if (usdToEur > 0) usdToRub / usdToEur else 100.0

                Result.success(RatesResult(usdToRub = usdToRub, eurToRub = eurToRub, source = "open.er-api.com"))
            } else {
                conn.disconnect()
                Result.failure(Exception("HTTP error ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
