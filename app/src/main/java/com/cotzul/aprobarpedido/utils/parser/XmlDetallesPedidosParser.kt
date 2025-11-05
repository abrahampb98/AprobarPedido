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

class XmlParserDetallesPedidos {

    companion object {

        @Throws(XmlPullParserException::class, IOException::class)
        fun parseMultiTable(xmlData: String, database: SQLiteDatabase, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var currentTable = -1
            var currentTag: String? = null
            var currentValues = ContentValues()

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {

                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name

                            when (currentTag) {
                                // Identificar cada bloque por su nombre
                                "Table" -> {
                                    currentTable = 0  // Cabecera Pedido
                                    currentValues = ContentValues()
                                }
                                "Table1" -> {
                                    currentTable = 1  // CxC
                                    currentValues = ContentValues()
                                }
                                "Table2" -> {
                                    currentTable = 2  // Detalle Pedido
                                    currentValues = ContentValues()
                                }
                                "Table3" -> {
                                    currentTable = 3  // Auditoría Pedido
                                    currentValues = ContentValues()
                                }

                                // Si es otro tag dentro de una tabla, lo leemos
                                else -> {
                                    parser.next()
                                    val text = parser.text ?: ""
                                    when (currentTable) {
                                        0 -> currentValues.put(currentTag, text)
                                        1 -> currentValues.put(currentTag, text)
                                        2 -> currentValues.put(currentTag, text)
                                        3 -> currentValues.put(currentTag, text)
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "Table" -> {
                                    database.insert("fa_ws_CabPedido", null, currentValues)
                                    currentTable = -1
                                }
                                "Table1" -> {
                                    database.insert("cc_ws_cxc", null, currentValues)
                                    currentTable = -1
                                }
                                "Table2" -> {
                                    database.insert("fa_ws_DetallePedido", null, currentValues)
                                    currentTable = -1
                                }
                                "Table3" -> {
                                    database.insert("fa_ws_AuditoriaPedido", null, currentValues)
                                    currentTable = -1
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("XmlParserPedidosMulti", "Error parsing XML: ${e.message}")
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Error al procesar XML: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
                return "Error al parsear XML"
            }

            return "Parseo completado correctamente"
        }
    }
}
