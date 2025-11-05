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

class XmlParserRentabilidades {

    companion object {

        @Throws(XmlPullParserException::class, IOException::class)
        fun parserRentabilidad(xmlData: String, database: SQLiteDatabase, context: Context, opcion: Int): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            var currentTag: String? = null
            var values = ContentValues()

            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {

                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name

                            when (currentTag) {
                                "Table" -> {
                                    // inicio de un nuevo registro
                                    values = ContentValues()
                                }
                                // cualquier otro tag dentro de <Table>
                                else -> {
                                    parser.next() // mover al texto
                                    val text = parser.text ?: ""
                                    values.put(currentTag, text)
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                // fin de un registro → insertar
                                when (opcion) {
                                    24 -> {
                                        database.insert("fa_ws_rentabilidadActual", null, values)
                                    }

                                    26 -> {
                                        // Aquí agregas lo que debe hacer cuando opcion = 26
                                        database.insert("fa_ws_rentabilidadCliente", null, values)
                                    }

                                    // Puedes seguir agregando más casos
                                    28 -> {
                                        database.insert("fa_ws_rentabilidadVendedor", null, values)
                                    }

                                    else -> {

                                    }
                                }


                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("XmlParserCabCobro", "Error parsing XML: ${e.message}", e)
                AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage("Error al procesar XML: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
                return "Error al parsear XML"
            }

            return "Parseo de cc_ws_cabcobro completado correctamente"
        }
    }
}
