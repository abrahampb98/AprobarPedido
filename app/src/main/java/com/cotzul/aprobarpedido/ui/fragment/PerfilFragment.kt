package com.cotzul.aprobarpedido.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cotzul.aprobarpedido.BuildConfig
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.network.SolicitudSoap
import com.cotzul.aprobarpedido.ui.activity.MainActivity
import com.cotzul.aprobarpedido.ui.fragment.LoginFragment.CadenaHolder.us_login
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.cotzul.aprobarpedido.utils.parser.XmlDesbloquearPedido

class PerfilFragment : Fragment(R.layout.fragment_perfil) {
    private lateinit var txtNombreUsuario: TextView
    private lateinit var llenarControles: ClsLLenarControles
    var ep_codigo :Int = 0
    private lateinit var txtVersionApp: TextView
    private lateinit var ClaseXmlPedido: XmlDesbloquearPedido
    lateinit var solicitudSoap: SolicitudSoap
    lateinit var dbHelper: SqLiteOpenHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)
        txtVersionApp = view.findViewById(R.id.txtVersionApp)

        llenarControles = ClsLLenarControles(requireContext())
        ep_codigo = LoginFragment.CadenaHolder.ep_codigo
        txtNombreUsuario.setText(llenarControles.fnObtenerNombreUsuario(ep_codigo))
        val versionName = BuildConfig.VERSION_NAME
        txtVersionApp.text = "Version App: $versionName"


        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sistema")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                fnCerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fnCerrarSesion() {
        fnDesbloquearPedidos()
    }


    fun fnDesbloquearPedidos(){

            ClaseXmlPedido = XmlDesbloquearPedido(requireContext())
            solicitudSoap = SolicitudSoap(requireContext())
            MiAsyncTaskDesbloquearPedido().execute()
    }

    private inner class MiAsyncTaskDesbloquearPedido () :
        AsyncTask<Void, Void, String>() {


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {

            val cadena = ClaseXmlPedido.fnObtenerPedidosXML(us_login)

            solicitudSoap.initializeVariables(getString(R.string.str_DesbloquearPedidos).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            return pedido
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            // 1️⃣ Limpia las preferencias
            val prefs = requireContext().getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 2️⃣ Retraso breve antes de reiniciar (para evitar pantalla blanca)
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }, 150) // ⏱️ Retraso de 150 ms: suficiente para suavizar la transición
        }

    }

}
