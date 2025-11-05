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

class XmlParserProductos {
    companion object {
        @Throws(XmlPullParserException::class, IOException::class)
        fun parserProdcutos(xmlData: String, database: SQLiteDatabase, context: Context): String {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlData))

            // Variables para almacenar los valores de las etiquetas
            var it_codigo: String? = null
            var it_referencia: String? = null
            var it_descripcion: String? = null
            var it_titulo: String? = null
            var it_familia: String? = null
            var it_marca: String? = null
            var it_almesa: String? = null
            var it_teler: String? = null
            var it_mmg: String? = null
            var it_mmq: String? = null
            var pv_precio5: String? = null
            var pv_precio6: String? = null
            var pv_precio7: String? = null
            var um_unidadCM: String? = null
            var um_unidadCE: String? = null
            var um_sku: String? = null
            var um_pesoCE: String? = null
            var pv_preciosubdistrib: String? = null
            var pv_desctosubdistrib: String? = null
            var pv_costoN: String? = null
            var it_exhVmr: String? = null
            var it_dcp: String? = null
            var it_exhTele: String? = null
            var it_costoprom: String? = null
            var it_activaex: String? = null

            try {
                // Recorre el XML hasta el final
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "Table" -> {
                                    // Reinicia las variables para cada nuevo Table
                                    it_codigo = null
                                    it_referencia = null
                                    it_descripcion = null
                                    it_titulo = null
                                    it_familia = null
                                    it_marca = null
                                    it_almesa = null
                                    it_teler = null
                                    it_mmg = null
                                    it_mmq = null
                                    pv_precio5 = null
                                    pv_precio6 = null
                                    pv_precio7 = null
                                    um_unidadCM = null
                                    um_unidadCE = null
                                    um_sku = null
                                    um_pesoCE = null
                                    pv_preciosubdistrib = null
                                    pv_desctosubdistrib = null
                                    pv_costoN = null
                                    it_exhVmr = null
                                    it_dcp = null
                                    it_exhTele = null
                                    it_costoprom = null
                                    it_activaex = null
                                }

                                "it_codigo" -> { parser.next(); it_codigo = parser.text }
                                "it_referencia" -> { parser.next(); it_referencia = parser.text }
                                "it_descripcion" -> { parser.next(); it_descripcion = parser.text }
                                "it_titulo" -> { parser.next(); it_titulo = parser.text }
                                "it_familia" -> { parser.next(); it_familia = parser.text }
                                "it_marca" -> { parser.next(); it_marca = parser.text }
                                "it_almesa" -> { parser.next(); it_almesa = parser.text }
                                "it_teler" -> { parser.next(); it_teler = parser.text }
                                "it_mmg" -> { parser.next(); it_mmg = parser.text }
                                "it_mmq" -> { parser.next(); it_mmq = parser.text }
                                "pv_precio5" -> { parser.next(); pv_precio5 = parser.text }
                                "pv_precio6" -> { parser.next(); pv_precio6 = parser.text }
                                "pv_precio7" -> { parser.next(); pv_precio7 = parser.text }
                                "um_unidadCM" -> { parser.next(); um_unidadCM = parser.text }
                                "um_unidadCE" -> { parser.next(); um_unidadCE = parser.text }
                                "um_sku" -> { parser.next(); um_sku = parser.text }
                                "um_pesoCE" -> { parser.next(); um_pesoCE = parser.text }
                                "pv_preciosubdistrib" -> { parser.next(); pv_preciosubdistrib = parser.text }
                                "pv_desctosubdistrib" -> { parser.next(); pv_desctosubdistrib = parser.text }
                                "pv_costoN" -> { parser.next(); pv_costoN = parser.text }
                                "it_exhVmr" -> { parser.next(); it_exhVmr = parser.text }
                                "it_dcp" -> { parser.next(); it_dcp = parser.text }
                                "it_exhTele" -> { parser.next(); it_exhTele = parser.text }
                                "it_costoprom" -> { parser.next(); it_costoprom = parser.text }
                                "it_activaex" -> { parser.next(); it_activaex = parser.text }
                            }
                        }

                        // Verifica si encontramos el final de un "Table"
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "Table") {
                                // Insertar los datos en la base de datos por cada Table encontrado
                                if (it_codigo != null) {
                                    val values = ContentValues().apply {
                                        put("it_codigo", it_codigo)
                                        put("it_referencia", it_referencia)
                                        put("it_descripcion", it_descripcion)
                                        put("it_titulo", it_titulo)
                                        put("it_familia", it_familia)
                                        put("it_marca", it_marca)
                                        put("it_almesa", it_almesa)
                                        put("it_teler", it_teler)
                                        put("it_mmg", it_mmg)
                                        put("it_mmq", it_mmq)
                                        put("pv_precio5", pv_precio5)
                                        put("pv_precio6", pv_precio6)
                                        put("pv_precio7", pv_precio7)
                                        put("um_unidadCM", um_unidadCM)
                                        put("um_unidadCE", um_unidadCE)
                                        put("um_sku", um_sku)
                                        put("um_pesoCE", um_pesoCE)
                                        put("pv_preciosubdistrib", pv_preciosubdistrib)
                                        put("pv_desctosubdistrib", pv_desctosubdistrib)
                                        put("pv_costoN", pv_costoN)
                                        put("it_exhVmr", it_exhVmr)
                                        put("it_dcp", it_dcp)
                                        put("it_exhTele", it_exhTele)
                                        put("it_costoprom", it_costoprom)
                                        put("it_activaex", it_activaex)
                                    }
                                    val newRowId = database.insert("iv_ws_item", null, values)
                                    if (newRowId == -1L) {
                                        Log.e("ItemTempParser", "Error inserting into database")
                                    } else {
                                        //Log.d("ItemTempParser", "Inserted successfully with row id: $newRowId")
                                    }
                                } else {
                                    Log.e("ItemTempParser", "Missing required fields in the XML data")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ItemTempXMLParser", "Error parsing XML: ${e.message}")
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Error")
                builder.setMessage("Se produjo un error al procesar el XML: ${e.message}")
                builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.setCancelable(false)
                builder.show()
            }

            return "Parseo completado"
        }
    }
}
