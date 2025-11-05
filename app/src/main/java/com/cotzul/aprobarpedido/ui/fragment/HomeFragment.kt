package com.cotzul.aprobarpedido.ui.fragment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.network.SolicitudSoap
import com.cotzul.aprobarpedido.ui.adapters.AdaptadorPedido
import com.cotzul.aprobarpedido.ui.adapters.Pedidos
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.cotzul.aprobarpedido.utils.cls.SpinnerItem
import com.cotzul.aprobarpedido.utils.parser.XmlDesbloquearPedido
import com.cotzul.aprobarpedido.utils.parser.XmlParserCabCobro
import com.cotzul.aprobarpedido.utils.parser.XmlParserDetallesPedidos
import com.cotzul.aprobarpedido.utils.parser.XmlParserPedidos

class HomeFragment : Fragment(R.layout.fragment_home), AdaptadorPedido.OnItemClickListener {
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var spinnerOpciones: Spinner
    private lateinit var txtUsuario: TextView
    private lateinit var btnVerDetalles: Button
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    var ep_codigo :Int = 0
    var vgsEstado: String = ""
    var vgiOpcionSpiner: Int = 0
    var us_login: String = ""
    private lateinit var adaptadorDatosPedido: AdaptadorPedido
    private val datosList = mutableListOf<Pedidos>()
    private lateinit var recyclerViewDetalle: RecyclerView
    private lateinit var txtClienteAP: TextView
    private lateinit var txtDocumAP: TextView
    private lateinit var txtVendedorAP: TextView
    private lateinit var txtTotalAP: TextView
    private lateinit var ClaseXmlPedido: XmlDesbloquearPedido
    var pe_coddocumento: Int  = 0
    var cl_codigo : Int = 0
    var vgbEdicion: Boolean = false
    var vgbLoad: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtUsuario = view.findViewById(R.id.txtUsuario)
        btnVerDetalles = view.findViewById(R.id.btnVerDetalles)
        dbHelper = SqLiteOpenHelper(requireContext())
        llenarControles = ClsLLenarControles(requireContext())
        spinnerOpciones = view.findViewById(R.id.spinnerOpciones)
        llenarControles.fnLLenarSpinnerTipoPedido(spinnerOpciones)
        adaptadorDatosPedido = AdaptadorPedido(datosList, this)
        if (LoginFragment.CadenaHolder.ep_codigo != 0) {
            ep_codigo = LoginFragment.CadenaHolder.ep_codigo
            us_login = LoginFragment.CadenaHolder.us_login
            txtUsuario.setText(llenarControles.fnObtenerNombreUsuario(ep_codigo))
        }
        recyclerViewDetalle = view.findViewById(R.id.recyclerViewDetalle)
        txtClienteAP = view.findViewById(R.id.txtClienteAP)
        txtDocumAP = view.findViewById(R.id.txtDocumAP)
        txtVendedorAP = view.findViewById(R.id.txtVendedorAP)
        txtTotalAP = view.findViewById(R.id.txtTotalAP)


        // 🔹 Escucha el resultado del fragmento de edición
        setFragmentResultListener("pedidoEditadoKey") { _, bundle ->
            val actualizado = bundle.getBoolean("actualizado", false)
            if (actualizado) {
                fnLLenarPedidos()
            }
        }



        btnVerDetalles.setOnClickListener {
            fnConsultarPedido()
        }


        spinnerOpciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val itemSeleccionado = parent?.getItemAtPosition(position) as SpinnerItem
                if (adaptadorDatosPedido.getItemCount() > 1){
                    if (!vgbEdicion) {
                        fnDesbloquearPedidos()
                    }
                }
                if (!vgbEdicion) {
                    datosList.clear()
                    adaptadorDatosPedido.clearItems()
                    vgsEstado = itemSeleccionado.estado
                    vgiOpcionSpiner = itemSeleccionado.codigo
                    fnConsultarTipoPedido()
                    vgbLoad = true
                }else{
                    fnLLenarPedidos()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onItemClick(item: Pedidos) {
        txtClienteAP.setText(item.Cliente)
        txtDocumAP.setText(item.Documento.toString())
        txtVendedorAP.setText(item.Vendedor)
        txtTotalAP.setText(item.Valor.toString())
        pe_coddocumento = item.Documento
        cl_codigo = item.Codigo
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnConsultarTipoPedido(){
        if (vgiOpcionSpiner != -1){
            if (fnIsNetworkAvailable(requireContext())) {
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = fnShowProgressDialog()
                MiAsyncTaskTipoPedido(progressDialog).execute()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Verifique su conexión a internet",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else if (vgbLoad){
            fnDesbloquearPedidos()
        }
    }

    private inner class MiAsyncTaskTipoPedido(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM fa_ws_Pedido")

            var cadena = ""

            if (vgiOpcionSpiner == 0){
                cadena="2,1,0,$us_login,0,1,''"
            }else{
                cadena="2,2,0,$us_login,0,1,$vgsEstado"
            }

            solicitudSoap.initializeVariables(getString(R.string.str_Pedidos).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserPedidos.parserPedidos(result,database,requireContext())
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            if (result != null && result != "") {
                if (result.contains("Pedidos")) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Sistema")
                        .setMessage(result)
                        .setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    fnLLenarPedidos()
                }
            }else{
                AlertDialog.Builder(requireContext())
                    .setTitle("Sistema")
                    .setMessage("No Hay Pedidos Por Revisar")
                    .setPositiveButton("Aceptar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    }

    fun fnIsNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun fnShowProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Pedido (s)...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private fun fnShowProgressDialogDesbloquea(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Desbloqueando Pedido (s)...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    fun fnLLenarPedidos(){

        datosList.clear()
        adaptadorDatosPedido.clearItems()

        val resultados = llenarControles.fnCargarPedidos()
        for (dato in resultados) {
            datosList.add(dato)
        }
        // Configura el RecyclerView y asigna el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewDetalle.layoutManager = layoutManager
        recyclerViewDetalle.adapter = adaptadorDatosPedido
    }


    fun fnDesbloquearPedidos(){
        if (fnIsNetworkAvailable(requireContext())) {
            datosList.clear()
            adaptadorDatosPedido.clearItems()
            fnLimpiarControles()
            ClaseXmlPedido = XmlDesbloquearPedido(requireContext())
            solicitudSoap = SolicitudSoap(requireContext())
            val progressDialog = fnShowProgressDialogDesbloquea()
            MiAsyncTaskDesbloquearPedido(progressDialog).execute()
        } else {
            Toast.makeText(requireContext(),"Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    private inner class MiAsyncTaskDesbloquearPedido (private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            val cadena = ClaseXmlPedido.fnObtenerPedidosXML(us_login)

            solicitudSoap.initializeVariables(getString(R.string.str_DesbloquearPedidos).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
        }

    }


    fun fnConsultarPedido(){
        if (pe_coddocumento != 0) {
            if (fnIsNetworkAvailable(requireContext())) {
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = fnShowProgressDialog()
                MiAsyncTaskPedido(progressDialog).execute()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Verifique su conexión a internet",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else {
            showToast("Debe Seleccionar un Pedido")
        }
    }

    private inner class MiAsyncTaskPedido (private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM cc_ws_cxc")
            database.execSQL("DELETE FROM fa_ws_CabPedido")
            database.execSQL("DELETE FROM fa_ws_DetallePedido")
            database.execSQL("DELETE FROM fa_ws_AuditoriaPedido")

            var cadena ="2,0,0,'$us_login',$pe_coddocumento,2,''"

            solicitudSoap.initializeVariables(getString(R.string.str_Pedidos).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserDetallesPedidos.parseMultiTable(result,database,requireContext())
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            fnTraerCxC(progressDialog)
        }

    }

    fun fnTraerCxC( progressDialog: ProgressDialog){
        if (fnIsNetworkAvailable(requireContext())) {
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTaskTraerCxC(progressDialog).execute()
        } else {
            Toast.makeText(requireContext(),"Verifique su conexión a internet", Toast.LENGTH_LONG).show()
        }
    }


    private inner class MiAsyncTaskTraerCxC(private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM cc_ws_cabcobro")

            var cadena = "$cl_codigo,1"

            solicitudSoap.initializeVariables(getString(R.string.str_CxC).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserCabCobro.parseCabCobro(result,database,requireContext())
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //progressDialog.dismiss()
            if (result != null ) {
                fnAbrirEdicionPedido(progressDialog)
            }
        }

    }

    fun fnAbrirEdicionPedido(progressDialog: ProgressDialog) {
        vgbEdicion = true

        val bundle = Bundle().apply {
            putInt("documento", pe_coddocumento)
            putString("usuario", us_login)
            putInt("codigo", ep_codigo)
        }

        val editarPedidoFragment = EditarPedidoFragment().apply {
            arguments = bundle
        }

        // 🔹 Muestra y trae el overlay al frente antes de insertar el fragment
        val overlay = requireView().findViewById<View>(R.id.overlay_container)
        overlay.visibility = View.VISIBLE
        overlay.bringToFront()

        // 🔹 Reemplaza (no agregues) el contenido del overlay para evitar apilamiento visual
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
            )
            .replace(R.id.overlay_container, editarPedidoFragment, "editarPedido")
            .addToBackStack("editarPedido")
            .commit()

        progressDialog.dismiss()

    }

    fun fnLimpiarControles(){
        txtClienteAP.setText("")
        txtDocumAP.setText("")
        txtVendedorAP.setText("")
        txtTotalAP.setText("")
        pe_coddocumento = 0
        cl_codigo = 0
    }

}
