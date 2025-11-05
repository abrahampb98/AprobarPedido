package com.cotzul.aprobarpedido.ui.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.network.SolicitudSoap
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.cotzul.aprobarpedido.utils.cls.RentabilidadActual
import com.cotzul.aprobarpedido.utils.cls.RentabilidadCliente
import com.cotzul.aprobarpedido.utils.cls.RentabilidadVendedor
import com.cotzul.aprobarpedido.utils.cls.SpinnerItem
import com.cotzul.aprobarpedido.utils.parser.XmlDesbloquearPedido
import com.cotzul.aprobarpedido.utils.parser.XmlParserPedidos
import com.cotzul.aprobarpedido.utils.parser.XmlParserRentabilidades
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RentabilidadFragment : Fragment(R.layout.fragment_rentabilidad) {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtFechaHoy: TextView
    private lateinit var spinnerTipoRentabilidad: Spinner
    private lateinit var btnFechaIni: MaterialButton
    private lateinit var btnFechaFin: MaterialButton
    private lateinit var btnBuscarRentabilidad: Button
    private var isDatePickerShown = false
    lateinit var solicitudSoap: SolicitudSoap
    private lateinit var contenedorFijas: LinearLayout
    private lateinit var contenedorScroll: LinearLayout
    private lateinit var contenedorDetFijas: LinearLayout
    private lateinit var contenedorDetScroll: LinearLayout
    private lateinit var scrollCabeceras: HorizontalScrollView
    private lateinit var scrollDetalles: HorizontalScrollView
    private var opcion: Int = 0
    lateinit var dbHelper: SqLiteOpenHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llenarControles = ClsLLenarControles(requireContext())
        dbHelper = SqLiteOpenHelper(requireContext())

        txtFechaHoy = view.findViewById(R.id.txtFechaHoy)
        btnFechaIni = view.findViewById(R.id.btnFechaIni)
        btnFechaFin = view.findViewById(R.id.btnFechaFin)
        btnBuscarRentabilidad = view.findViewById(R.id.btnBuscarRentabilidad)
        spinnerTipoRentabilidad = view.findViewById(R.id.spinnerTipoRentabilidad)
        contenedorFijas = requireView().findViewById(R.id.contenedorCabecerasFijas)
        contenedorScroll = requireView().findViewById(R.id.contenedorCabecerasScroll)
        contenedorDetFijas = requireView().findViewById(R.id.contenedorDetallesFijos)
        contenedorDetScroll = requireView().findViewById(R.id.contenedorDetallesScroll)
        scrollCabeceras = view.findViewById(R.id.scrollCabeceras)
        scrollDetalles = view.findViewById(R.id.scrollDetalles)

        btnFechaIni.text = fnFechaBoton()
        btnFechaFin.text = fnFechaBoton()


        txtFechaHoy.setText(fnFecha())
        llenarControles.fnLLenarSpinnerTipoRentabilidad(spinnerTipoRentabilidad)


        spinnerTipoRentabilidad.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val itemSeleccionado = parent?.getItemAtPosition(position) as SpinnerItem
                    opcion = itemSeleccionado.codigo

                    // 🧹 Limpia vistas anteriores
                    contenedorFijas.removeAllViews()
                    contenedorScroll.removeAllViews()
                    contenedorDetFijas.removeAllViews()
                    contenedorDetScroll.removeAllViews()

                    // 📐 Función auxiliar para convertir dp → px
                    fun dpToPx(dp: Float): Int =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dp,
                            resources.displayMetrics
                        ).toInt()

                    val altoPx = dpToPx(35f)

                    // 🧹 Normaliza el texto (quita espacios, puntos, etc.)
                    fun normalizarTitulo(titulo: String): String =
                        titulo.replace(" ", "")
                            .replace(".", "")
                            .replace("%", "")
                            .replace("-", "")
                            .replace("(", "")
                            .replace(")", "")
                            .replace("+", "")
                            .replace("/", "")
                            .trim()
                            .uppercase()

                    // === 🎯 Define anchos por campo según la opción seleccionada ===
                    val anchoCampos: Map<String, Int> = when (opcion) {

                        // 🔸 Rentabilidad Vendedor
                        28 -> mapOf(
                            "TIPO" to dpToPx(80f),
                            "VENDEDOR" to dpToPx(160f),
                            "ATOTDESC" to dpToPx(90f),
                            "BCOSTONACKARDEX" to dpToPx(100f),
                            "CUTINACKARDEX" to dpToPx(100f),
                            "AB" to dpToPx(80f)
                        )

                        // 🔸 Rentabilidad Cliente
                        26 -> mapOf(
                            "CODIGO" to dpToPx(70f),
                            "CLIENTE" to dpToPx(170f),
                            "ATOTDESC" to dpToPx(90f),
                            "BASEGURO" to dpToPx(90f),
                            "CCOSTPROMKARDEX" to dpToPx(90f),
                            "DUTILIDADKARDEX" to dpToPx(90f),
                            "ATA" to dpToPx(80f),
                            "DTD" to dpToPx(80f),
                            "AC" to dpToPx(80f),
                            "BC" to dpToPx(80f)
                        )

                        // 🔸 Rentabilidad Actual
                        24 -> mapOf(
                            "BODEGA" to dpToPx(120f),
                            "MES" to dpToPx(90f),
                            "BASEGURO" to dpToPx(90f),
                            "CCOSTPROM" to dpToPx(90f),
                            "DUTKARDEX" to dpToPx(90f),
                            "BC" to dpToPx(90f)
                        )

                        else -> emptyMap()
                    }

                    // === 1️⃣ Obtén las cabeceras ===
                    val headers = when (opcion) {
                        28 -> resources.getStringArray(R.array.headers_rentabilidad_vendedor)
                        26 -> resources.getStringArray(R.array.headers_rentabilidad_cliente)
                        24 -> resources.getStringArray(R.array.headers_rentabilidad_actual)
                        else -> emptyArray()
                    }

                    val fijas = headers.take(2)
                    val scrollables = headers.drop(2)

                    // === 🧱 Función para crear una celda de cabecera ===
                    fun crearCelda(titulo: String): TextView {
                        return TextView(requireContext()).apply {
                            text = titulo
                            textSize = 12f
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            setTypeface(null, Typeface.BOLD)
                            gravity = Gravity.CENTER_HORIZONTAL
                            setPadding(2, 2, 2, 2)
                            setBackgroundResource(R.drawable.bg_header_cell)
                        }
                    }

                    // === 2️⃣ Crea las columnas fijas ===
                    fijas.forEach { titulo ->
                        val claveNormalizada = normalizarTitulo(titulo)
                        val ancho = anchoCampos[claveNormalizada] ?: dpToPx(100f)
                        val celda = crearCelda(titulo)
                        val params = LinearLayout.LayoutParams(ancho, altoPx)
                        params.setMargins(2, 2, 2, 2)
                        contenedorFijas.addView(celda, params)
                    }

                    // === 3️⃣ Crea las columnas desplazables ===
                    scrollables.forEach { titulo ->
                        val claveNormalizada = normalizarTitulo(titulo)
                        val ancho = anchoCampos[claveNormalizada] ?: dpToPx(100f)
                        val celda = crearCelda(titulo)
                        val params = LinearLayout.LayoutParams(ancho, altoPx)
                        params.setMargins(2, 2, 2, 2)
                        contenedorScroll.addView(celda, params)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        btnFechaIni.setOnClickListener {
            if (!isDatePickerShown) {
                showDatePickerDialog(btnFechaIni)
            }
        }

        btnFechaFin.setOnClickListener {
            if (!isDatePickerShown) {
                showDatePickerDialog(btnFechaFin)
            }
        }

        btnBuscarRentabilidad.setOnClickListener {
            fnConsultarRentabilidad()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) { // true = activa el bloqueo
                override fun handleOnBackPressed() {
                    // No haces nada aquí => botón atrás bloqueado
                }
            }
        )

    }

    private fun fnFecha(): String {
        val tz = TimeZone.getTimeZone("America/Guayaquil")
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale("es", "EC"))
        fmt.timeZone = tz
        return "Fecha: ${fmt.format(Date())}"
    }

    private fun fnFechaBoton(): String {
        val tz = TimeZone.getTimeZone("America/Guayaquil")
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale("es", "EC"))
        fmt.timeZone = tz
        return fmt.format(Date())
    }


    private fun showDatePickerDialog(targetButton: MaterialButton) {
        // Evita abrir más de uno
        if (isDatePickerShown) return

        isDatePickerShown = true

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.CustomMaterialDatePickerTheme)
            .build()

        // Listener al confirmar la fecha
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection
            }
            val formattedDate = formatDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            targetButton.text = formattedDate
        }

        // Cuando se cierra el picker (por cualquier motivo)
        datePicker.addOnDismissListener {
            isDatePickerShown = false
        }

        // Mostrar el selector
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
    }

    fun fnIsNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun fnConsultarRentabilidad() {
        // Limpia el detalle anterior
        contenedorDetFijas.removeAllViews()
        contenedorDetScroll.removeAllViews()
        if (fnIsNetworkAvailable(requireContext())) {
            if (opcion != 0) {
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = fnShowProgressDialog()
                val fechaInicio = btnFechaIni.text.toString().replace("/", "-")
                val fechaFin = btnFechaFin.text.toString().replace("/", "-")
                MiAsyncTaskRentabilidades(progressDialog, fechaInicio, fechaFin).execute()
            }
        } else {
            Toast.makeText(requireContext(), "Verifique su conexión a internet", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun fnShowProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Rentabilidad...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }


    private inner class MiAsyncTaskRentabilidades(
        private val progressDialog: ProgressDialog,
        private val fechaInc: String,
        private val fechaFin: String
    ) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM fa_ws_rentabilidadVendedor")
            database.execSQL("DELETE FROM fa_ws_rentabilidadCliente")
            database.execSQL("DELETE FROM fa_ws_rentabilidadActual")

            val cadena = "2,0,'$fechaInc','$fechaFin','3','9',0,$opcion"

            solicitudSoap.initializeVariables(getString(R.string.str_Rentabilidad).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserRentabilidades.parserRentabilidad(
                    result,
                    database,
                    requireContext(),
                    opcion
                )
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            fnLLenarDetalles()
            progressDialog.dismiss()

        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun fnLLenarDetalles() {

        // 🧹 Limpia SOLO los detalles
        contenedorDetFijas.removeAllViews()
        contenedorDetScroll.removeAllViews()

        val altoPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 35f, resources.displayMetrics
        ).toInt()

        fun crearCelda(texto: String, esTotal: Boolean = false): TextView {
            return TextView(requireContext()).apply {
                text = texto
                textSize = 11f
                gravity = Gravity.CENTER_HORIZONTAL
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setBackgroundResource(
                    if (esTotal) R.drawable.bg_header_cell else R.drawable.bg_cell_borde
                )
                setTypeface(null, if (esTotal) Typeface.BOLD else Typeface.NORMAL)
                setPadding(2, 2, 2, 2)
            }
        }

        // 🧩 Función para limpiar ceros
        fun limpiarCero(texto: String): String {
            return if (texto.contains("0.00") || texto == "$ 0.00" || texto == "0.00%" || texto == "0.0%" || texto == "0%" || texto == "$ 0.0") {
                ""
            } else texto
        }

        // === 🧩 OBTENER ANCHOS REALES DE LAS CABECERAS ===
        fun obtenerAnchosCabeceras(contenedor: LinearLayout): List<Int> {
            val anchos = mutableListOf<Int>()
            for (i in 0 until contenedor.childCount) {
                val view = contenedor.getChildAt(i)
                anchos.add(view.width.takeIf { it > 0 } ?: view.layoutParams.width)
            }
            return anchos
        }

        val anchosFijas = obtenerAnchosCabeceras(contenedorFijas)
        val anchosScroll = obtenerAnchosCabeceras(contenedorScroll)

        val datos = when (opcion) {
            28 -> llenarControles.fnObtenerRentabilidadVendedor()
            26 -> llenarControles.fnObtenerRentabilidadCliente()
            24 -> llenarControles.fnObtenerRentabilidadActual()
            else -> emptyList<Any>()
        }

        // === 🔹 LLENAR DETALLES ===
        datos.forEach { fila ->
            val filaFijas = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            val filaScroll = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }

            val valoresFijos: List<String>
            val valoresScroll: List<String>

            when (opcion) {
                28 -> {
                    val v = fila as RentabilidadVendedor
                    valoresFijos = listOf(v.tipo, v.vendedor)
                    valoresScroll = listOf(
                        limpiarCero("$ %.2f".format(v.totalMenosDescuento)),
                        limpiarCero("$ %.2f".format(v.costoNacionalizadoKardex)),
                        limpiarCero("$ %.2f".format(v.utilidadNacionalizado)),
                        limpiarCero("%.2f%%".format(v.porcentajeAB))
                    )
                }
                26 -> {
                    val c = fila as RentabilidadCliente
                    valoresFijos = listOf(c.cliente, c.nombreCliente)
                    valoresScroll = listOf(
                        limpiarCero("$ %.2f".format(c.A)),
                        limpiarCero("$ %.2f".format(c.B)),
                        limpiarCero("$ %.2f".format(c.C)),
                        limpiarCero("$ %.2f".format(c.D)),
                        limpiarCero("%.2f%%".format(c.ATA)),
                        limpiarCero("%.2f%%".format(c.DTD)),
                        limpiarCero("%.2f%%".format(c.AC / 100)),
                        limpiarCero("%.2f%%".format(c.BC / 100))
                    )
                }
                24 -> {
                    val a = fila as RentabilidadActual
                    valoresFijos = listOf(a.bodega, a.mes)
                    valoresScroll = listOf(
                        limpiarCero("$ %.2f".format(a.B)),
                        limpiarCero("$ %.2f".format(a.C)),
                        limpiarCero("$ %.2f".format(a.D)),
                        limpiarCero("%.2f%%".format(a.BC))
                    )
                }
                else -> {
                    valoresFijos = emptyList()
                    valoresScroll = emptyList()
                }
            }

            // Fijas
            valoresFijos.forEachIndexed { i, valor ->
                val ancho = anchosFijas.getOrNull(i)
                    ?: TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics).toInt()
                filaFijas.addView(crearCelda(valor), LinearLayout.LayoutParams(ancho, altoPx))
            }

            // Scrollables
            valoresScroll.forEachIndexed { i, valor ->
                val ancho = anchosScroll.getOrNull(i)
                    ?: TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics).toInt()
                filaScroll.addView(crearCelda(valor), LinearLayout.LayoutParams(ancho, altoPx))
            }

            contenedorDetFijas.addView(filaFijas)
            contenedorDetScroll.addView(filaScroll)
        }

        // === 🧮 AGREGAR FILA DE TOTALES (sin tocar estructura) ===
        if (datos.isNotEmpty()) {
            val filaTotalesFijas = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            val filaTotalesScroll = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }

            val textoTotal = crearCelda("TOTAL", esTotal = true)
            val anchoTotal = anchosFijas.getOrNull(0) ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics
            ).toInt()
            filaTotalesFijas.addView(textoTotal, LinearLayout.LayoutParams(anchoTotal, altoPx))

            val anchoVacio = anchosFijas.getOrNull(1) ?: TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics
            ).toInt()
            filaTotalesFijas.addView(crearCelda("", esTotal = true), LinearLayout.LayoutParams(anchoVacio, altoPx))

            val sumas: List<Double> = when (opcion) {
                28 -> {
                    val lista = datos as List<RentabilidadVendedor>
                    listOf(lista.sumOf { it.totalMenosDescuento }, 0.0, 0.0, 0.0)
                }
                26 -> {
                    val lista = datos as List<RentabilidadCliente>
                    listOf(lista.sumOf { it.A }, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                }
                24 -> {
                    val lista = datos as List<RentabilidadActual>
                    listOf(lista.sumOf { it.B }, 0.0, 0.0, 0.0)
                }
                else -> emptyList()
            }

            sumas.forEachIndexed { i, valor ->
                val ancho = anchosScroll.getOrNull(i)
                    ?: TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110f, resources.displayMetrics).toInt()
                val texto = if (valor == 0.0) "" else "$ %.2f".format(valor)
                filaTotalesScroll.addView(crearCelda(texto, esTotal = true), LinearLayout.LayoutParams(ancho, altoPx))
            }

            contenedorDetFijas.addView(filaTotalesFijas)
            contenedorDetScroll.addView(filaTotalesScroll)
        }

        // === 🔄 Sincronización de scroll ===
        scrollCabeceras.setOnTouchListener { _, event ->
            scrollDetalles.onTouchEvent(event)
            false
        }
        scrollDetalles.setOnTouchListener { _, event ->
            scrollCabeceras.onTouchEvent(event)
            false
        }
    }


}
