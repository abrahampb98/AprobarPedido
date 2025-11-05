package com.cotzul.aprobarpedido.data

// En tu clase SqLiteOpenHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, "db_gerencia.db", null, 1) { //1

    override fun onCreate(db: SQLiteDatabase) {
        Log.e("SqLiteOpenHelper", "Creando la base de datos")
        verificarTablas(db)
        crearTablas(db)
    }
    private fun crearTablas(db: SQLiteDatabase) {


        val tablaUsuario= """ CREATE TABLE IF NOT EXISTS se_ws_usuario (
            ep_codigo INTEGER ,
            us_login TEXT NOT NULL,
            us_nombre TEXT NOT NULL,
            us_password TEXT NOT NULL,
            us_fechaing TEXT NOT NULL
        );
        """.trimIndent()
        db.execSQL(tablaUsuario)


        val tablaTipoPedido= """ CREATE TABLE IF NOT EXISTS fa_ws_estadoPedido (
            ep_codigo INTEGER ,
            ep_estadoPedido TEXT    NOT NULL,
            ep_descripcion TEXT    NOT NULL
        );
        """.trimIndent()
        db.execSQL(tablaTipoPedido)

        val tablaPedido= """ CREATE TABLE IF NOT EXISTS fa_ws_Pedido (
            pe_coddocumento INTEGER ,
            cl_codigo INTEGER NOT NULL,
            cl_cliente TEXT NOT NULL,
            vn_vendedor TEXT NOT NULL,
            pe_estado TEXT NOT NULL,
            pe_valorTotal REAL NOT NULL
        );
        """.trimIndent()
        db.execSQL(tablaPedido)

        //resumen cxc
        val tablaCxC = """ CREATE TABLE IF NOT EXISTS cc_ws_cxc (
                cc_sec INTEGER ,
                cc_descripcion TEXT ,
                cl_codigo INTEGER ,
                cl_cliente TEXT ,
                cl_saldo REAL
            );
            """.trimIndent()
        db.execSQL(tablaCxC)


        val tablaCabPedido = """ CREATE TABLE IF NOT EXISTS fa_ws_CabPedido (
                pe_coddocumento INTEGER ,
                cl_codigo INTEGER ,
                cl_cliente INTEGER ,
                vn_vendedor INTEGER ,
                cl_accion TEXT ,
                cc_cupoasignado REAL ,
                cc_cupoutilizado REAL,
                cc_cupodisponible REAL ,
                cl_valorpatrimonio REAL ,
                cl_observacion TEXT ,
                cl_usuario REAL ,
                pp_descripcion REAL ,
                pe_valorbruto REAL ,
                pe_valordescuento REAL ,
                pe_seguro REAL ,
                pe_valoriva REAL ,
                pe_flete REAL ,
                pe_valorTotal REAL
            );
            """.trimIndent()
    db.execSQL(tablaCabPedido)

        val tablaDetallePedido = """
            CREATE TABLE IF NOT EXISTS fa_ws_DetallePedido (
                pe_coddocumento INTEGER ,
                cl_codigo INTEGER ,
                cl_cliente TEXT,
                it_codigo TEXT,
                dp_secuencia INTEGER,
                dp_descripcion TEXT,
                it_titulo TEXT,
                ma_descripcion TEXT,
                dp_cantidad REAL ,
                dp_facturar REAL,
                dp_preciopedido REAL ,
                dp_preciounitario REAL,
                dp_total REAL,
                dp_costonac REAL,
                dp_subtotalcn REAL,
                it_subfamilia TEXT,
                dp_costopromedio REAL,
                dp_subtotalcostopromedio REAL            
            );
        """.trimIndent()

        db.execSQL(tablaDetallePedido)

        //detalle de las cxc en observaciones
        val tablaAuditoriaPedido= """ CREATE TABLE IF NOT EXISTS fa_ws_AuditoriaPedido (
                pe_coddocumento INTEGER ,
                pe_detdocumento TEXT ,
                pe_usuario TEXT ,
                pe_observacion TEXT ,
                pe_fecha TEXT
            );
            """.trimIndent()
        db.execSQL(tablaAuditoriaPedido)


        val tablacabcobro = """ CREATE TABLE IF NOT EXISTS cc_ws_cabcobro (
            TipoD TEXT,
            OrdenDoc INTEGER,
            TipoDocumento TEXT,
            Empresa TEXT,
            Bodega TEXT,
            Provincia TEXT,
            Ciudad TEXT,
            cl_codigo INTEGER,
            Cliente TEXT,
            ObservacionCliente TEXT,
            CupoTotal REAL,
            CupoDisponible REAL,
            FechaDoc TEXT,
            Sri TEXT,
            NumDoc TEXT,
            EtiquetaDoc TEXT,
            Vendedor TEXT,
            Total REAL,
            Saldo REAL,
            ValorCuota REAL,
            NumCheque TEXT,
            FactEmitida TEXT,
            FechaOrden TEXT,
            us_estado TEXT,
            patrimonio REAL
        );
    """.trimIndent()
        db.execSQL(tablacabcobro)


        val tablaRentabilidad= """ 
            CREATE TABLE IF NOT EXISTS fa_ws_rentabilidad (
                re_codigo INTEGER ,
                re_descripcion TEXT 
            );
            """.trimIndent()
        db.execSQL(tablaRentabilidad)

        val tablaRentabilidadVendedor = """
            CREATE TABLE IF NOT EXISTS fa_ws_rentabilidadVendedor (
                supervisor TEXT,
                vendedor TEXT,
                nombrevendedor TEXT,
                subtotal REAL,
                costoNacionalizadoKardex REAL,
                utilidadKardex REAL,
                costoISDGastoImportacion REAL,
                utilidadNacionalizado REAL,
                tipo TEXT,
                codtipo TEXT,
                orden INTEGER
            );
        """.trimIndent()

        db.execSQL(tablaRentabilidadVendedor)

        val tablaRentabilidadCliente = """
            CREATE TABLE IF NOT EXISTS fa_ws_rentabilidadCliente (
                supervisor TEXT,
                cliente TEXT,
                nombrecliente TEXT,
                subtotal REAL,
                costoNacionalizadoKardex REAL,
                utilidadKardex REAL,
                costoISDGastoImportacion REAL,
                utilidadNacionalizado REAL,
                totalMenosDescuentoSeguro REAL
            );
        """.trimIndent()

        db.execSQL(tablaRentabilidadCliente)


        val tablaRentabilidadActual = """
            CREATE TABLE IF NOT EXISTS fa_ws_rentabilidadActual (
                supervisor TEXT,
                mes TEXT,
                bodega TEXT,
                bo_orden TEXT,
                subtotal REAL,
                costoNacionalizadoKardex REAL,
                utilidadKardex REAL,
                costoISDGastoImportacion REAL,
                utilidadNacionalizado REAL,
                totalMenosDescuentoSeguro REAL
            );
        """.trimIndent()

        db.execSQL(tablaRentabilidadActual)


        val tablaItem = "CREATE TABLE  IF NOT EXISTS  iv_ws_item (" +
                "em_codigo INTEGER," +
                "bo_codigo INTEGER," +
                "it_codigo TEXT,"+
                "it_referencia TEXT,"+
                "it_descripcion TEXT,"+
                "it_titulo TEXT,"+
                "it_familia TEXT," +
                "it_marca TEXT," +
                "it_almesa TEXT," +
                "it_teler TEXT," +
                "it_mmg TEXT," +
                "it_mmq TEXT," +
                "pv_precio5 TEXT," +
                "pv_precio6 TEXT," +
                "pv_precio7 TEXT," +
                "it_fechaing TEXT," +
                "um_unidadCM TEXT," +
                "um_unidadCE TEXT," +
                "um_sku TEXT," +
                "um_pesoCE TEXT," +
                "pv_preciosubdistrib TEXT," +
                "pv_desctosubdistrib TEXT," +
                "pv_costoN TEXT," +
                "it_exhVmr TEXT," +
                "it_dcp TEXT," +
                "it_exhTele TEXT," +
                "it_costoprom TEXT," +
                "it_activaex TEXT," +
                "it_regalo TEXT," +
                "it_formaPago INTEGER)"
        db.execSQL(tablaItem)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("SqLiteOpenHelper", "Actualizando la base de datos")

        // Verificar y agregar columnas si no existen
       /* agregarColumnaSiNoExiste(db,"ve_ws_vendedor", "bo_codigo", "TEXT")*/

        // Llamar a crearTablas si es necesario
        crearTablas(db)
    }

    private fun agregarColumnaSiNoExiste(db: SQLiteDatabase, tabla: String, columna: String, tipo: String) {
        val cursor = db.rawQuery("PRAGMA table_info($tabla)", null)
        var columnaExiste = false

        if (cursor != null) {
            val indexName = cursor.getColumnIndex("name")
            if (indexName != -1) { // Verificar si el índice de la columna "name" es válido
                while (cursor.moveToNext()) {
                    val nombreColumna = cursor.getString(indexName)
                    if (columna == nombreColumna) {
                        columnaExiste = true
                        break
                    }
                }
            } else {
                Log.e("SQLiteUpgrade", "No se encontró la columna 'name' en PRAGMA table_info para la tabla $tabla")
            }
            cursor.close()
        }

        if (!columnaExiste) {
            Log.d("SQLiteUpgrade", "Agregando columna $columna a la tabla $tabla")
            db.execSQL("ALTER TABLE $tabla ADD COLUMN $columna $tipo")
        } else {
            Log.d("SQLiteUpgrade", "La columna $columna ya existe en la tabla $tabla")
        }
    }


    private fun verificarTablas(db: SQLiteDatabase) {
        val tablas = listOf(
            "se_ws_usuario",
            "fa_ws_estadoPedido",
            "fa_ws_Pedido",
            "cc_ws_cxc",
            "fa_ws_CabPedido",
            "fa_ws_DetallePedido",
            "fa_ws_AuditoriaPedido",
            "cc_ws_cabcobro",
            "iv_ws_item"
        )

        for (tabla in tablas) {
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(tabla)
            )
            val existe = cursor.count > 0
            cursor.close()

            if (!existe) {
                Log.w("SQLiteHelper", "⚠️ Tabla faltante detectada: $tabla — Creando...")
                crearTablas(db)
                break
            }
        }
    }



}
