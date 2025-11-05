package com.cotzul.aprobarpedido.ui.fragment

import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.Layout
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.network.SolicitudSoap
import com.cotzul.aprobarpedido.ui.adapters.AdaptadorPrecio
import com.cotzul.aprobarpedido.ui.adapters.Precios
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.cotzul.aprobarpedido.utils.parser.XmlParserProductos

class PreciosFragment : Fragment(R.layout.fragment_precios), AdaptadorPrecio.OnItemClickListener{
    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var edtBuscarReferencia: EditText
    private lateinit var recyclerProductos: RecyclerView
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper
    private val datosList = mutableListOf<Precios>()
    private lateinit var adaptadorPrecios: AdaptadorPrecio
    var referencia: String = ""
    private lateinit var layoutNoResultados: LinearLayout


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llenarControles = ClsLLenarControles(requireContext())
        edtBuscarReferencia = view.findViewById(R.id.edtBuscarReferencia)
        recyclerProductos = view.findViewById(R.id.recyclerProductos)
        dbHelper = SqLiteOpenHelper(requireContext())
        adaptadorPrecios = AdaptadorPrecio(datosList,this)
        layoutNoResultados = view.findViewById(R.id.layoutNoResultados)


        edtBuscarReferencia.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                fnBuscarProductos()
                true // indica que el evento fue manejado
            } else {
                false
            }
        }

    }

    override fun onItemClick(item: Precios) {

    }

    override fun btnVerMas(item: Precios) {
        fnPreciosStock(item.referencia)
    }

    fun fnBuscarProductos(){
        if (edtBuscarReferencia.text.toString().isNotEmpty()) {
            if (fnIsNetworkAvailable(requireContext())) {
                datosList.clear()
                adaptadorPrecios.clearItems()
                referencia = edtBuscarReferencia.text.toString()
                solicitudSoap = SolicitudSoap(requireContext())
                val progressDialog = fnShowProgressDialog()
                MiAsyncTaskPedido(progressDialog, referencia).execute()
            } else {
                showToast("Verifique su conexión a internet")
            }
        }else {
            showToast("Debe Seleccionar un Pedido")
        }
    }

    fun fnIsNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun fnShowProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Cargando Producto (s)...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    private inner class MiAsyncTaskPedido (private val progressDialog: ProgressDialog, private val referencia: String) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {
            database = dbHelper.writableDatabase

            database.execSQL("DELETE FROM iv_ws_item")



            val cadena = "2,1,3,'$referencia'"

            solicitudSoap.initializeVariables(getString(R.string.str_precios).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            if (!result.isNullOrBlank()) {
                pedido = XmlParserProductos.parserProdcutos(result,database,requireContext())
            }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            fnCargarDatosProductos(progressDialog)
        }

    }

    fun fnCargarDatosProductos(progressDialog: ProgressDialog){
        val resultados = llenarControles.fnBuscarReferencia(referencia)

        for (dato in resultados) {
            datosList.add(dato)
        }
        // Configura el RecyclerView y asigna el adaptador
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerProductos.layoutManager = layoutManager
        recyclerProductos.adapter = adaptadorPrecios

        if (resultados.isEmpty()) {
            recyclerProductos.visibility = View.GONE
            layoutNoResultados.visibility = View.VISIBLE
        } else {
            recyclerProductos.visibility = View.VISIBLE
            layoutNoResultados.visibility = View.GONE
        }



        fnOcultarTeclado()

        progressDialog.dismiss()
    }


    fun fnPreciosStock(referencia: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.detalles_producto, null) // Usa el nombre de tu archivo XML

        val selectedItems = referencia
        if (selectedItems.isNullOrEmpty()) {
            showToast("No se han seleccionado elementos para agregar")
        } else {
            // Obtén el primer ítem seleccionado
            val primerItem = selectedItems.firstOrNull()
            val tituloDialogo = referencia ?:  "Detalles del Ítem"


            // Asigna valores de stock comenzando desde el segundo valor en el array de stock
            primerItem?.let {
                val stock = llenarControles.fnObtenerStockEnLinea(referencia)

                // Empieza a asignar desde el segundo valor (índice 1 en la lista)
                stock.drop(1).forEachIndexed { index, valor ->
                    val textViewId = resources.getIdentifier("valueItem${index + 1}", "id", requireActivity().packageName)
                    val textView = view.findViewById<TextView>(textViewId)
                    textView?.text = valor
                }
            }

            // Crear y mostrar el diálogo con el título modificado
            AlertDialog.Builder(requireContext())
                .setTitle(tituloDialogo)
                .setView(view)
                .setCancelable(true)
                .show()
        }
    }

    fun fnOcultarTeclado() {
        val inputMethodManager =
            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


}