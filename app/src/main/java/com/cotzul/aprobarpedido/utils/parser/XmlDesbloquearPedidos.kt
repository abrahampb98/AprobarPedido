package com.cotzul.aprobarpedido.utils.parser


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper

class XmlDesbloquearPedido(private val context: Context) {

    fun fnObtenerPedidosXML(vlpUsuario: String): String {
        var xml: String = ""
        val db = abrirBaseDatos()

        try {
            // Consulta para obtener los datos del pedido
            xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
            xml += "<c "
                // Asume que conoces los nombres de las columnas y su correspondencia en el XML
            xml += "c0=\"2\" "
            xml += "c1=\"1\" "
                // Continúa para todas las columnas necesarias
            xml = xml + ">"

                // Consulta para obtener los detalles del pedido
                val cursorD = db.rawQuery("select pe_coddocumento from fa_ws_Pedido where pe_coddocumento <> 0", null)
                while (cursorD.moveToNext()) {
                    xml += "<detalle "
                    xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("pe_coddocumento"))}\" " // Ajusta el nombre de columna
                    xml = xml + "></detalle>"
                }
                cursorD.close()

                xml += "</c>',2,'$vlpUsuario'"
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return xml
    }

    private fun abrirBaseDatos(): SQLiteDatabase {
        val dbHelper = SqLiteOpenHelper(context) // Asegúrate de que esta clase esté bien implementada para manejar la base de datos
        return dbHelper.readableDatabase
    }

}
