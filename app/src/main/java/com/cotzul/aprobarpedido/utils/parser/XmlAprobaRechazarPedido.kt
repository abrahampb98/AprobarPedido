package com.cotzul.aprobarpedido.utils.parser


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper

class XmlAprobarPedido(private val context: Context) {

    fun fnObtenerPedidosXML(vlpcodUsuario: Int, vlpUsuario: String): String {
        var xml: String = ""
        val db = abrirBaseDatos()

        try {
            // Consulta para obtener los datos del pedido
            xml = "'<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
            xml += "<c "
            val cursorC = db.rawQuery("SELECT 2 as em_codigo, 1 as bo_codigo, c.pe_coddocumento, p.pe_estado, a.pe_usuario, a.pe_observacion\n" +
                    "FROM fa_ws_CabPedido c\n" +
                    "INNER JOIN fa_ws_Pedido p on c.pe_coddocumento=p.pe_coddocumento\n" +
                    "INNER JOIN fa_ws_AuditoriaPedido a on c.pe_coddocumento=a.pe_coddocumento", null)
            while (cursorC.moveToNext()) {
                xml += "c0=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("em_codigo"))}\" "
                xml += "c1=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("bo_codigo"))}\" "
                xml += "c2=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_coddocumento"))}\" "
                xml += "c3=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_estado"))}\" "
                xml += "c4=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_usuario"))}\" "
                xml += "c5=\"$vlpcodUsuario\" "
                xml += "c6=\"${cursorC.getString(cursorC.getColumnIndexOrThrow("pe_observacion"))}\" "

                xml = xml + ">"

                // Consulta para obtener los detalles del pedido
                val cursorD = db.rawQuery("SELECT it_codigo, dp_facturar, dp_preciounitario FROM fa_ws_DetallePedido ", null)
                while (cursorD.moveToNext()) {
                    xml += "<detalle "
                    xml += "d0=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("it_codigo"))}\" "
                    xml += "d1=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_facturar"))}\" "
                    xml += "d2=\"${cursorD.getString(cursorD.getColumnIndexOrThrow("dp_preciounitario"))}\" "
                    xml = xml + "></detalle>"
                }
                cursorD.close()
            }
            cursorC.close()

            xml += "</c>',1,'$vlpUsuario'"
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
