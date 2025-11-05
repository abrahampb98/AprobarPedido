package com.cotzul.aprobarpedido.utils.cls

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.database.getStringOrNull
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.ui.adapters.Pedidos
import com.cotzul.aprobarpedido.ui.adapters.Precios
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClsLLenarControles (private val context: Context){

    fun fnInsertUsuario(epCodigo: Int, login: String, nombre: String, password: String, fechaIng: String): Long {
        val db = DatabaseManager.openDatabase(context)
        var resultado: Long = -1

        try {
            // Verificar si el usuario ya existe (por ep_codigo o login)
            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM se_ws_usuario WHERE ep_codigo = ? OR us_login = ?",
                arrayOf(epCodigo.toString(), login)
            )

            var existe = false
            if (cursor.moveToFirst()) {
                existe = cursor.getInt(0) > 0
            }
            cursor.close()

            if (!existe) {
                val values = ContentValues().apply {
                    put("ep_codigo", epCodigo)
                    put("us_login", login)
                    put("us_nombre", nombre)
                    put("us_password", password)
                    put("us_fechaing", fechaIng)
                }

                // Insertar solo si no existe
                resultado = db.insert("se_ws_usuario", null, values)
            } else {
                //Log.d("fnInsertUsuario", "El usuario ya existe, no se insertó.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return resultado
    }


    fun fnValidarLogin(login: String, password: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var existe = false

        val query = """
        SELECT us_login 
        FROM se_ws_usuario 
        WHERE us_login = ? AND us_password = ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(login, password)).use { cursor ->
            if (cursor.moveToFirst()) {
                existe = true
            }
        }

        DatabaseManager.closeDatabase()
        return existe
    }

    fun fnObtenerCodigoUsuario(us_login: String): Int{
        val db = DatabaseManager.openDatabase(context)
        val query = "SELECT ep_codigo FROM se_ws_usuario WHERE us_login = ?"
        return db.rawQuery(query, arrayOf(us_login)).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow("ep_codigo"))
            } else {
                -1 // Retorna -1 o cualquier valor por defecto que elijas si no se encuentra la cuenta
            }
        }.also {
            DatabaseManager.closeDatabase()
        }
    }



    fun fnInsertarTiposPedido(): Long {
        val db = DatabaseManager.openDatabase(context)
        var resultado: Long = -1

        try {
            // Verificar si existen registros
            val cursor = db.rawQuery("SELECT COUNT(*) FROM fa_ws_estadoPedido", null)
            val existe = cursor.use { it.moveToFirst() && it.getInt(0) > 0 }

            if (!existe) {
                db.beginTransaction()
                try {
                    val tipos = listOf(
                        Triple(-1, "", "SELECCIONAR"),
                        Triple(0, "", "TODOS"),
                        Triple(1, "A", "NUEVO"),
                        Triple(2, "B", "BACKORDER"),
                        Triple(4, "D", "NO APROBADO"),
                        Triple(3, "R", "REACTIVADO")
                    )

                    tipos.forEach { (codigo, estado, descripcion) ->
                        val values = ContentValues().apply {
                            put("ep_codigo", codigo)
                            put("ep_estadoPedido", estado)
                            put("ep_descripcion", descripcion)
                        }
                        db.insert("fa_ws_estadoPedido", null, values)
                    }

                    db.setTransactionSuccessful()
                    resultado = 1L
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    db.endTransaction()
                }
            } else {
                // Ya existen registros, no se insertan datos
                resultado = 0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return resultado
    }



    fun fnObtenerNombreUsuario(ep_codigo: Int): String {
        val db = DatabaseManager.openDatabase(context)
        var nombreUsuario = ""

        try {
            val query = "SELECT us_nombre FROM se_ws_usuario WHERE ep_codigo = ?"
            db.rawQuery(query, arrayOf(ep_codigo.toString())).use { cursor ->
                if (cursor.moveToFirst()) {
                    nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("us_nombre"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return nombreUsuario
    }


    fun fnLLenarSpinnerTipoPedido( spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)

        val query = """ 
            SELECT ep_codigo, ep_estadoPedido, ep_descripcion FROM fa_ws_estadoPedido
    """

        db.rawQuery(query, null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("ep_codigo"))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow("ep_estadoPedido")) ?: ""
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("ep_descripcion")) ?: ""
                items.add(SpinnerItem(codigo, estado, descripcion))
            }

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        DatabaseManager.closeDatabase()
    }

    fun fnCargarPedidos(): List<Pedidos> {
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<Pedidos>()

        val query = """
        SELECT
            pe_coddocumento, 
            cl_codigo, 
            cl_cliente, 
            vn_vendedor, 
            e.ep_descripcion, 
            pe_valorTotal 
        FROM fa_ws_Pedido p
        INNER JOIN fa_ws_estadoPedido e on p.pe_estado = e.ep_estadoPedido
        WHERE pe_coddocumento <> 0
    """

        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                val pedido = Pedidos(
                    Documento = cursor.getInt(cursor.getColumnIndexOrThrow("pe_coddocumento")),
                    Codigo = cursor.getInt(cursor.getColumnIndexOrThrow("cl_codigo")),
                    Cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_cliente")),
                    Vendedor = cursor.getString(cursor.getColumnIndexOrThrow("vn_vendedor")),
                    Estado = cursor.getString(cursor.getColumnIndexOrThrow("ep_descripcion")),
                    Valor = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valorTotal")),
                    Ubicacion = "",
                    Cupo = 0.0,
                    CupoD = 0.0
                )
                pedidos.add(pedido)
            }
        }

        DatabaseManager.closeDatabase()
        return pedidos
    }


    fun fnCargarEdicionPedido(codigo: Int): List<Pedidos> {
        val db = DatabaseManager.openDatabase(context)
        val pedidos = mutableListOf<Pedidos>()

        val query = """
          SELECT DISTINCT
                pe_coddocumento, 
                p.cl_codigo, 
                cl_cliente, 
                vn_vendedor, 
                e.ep_descripcion, 
                pe_valorTotal,
                Provincia || ' - ' || Ciudad AS ubicacion,
                CupoTotal, 
                CupoDisponible
            FROM fa_ws_Pedido p
            INNER JOIN fa_ws_estadoPedido e ON p.pe_estado = e.ep_estadoPedido
            INNER JOIN cc_ws_cabcobro c ON p.cl_codigo=c.cl_codigo AND c.Empresa='Cotzul'
            WHERE pe_coddocumento = ?
    """

        try {
            db.rawQuery(query, arrayOf(codigo.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    val pedido = Pedidos(
                        Documento = cursor.getInt(cursor.getColumnIndexOrThrow("pe_coddocumento")),
                        Codigo = cursor.getInt(cursor.getColumnIndexOrThrow("cl_codigo")),
                        Cliente = cursor.getString(cursor.getColumnIndexOrThrow("cl_cliente")),
                        Vendedor = cursor.getString(cursor.getColumnIndexOrThrow("vn_vendedor")),
                        Estado = cursor.getString(cursor.getColumnIndexOrThrow("ep_descripcion")),
                        Valor = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valorTotal")),
                        Ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("ubicacion")),
                        Cupo = cursor.getDouble(cursor.getColumnIndexOrThrow("CupoTotal")),
                        CupoD =    cursor.getDouble(cursor.getColumnIndexOrThrow("CupoDisponible"))
                    )
                    pedidos.add(pedido)
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarEdicionPedido", "Error al cargar pedido: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return pedidos
    }


    fun fnCargarFacturas(): List<Factura> {
        val db = DatabaseManager.openDatabase(context)
        val facturas = mutableListOf<Factura>()

        val query = """
        SELECT DISTINCT
            a.Sri, 
            a.FechaDoc, 
            a.Vendedor, 
            a.EtiquetaDoc, 
            a.Total, 
            a.Saldo, 
            a.NumCheque, 
            a.FactEmitida,
			case when b.EtiquetaDoc is null then False else True end as boton
        FROM cc_ws_cabcobro a
		LEFT join cc_ws_cabcobro b on a.Sri=b.Sri and b.EtiquetaDoc <> 'Fact Crédito'
        WHERE a.EtiquetaDoc = 'Fact Crédito'
        ORDER BY 4 DESC
    """

        try {
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {

                    val sri = cursor.getString(cursor.getColumnIndexOrThrow("Sri"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("FechaDoc"))
                    val vendedor = cursor.getString(cursor.getColumnIndexOrThrow("Vendedor"))
                    val tipoDoc = cursor.getString(cursor.getColumnIndexOrThrow("EtiquetaDoc"))
                    val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))
                    val saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("Saldo"))
                    val detCheque = cursor.getString(cursor.getColumnIndexOrThrow("NumCheque")) ?: "---"
                    val observacion = cursor.getString(cursor.getColumnIndexOrThrow("FactEmitida")) ?: ""
                    val boton = cursor.getInt(cursor.getColumnIndexOrThrow("boton")) == 1


                    val factura = Factura(
                        sri = sri,
                        fecha = fecha,
                        vendedor = vendedor,
                        tipoDoc = tipoDoc,
                        valor = valor,
                        saldo = saldo,
                        detCheque = detCheque,
                        dias = observacion,
                        botton = boton
                    )

                    facturas.add(factura)
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarFacturas", "Error al cargar facturas: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return facturas
    }

    fun fnCargarSoporteFacturas(sri: String): List<Factura> {
        val db = DatabaseManager.openDatabase(context)
        val facturas = mutableListOf<Factura>()

        val query = """
        SELECT 
            Sri, 
            FechaDoc, 
            Vendedor, 
            EtiquetaDoc, 
            ValorCuota, 
            Saldo, 
            NumCheque, 
            FactEmitida,
            0 AS boton
        FROM cc_ws_cabcobro 
        WHERE Sri = ? 
          AND EtiquetaDoc <> 'Fact Crédito'
    """

        try {
            db.rawQuery(query, arrayOf(sri)).use { cursor ->
                while (cursor.moveToNext()) {
                    val sriValue = cursor.getString(cursor.getColumnIndexOrThrow("Sri"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("FechaDoc"))
                    val vendedor = cursor.getString(cursor.getColumnIndexOrThrow("Vendedor"))
                    val tipoDoc = cursor.getString(cursor.getColumnIndexOrThrow("EtiquetaDoc"))
                    val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("ValorCuota"))
                    val saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("Saldo"))
                    val detCheque = cursor.getString(cursor.getColumnIndexOrThrow("NumCheque")) ?: "---"
                    val observacion = cursor.getString(cursor.getColumnIndexOrThrow("FactEmitida")) ?: ""
                    val boton = cursor.getInt(cursor.getColumnIndexOrThrow("boton")) == 1 // ✅ alias agregado

                    val factura = Factura(
                        sri = sriValue,
                        fecha = fecha,
                        vendedor = vendedor,
                        tipoDoc = tipoDoc,
                        valor = valor,
                        saldo = saldo,
                        detCheque = detCheque,
                        dias = observacion,
                        botton = boton
                    )

                    facturas.add(factura)
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarSoporteFacturas", "Error al cargar facturas: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return facturas
    }

    fun fnCupoCliente(documento: Int): List<Cupo> {
        val db = DatabaseManager.openDatabase(context)
        val cupos = mutableListOf<Cupo>()

        val query = """
        SELECT 
            cl_observacion,
            cc_cupoasignado,
            cc_cupoutilizado,
            cc_cupodisponible,
            cl_valorpatrimonio,
            pp_descripcion,
            pe_valorbruto,
            pe_valordescuento,
            pe_seguro,
            pe_valoriva,
            pe_flete,
            pe_valorTotal
        FROM fa_ws_CabPedido
        WHERE pe_coddocumento = ?
    """

        try {
            db.rawQuery(query, arrayOf(documento.toString())).use { cursor ->
                while (cursor.moveToNext()) {

                    val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("pp_descripcion")) ?: ""
                    val totalCupo = cursor.getDouble(cursor.getColumnIndexOrThrow("cc_cupoasignado"))
                    val cupoUtilizado = cursor.getDouble(cursor.getColumnIndexOrThrow("cc_cupoutilizado"))
                    val cupoDisponible = cursor.getDouble(cursor.getColumnIndexOrThrow("cc_cupodisponible"))
                    val patrimonio = cursor.getDouble(cursor.getColumnIndexOrThrow("cl_valorpatrimonio"))
                    val descripcionProducto = cursor.getString(cursor.getColumnIndexOrThrow("cl_observacion")) ?: ""
                    val valorBruto = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valorbruto"))
                    val valorDescuento = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valordescuento"))
                    val valorSeguro = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_seguro"))
                    val valorIva = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valoriva"))
                    val valorFlete = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_flete"))
                    val valorTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("pe_valorTotal"))

                    val cupo = Cupo(
                        total = totalCupo,
                        utilizado = cupoUtilizado,
                        disponible = cupoDisponible,
                        patrimonio = patrimonio,
                        descripcion = descripcion,
                        valorBase = valorBruto,
                        valorDescuento = valorDescuento,
                        valorSeguro = valorSeguro,
                        valorIva = valorIva,
                        valorFlete = valorFlete,
                        valorTotal = valorTotal,
                        observacion = descripcionProducto
                    )

                    cupos.add(cupo)
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarCupo", "Error al cargar cupo: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return cupos
    }


    fun fnCargarObservaciones(): List<Observacion> {
        val db = DatabaseManager.openDatabase(context)
        val observaciones = mutableListOf<Observacion>()

        val query = """
        SELECT 
            pe_usuario, 
            pe_observacion, 
            pe_fecha 
        FROM fa_ws_AuditoriaPedido
        ORDER BY pe_fecha DESC
    """

        try {
            db.rawQuery(query, null).use { cursor ->
                var contador = 1 // 🔢 Contador manual para la secuencia
                while (cursor.moveToNext()) {
                    val observacion = cursor.getString(cursor.getColumnIndexOrThrow("pe_observacion")) ?: ""
                    val usuario = cursor.getString(cursor.getColumnIndexOrThrow("pe_usuario")) ?: ""
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("pe_fecha")) ?: ""

                    val obs = Observacion(
                        secuencia = contador,
                        observacion = observacion,
                        usuario = usuario,
                        fecha = fecha
                    )

                    observaciones.add(obs)
                    contador++ // Incrementar la secuencia
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarObservaciones", "Error al cargar observaciones: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return observaciones
    }


    fun fnCargarDetallesPedido(documento: Int): List<DetallePedido> {
        val db = DatabaseManager.openDatabase(context)
        val detalles = mutableListOf<DetallePedido>()

        val query = """
        SELECT 
            it_codigo, 
            dp_descripcion,  
            it_titulo, 
            ma_descripcion, 
            it_subfamilia, 
            dp_facturar, 
            dp_preciopedido, 
            dp_preciounitario, 
            dp_costopromedio, 
            dp_subtotalcostopromedio, 
            dp_total
        FROM fa_ws_DetallePedido
        WHERE pe_coddocumento = ?
    """

        try {
            db.rawQuery(query, arrayOf(documento.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    val codigo = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")) ?: ""
                    val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("dp_descripcion")) ?: ""
                    val titulo = cursor.getString(cursor.getColumnIndexOrThrow("it_titulo")) ?: ""
                    val marca = cursor.getString(cursor.getColumnIndexOrThrow("ma_descripcion")) ?: ""
                    val subFamilia = cursor.getString(cursor.getColumnIndexOrThrow("it_subfamilia")) ?: ""
                    val cantidad = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_facturar"))
                    val precioPedido = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_preciopedido"))
                    val precioUnitario = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_preciounitario"))
                    val costoNacional = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_costopromedio"))
                    val subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_subtotalcostopromedio"))
                    val total = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_total"))

                    val detalle = DetallePedido(
                        codigo = codigo,
                        descripcion = descripcion,
                        titulo = titulo,
                        marca = marca,
                        subFamilia = subFamilia,
                        cantidad = cantidad,
                        precioPedido = precioPedido,
                        precioUnitario = precioUnitario,
                        costoProm = costoNacional,
                        subtotal = subtotal,
                        total = total,
                        aceptarPrecio = false // inicial por defecto
                    )

                    detalles.add(detalle)
                }
            }
        } catch (e: Exception) {
            Log.e("fnCargarDetallesPedido", "Error al cargar detalles: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return detalles
    }


    fun fnActualizarPrecioDetalle(codigo: String, nuevoPrecio: Double): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var exito = false

        try {
            val valores = ContentValues().apply {
                put("dp_preciounitario", nuevoPrecio)
            }

            // Ejecutar la actualización
            val filasAfectadas = db.update(
                "fa_ws_DetallePedido",
                valores,
                "it_codigo = ?",
                arrayOf(codigo)
            )

            exito = filasAfectadas > 0
            //Log.i("fnActualizarPrecioDetalle", "Precio actualizado correctamente para código $codigo -> $nuevoPrecio")

        } catch (e: Exception) {
            Log.e("fnActualizarPrecioDetalle", "Error al actualizar precio: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return exito
    }


    fun fnAuditoriaPedido(documento: Int, usuario: String, observacion: String): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var exito = false

        try {
            // 📅 Obtener la fecha actual en formato yyyy-MM-dd HH:mm:ss
            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )

            val valores = ContentValues().apply {
                put("pe_coddocumento", documento)
                put("pe_detdocumento", 1)
                put("pe_usuario", usuario)
                put("pe_observacion", observacion)
                put("pe_fecha", fechaActual)
            }

            // 🟢 Ejecutar el INSERT
            val resultado = db.insert("fa_ws_AuditoriaPedido", null, valores)

            exito = resultado != -1L
            if (exito) {
                Log.i("fnAuditoriaPedido", "Auditoría registrada para doc=$documento, usuario=$usuario")
            }

        } catch (e: Exception) {
            Log.e("fnAuditoriaPedido", "Error al registrar auditoría: ${e.message}")
        } finally {
            DatabaseManager.closeDatabase()
        }

        return exito
    }


    fun fnActualizarEstadoPedido(estado: String, pedido: Int): Boolean {
        val db = DatabaseManager.openDatabase(context)
        var exito = false

        try {
            val valores = ContentValues().apply {
                put("pe_estado", estado)
            }

            // Ejecutar la actualización correctamente
            val filasAfectadas = db.update(
                "fa_ws_Pedido",
                valores,
                "pe_coddocumento = ?",
                arrayOf(pedido.toString())
            )

            exito = filasAfectadas > 0

            Log.i("fnActualizarEstadoPedido", "Estado actualizado correctamente para pedido $pedido -> $estado")

        } catch (e: Exception) {
            Log.e("fnActualizarEstadoPedido", "Error al actualizar estado: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return exito
    }

    fun fnObtenerSaldos(): List<CxcItem> {
        val lista = mutableListOf<CxcItem>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val cursor = db.rawQuery(
                "SELECT cc_sec, cc_descripcion, cl_saldo FROM cc_ws_cxc",
                null
            )

            if (cursor.moveToFirst()) {
                do {
                    val item = CxcItem(
                        cc_sec = cursor.getInt(cursor.getColumnIndexOrThrow("cc_sec")),
                        cc_descripcion = cursor.getString(cursor.getColumnIndexOrThrow("cc_descripcion")),
                        cl_saldo = cursor.getDouble(cursor.getColumnIndexOrThrow("cl_saldo"))
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("obtenerListaCxc", "Error al obtener datos: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }


    fun fnObtenerRentabilidades(documento: Int): List<Rentabilidades> {
        val lista = mutableListOf<Rentabilidades>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val cursor = db.rawQuery(
                """
            SELECT 
                SUM(dp_subtotalcn) AS dp_subtotalcn,
                SUM(dp_subtotalcostopromedio) AS dp_subtotalcp,
                SUM(dp_facturar * dp_preciounitario) AS dp_subtotalReal,
                (pe_valordescuento * 100) / pe_valorbruto AS descuentoReal
            FROM fa_ws_DetallePedido d
            INNER JOIN fa_ws_CabPedido c 
                ON d.pe_coddocumento = c.pe_coddocumento
            WHERE d.pe_coddocumento = ?
            """.trimIndent(),
                arrayOf(documento.toString())
            )

            if (cursor.moveToFirst()) {
                do {
                    val item = Rentabilidades(
                        dp_subtotalcn = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_subtotalcn")),
                        dp_subtotalcp = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_subtotalcp")),
                        dp_subtotalReal = cursor.getDouble(cursor.getColumnIndexOrThrow("dp_subtotalReal")),
                        descuentoReal = cursor.getDouble(cursor.getColumnIndexOrThrow("descuentoReal"))
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("fnObtenerRentabilidades", "Error al obtener datos: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }

    fun fnInsertarTiposRentabilidad(): Long {
        val db = DatabaseManager.openDatabase(context)
        var resultado: Long = -1

        try {
            // Verificar si existen registros en la tabla
            val cursor = db.rawQuery("SELECT COUNT(*) FROM fa_ws_rentabilidad", null)
            val existe = cursor.use { it.moveToFirst() && it.getInt(0) > 0 }

            if (!existe) {
                db.beginTransaction()
                try {
                    // Lista de tipos de rentabilidad a insertar
                    val tipos = listOf(
                        Pair(0, "SELECCIONA TIPO RENTABILIDAD"),
                        Pair(24, "RENT. ACTUAL"),
                        Pair(28, "RENT. VENDEDOR"),
                        Pair(26, "RENT. X CLIENTE")
                    )

                    // Insertar cada registro en la tabla
                    tipos.forEach { (codigo, descripcion) ->
                        val values = ContentValues().apply {
                            put("re_codigo", codigo)
                            put("re_descripcion", descripcion)
                        }
                        db.insert("fa_ws_rentabilidad", null, values)
                    }

                    db.setTransactionSuccessful()
                    resultado = 1L
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    db.endTransaction()
                }
            } else {
                // Ya existen registros, no se insertan datos
                resultado = 0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DatabaseManager.closeDatabase()
        }

        return resultado
    }


    fun fnLLenarSpinnerTipoRentabilidad( spinner: Spinner) {
        val db = DatabaseManager.openDatabase(context)

        val query = """ 
            SELECT re_codigo, re_descripcion FROM fa_ws_rentabilidad
    """

        db.rawQuery(query, null).use { cursor ->
            val items = mutableListOf<SpinnerItem>()
            while (cursor.moveToNext()) {
                val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("re_codigo"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("re_descripcion")) ?: ""

                items.add(SpinnerItem(codigo, "", descripcion))
            }

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        DatabaseManager.closeDatabase()
    }


    fun fnObtenerRentabilidadVendedor(): List<RentabilidadVendedor> {
        val lista = mutableListOf<RentabilidadVendedor>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val cursor = db.rawQuery(
                """
            SELECT 
                tipo ,
                nombrevendedor,
                ROUND(subtotal,2) AS A,
                ROUND(costoNacionalizadoKardex,2) AS B,
                ROUND(utilidadKardex,2) AS C,
                ROUND(subtotal / costoNacionalizadoKardex,2) AS 'A/B'
            FROM fa_ws_rentabilidadVendedor
            ORDER BY orden 
            """.trimIndent(), null
            )

            if (cursor.moveToFirst()) {
                do {
                    val item = RentabilidadVendedor(
                        tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                        vendedor = cursor.getString(cursor.getColumnIndexOrThrow("nombrevendedor")),
                        totalMenosDescuento = cursor.getDouble(cursor.getColumnIndexOrThrow("A")),
                        costoNacionalizadoKardex = cursor.getDouble(cursor.getColumnIndexOrThrow("B")),
                        utilidadNacionalizado = cursor.getDouble(cursor.getColumnIndexOrThrow("C")),
                        porcentajeAB = cursor.getDouble(cursor.getColumnIndexOrThrow("A/B"))
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("fnObtenerRentVendedor", "Error al obtener datos: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }


    fun fnObtenerRentabilidadActual(): List<RentabilidadActual> {
        val lista = mutableListOf<RentabilidadActual>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val cursor = db.rawQuery(
                """
            SELECT 
                bodega,
                 CASE mes
        WHEN 1 THEN 'Enero'
        WHEN 2 THEN 'Febrero'
        WHEN 3 THEN 'Marzo'
        WHEN 4 THEN 'Abril'
        WHEN 5 THEN 'Mayo'
        WHEN 6 THEN 'Junio'
        WHEN 7 THEN 'Julio'
        WHEN 8 THEN 'Agosto'
        WHEN 9 THEN 'Septiembre'
        WHEN 10 THEN 'Octubre'
        WHEN 11 THEN 'Noviembre'
        WHEN 12 THEN 'Diciembre'
        ELSE 'Desconocido'
    END AS mes,
                ROUND(subtotal,2) AS B,
                ROUND(costoNacionalizadoKardex, 2) AS C,
                ROUND(utilidadKardex, 2) AS D,
                ROUND(subtotal / costoNacionalizadoKardex, 2) AS 'B/C'
            FROM fa_ws_rentabilidadActual
            ORDER BY bo_orden
            """.trimIndent(),
                null
            )

            if (cursor.moveToFirst()) {
                do {
                    val item = RentabilidadActual(
                        bodega = cursor.getString(cursor.getColumnIndexOrThrow("bodega")),
                        mes = cursor.getString(cursor.getColumnIndexOrThrow("mes")),
                        B = cursor.getDouble(cursor.getColumnIndexOrThrow("B")),
                        C = cursor.getDouble(cursor.getColumnIndexOrThrow("C")),
                        D = cursor.getDouble(cursor.getColumnIndexOrThrow("D")),
                        BC = cursor.getDouble(cursor.getColumnIndexOrThrow("B/C"))
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("fnObtenerRentActual", "Error al obtener datos: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }


    fun fnObtenerRentabilidadCliente(): List<RentabilidadCliente> {
        val lista = mutableListOf<RentabilidadCliente>()
        val db = DatabaseManager.openDatabase(context)

        try {
            val cursor = db.rawQuery(
                """
            SELECT 
                cliente,
                nombrecliente,
                ROUND(subtotal, 2) AS A,
                ROUND(totalMenosDescuentoSeguro, 2) AS B,
                ROUND(costoNacionalizadoKardex, 2) AS C,
                ROUND(utilidadKardex, 2) AS D,
                ROUND(subtotal / (SELECT SUM(subtotal) FROM fa_ws_rentabilidadCliente) * 100, 2) AS ATA,
                ROUND(utilidadKardex / (SELECT SUM(utilidadKardex) FROM fa_ws_rentabilidadCliente) * 100, 2) AS DTD,
                ROUND(subtotal / costoNacionalizadoKardex * 100, 2) AS AC,
                ROUND(totalMenosDescuentoSeguro / costoNacionalizadoKardex * 100, 2) AS BC
            FROM fa_ws_rentabilidadCliente;
            """.trimIndent(),
                null
            )

            if (cursor.moveToFirst()) {
                do {
                    val item = RentabilidadCliente(
                        cliente = cursor.getString(cursor.getColumnIndexOrThrow("cliente")),
                        nombreCliente = cursor.getString(cursor.getColumnIndexOrThrow("nombrecliente")),
                        A = cursor.getDouble(cursor.getColumnIndexOrThrow("A")),
                        B = cursor.getDouble(cursor.getColumnIndexOrThrow("B")),
                        C = cursor.getDouble(cursor.getColumnIndexOrThrow("C")),
                        D = cursor.getDouble(cursor.getColumnIndexOrThrow("D")),
                        ATA = cursor.getDouble(cursor.getColumnIndexOrThrow("ATA")),
                        DTD = cursor.getDouble(cursor.getColumnIndexOrThrow("DTD")),
                        AC = cursor.getDouble(cursor.getColumnIndexOrThrow("AC")),
                        BC = cursor.getDouble(cursor.getColumnIndexOrThrow("BC"))
                    )
                    lista.add(item)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("fnObtenerRentCliente", "Error al obtener datos: ${e.message}", e)
        } finally {
            DatabaseManager.closeDatabase()
        }

        return lista
    }


    fun fnBuscarReferencia(referencia: String): List<Precios> {
        val referencias = mutableListOf<Precios>()
        val db = DatabaseManager.openDatabase(context)

        // Query con CASE según bodega y con parámetros seguros
        val sql = """
    SELECT 
        it_referencia, 
        it_codigo, 
        ROUND(it_teler + it_mmg + it_mmq + it_exhTele + it_exhVmr, 2) AS stock,
        ROUND(pv_preciosubdistrib, 3) AS pv_preciosubdistrib,
        ROUND(pv_precio5, 3)  AS pv_precio5,
        ROUND(pv_precio6, 3)  AS pv_precio6,
        ROUND(pv_precio7, 3)  AS pv_precio7,
        it_descripcion,
        um_pesoCE,
        it_costoprom
    FROM iv_ws_item
    WHERE it_referencia LIKE ?
    ORDER BY stock DESC
""".trimIndent()

        val args = arrayOf("$referencia%")   // ← solo 2 parámetros
        val cursor = db.rawQuery(sql, args)

        if (cursor.moveToFirst()) {
            do {
                val nuevaReferencia = Precios(
                    referencia = cursor.getString(cursor.getColumnIndexOrThrow("it_referencia")),
                    codigo     = cursor.getString(cursor.getColumnIndexOrThrow("it_codigo")),
                    stock      = cursor.getDouble(cursor.getColumnIndexOrThrow("stock")),
                    precioSub  = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_preciosubdistrib")),
                    precioCont = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio5")),
                    precioCred = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio6")),
                    descripcion= cursor.getString(cursor.getColumnIndexOrThrow("it_descripcion")) ?: "",
                    unidadCE   = cursor.getDouble(cursor.getColumnIndexOrThrow("um_pesoCE")),
                    costoProm  = cursor.getDouble(cursor.getColumnIndexOrThrow("it_costoprom")),
                    pv_precio7 = cursor.getDouble(cursor.getColumnIndexOrThrow("pv_precio7"))
                )
                referencias.add(nuevaReferencia)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return referencias
    }

    fun fnObtenerStockEnLinea(referencia: String): List<String> {
        val db = DatabaseManager.openDatabase(context)
        val valores = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT i.it_referencia AS Referencia," +
                    "i.it_descripcion AS Descripcion, " +
                    "ROUND(i.it_teler,2) AS StockTelerepuesto, " +
                    "ROUND(i.it_exhTele,2) AS StockExhTelerepuestos, " +
                    "ROUND(i.it_almesa,2) AS StockPortrans, " +
                    "ROUND(i.it_mmg,2) AS StockProMarket, " +
                    "ROUND(i.it_exhVmr,2) AS StockExhProMarket, " +
                    "ROUND(i.it_mmq ,2) AS StockReparadaAlm, " +
                    "ROUND(i.it_dcp,2) AS StockDepComercial, " +
                    "ROUND(i.pv_preciosubdistrib,3) AS SubDistribuidor, " +
                    "ROUND(i.pv_desctosubdistrib,3) AS DsctoSubDistr, " +
                    "ROUND(i.pv_precio5,3) AS Contado, " +
                    "ROUND(i.pv_precio6,3) AS Credito, " +
                    "ROUND(i.um_unidadCM,3) AS CartonMaster, " +
                    "ROUND(i.um_unidadCE,2) AS CartonEstandar, " +
                    "i.um_sku AS Sku, " +
                    "ROUND(i.um_pesoCE,3) AS PesoCE " +
                    "FROM iv_ws_item i " +
                    "WHERE i.it_referencia = ?",
            arrayOf(referencia)
        )

        val columnNames = cursor.columnNames
        while (cursor.moveToNext()) {
            for (columnName in columnNames) {
                val valor = cursor.getStringOrNull(cursor.getColumnIndex(columnName))
                valores.add(valor ?: "0")
            }
        }
        cursor.close()
        db.close()

        return valores
    }


}

data class SpinnerItem(
    val codigo: Int, val estado: String, val descripcion: String
)

{
override fun toString(): String = descripcion  // Para que ArrayAdapter muestre la descripción
}

data class Factura(
    val sri: String,
    val fecha: String,
    val vendedor: String,
    val tipoDoc: String,
    val valor: Double,
    val saldo: Double,
    val detCheque: String,
    val dias: String,
    val botton : Boolean
)


data class Cupo(
    val total: Double,
    val utilizado: Double,
    val disponible: Double,
    val patrimonio: Double,
    val descripcion: String,
    val valorBase: Double,
    val valorDescuento: Double,
    val valorSeguro: Double,
    val valorIva: Double,
    val valorFlete: Double,
    val valorTotal: Double,
    val observacion: String
)

data class Observacion(
    val secuencia: Int,
    val observacion: String,
    val usuario: String,
    val fecha: String
)

data class DetallePedido(
    val codigo: String,
    val descripcion: String,
    val titulo: String,
    val marca: String,
    val subFamilia: String,
    val cantidad: Double,
    val precioPedido: Double,
    val precioUnitario: Double,
    val costoProm: Double,
    val subtotal: Double,
    val total: Double,
    var aceptarPrecio: Boolean = false
)

data class CxcItem(
    val cc_sec: Int,
    val cc_descripcion: String,
    val cl_saldo: Double
)

data class Rentabilidades(
    val dp_subtotalcn: Double,
    val dp_subtotalcp: Double,
    val dp_subtotalReal: Double,
    val descuentoReal: Double
)

data class RentabilidadVendedor(
    val tipo: String,
    val vendedor: String,
    val totalMenosDescuento: Double,
    val costoNacionalizadoKardex: Double,
    val utilidadNacionalizado: Double,
    val porcentajeAB: Double
)

data class RentabilidadActual(
    val bodega: String,
    val mes: String,
    val B: Double,
    val C: Double,
    val D: Double,
    val BC: Double
)

data class RentabilidadCliente(
    val cliente: String,
    val nombreCliente: String,
    val A: Double,
    val B: Double,
    val C: Double,
    val D: Double,
    val ATA: Double,
    val DTD: Double,
    val AC: Double,
    val BC: Double
)



object DatabaseManager {
    private var instance: SQLiteDatabase? = null

    fun openDatabase(context: Context): SQLiteDatabase {
        if (instance == null || !instance!!.isOpen) {
            instance = SqLiteOpenHelper(context).readableDatabase
        }
        return instance!!
    }

    fun closeDatabase() {
        instance?.close()
        instance = null
    }


}