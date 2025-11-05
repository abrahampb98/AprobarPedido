package com.cotzul.aprobarpedido.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import android.content.Context
import com.cotzul.aprobarpedido.R
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class SolicitudSoap(private val context: Context) {
    lateinit var strIpPublica: String
    lateinit var str_id: String
    lateinit var str_cadena: String
    lateinit var str_ipLocal: String
    lateinit var strIp: String

    private fun fnobtenerIpActiva(): String {
        return if (fnConexion(str_ipLocal)) {
            str_ipLocal   // Está en la red local
        } else {
            strIpPublica  // Está fuera, usa IP pública
        }
    }

    private fun fnConexion(ip: String): Boolean {
        return try {
            val address = InetAddress.getByName(ip)
            address.isReachable(1000) // tiempo máximo 1 segundo
        } catch (e: Exception) {
            false
        }
    }


    fun initializeVariables(id: Int, Cadena: String) {
        // Accedemos a los recursos a través del contexto
        str_id = id.toString()
        str_cadena = Cadena
        str_ipLocal = context.getString(R.string.str_ipLocal)
        strIpPublica = context.getString(R.string.str_ipPublica)
        strIp= fnobtenerIpActiva()
    }

    fun realizarSolicitudSoap(): InputStream? {
        return try {
            val soapRequest = """<?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                       xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <wmObtenerDatos xmlns="http://$str_ipLocal/wsPortrans/Service.asmx">
              <id>$str_id</id>
              <cadena><![CDATA[$str_cadena]]></cadena>
            </wmObtenerDatos>
          </soap:Body>
        </soap:Envelope>"""

            val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
            val requestBody = soapRequest.toRequestBody(mediaType)

            // 🔹 Configuración de timeout en OkHttpClient
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Tiempo máximo para conectar al servidor
                .readTimeout(30, TimeUnit.SECONDS)     // Tiempo máximo para leer datos
                .writeTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para enviar datos
                .build()

            val request = Request.Builder()
                .url("http://$strIp/wsPortrans/service.asmx?op=wmObtenerDatos")
                .post(requestBody)
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ByteArrayInputStream(response.body!!.bytes())
                } else {
                    throw IOException("Error en la solicitud: ${response.code} - ${response.message}")
                }
            }
        } catch (e: IOException) {
            throw IOException("Excepción en la solicitud SOAP: ${e.message}")
        }
    }

}
