package com.cotzul.aprobarpedido.utils.parser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader

class XmlParserPedidos {
    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parserPedidos(xmlData: String, database: SQLiteDatabase, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var pe_coddocumento: String? = null
            var cl_codigo: String? = null
            var cl_cliente: String? = null
            var vn_vendedor: String? = null
            var pe_estado: String? = null
            var pe_valorTotal: String? = null
            var pe_observacion: String? = null

            // Variable para almacenar la observación encontrada (si no contiene "Pedidos")
            var observacionRetorno: String? = null

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "Table" -> {
                                    // Reinicia variables por cada Table
                                    pe_coddocumento = null
                                    cl_codigo = null
                                    cl_cliente = null
                                    vn_vendedor = null
                                    pe_estado = null
                                    pe_valorTotal = null
                                    pe_observacion = null
                                }
                                "pe_coddocumento" -> { parser.next(); pe_coddocumento = parser.text }
                                "cl_codigo" -> { parser.next(); cl_codigo = parser.text }
                                "cl_cliente" -> { parser.next(); cl_cliente = parser.text }
                                "vn_vendedor" -> { parser.next(); vn_vendedor = parser.text }
                                "pe_estado" -> { parser.next(); pe_estado = parser.text }
                                "pe_valorTotal" -> { parser.next(); pe_valorTotal = parser.text }
                                "pe_observacion" -> { parser.next(); pe_observacion = parser.text }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                // Procesar cada registro
                                if (pe_observacion != null) {
                                    if (pe_observacion.contains("Pedidos")) {
                                        observacionRetorno = pe_observacion
                                    }
                                }else if (pe_coddocumento == "0" && cl_codigo == "0"){
                                    observacionRetorno = ""
                                }else {
                                    val values = ContentValues().apply {
                                        put("pe_coddocumento", pe_coddocumento)
                                        put("cl_codigo", cl_codigo)
                                        put("cl_cliente", cl_cliente)
                                        put("vn_vendedor", vn_vendedor)
                                        put("pe_estado", pe_estado)
                                        put("pe_valorTotal", pe_valorTotal)
                                    }
                                    database.insert("fa_ws_Pedido", null, values)
                                    observacionRetorno = "Parseo completado"
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ItemTempXMLParser", "Error parsing XML: ${e.message}")
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Se produjo un error al procesar el XML: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
            }

            // Devuelve la observación si hubo alguna, o mensaje por defecto
            return observacionRetorno ?: "Parseo completado"
        }
    }
}
