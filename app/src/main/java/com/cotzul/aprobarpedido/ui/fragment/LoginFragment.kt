package com.cotzul.aprobarpedido.ui.fragment

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.data.SqLiteOpenHelper
import com.cotzul.aprobarpedido.ui.activity.MainActivity
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtUsuario: TextInputEditText
    private lateinit var txtContrasena: TextInputEditText
    private lateinit var dbHelper: SqLiteOpenHelper
    private lateinit var database: SQLiteDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = SqLiteOpenHelper(requireContext())
        database = dbHelper.writableDatabase

        llenarControles = ClsLLenarControles(requireContext())
        txtUsuario = view.findViewById(R.id.edtUser)
        txtContrasena = view.findViewById(R.id.edtPassword)

        llenarControles.fnInsertarTiposPedido()
        llenarControles.fnInsertarTiposRentabilidad()

        fnLLenarusuarios()

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            fnIniciarSesion()
        }
    }

    fun fnLLenarusuarios(){
        val vlsFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        llenarControles.fnInsertUsuario(16,"aledesma","Ing. Angel Ledesma Ginatta","0904148327",vlsFecha)
        llenarControles.fnInsertUsuario(82,"adledesma","Ab. Angel Ledesma Marriot","0908890825",vlsFecha)
        llenarControles.fnInsertUsuario(2943,"dreyes","Daniel Reyes Urquiza","0920014693",vlsFecha)
        llenarControles.fnInsertUsuario(74764,"apaladines","Augusto Paladines Bayas","0952053031",vlsFecha)

    }

    fun fnValidarUsuario(usuario:String, contrasena:String): Boolean {
        return    llenarControles.fnValidarLogin(usuario,contrasena)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun fnIniciarSesion(){
        val usuario = txtUsuario.text?.toString()?.trim()
        val contrasena = txtContrasena.text?.toString()?.trim()

        if (usuario.isNullOrEmpty()) {
            showToast("Ingrese usuario")
            return
        }
        if (contrasena.isNullOrEmpty()) {
            showToast("Ingrese contraseña")
            return
        }

        if (fnValidarUsuario(usuario,contrasena)){
            CadenaHolder.us_login = usuario
           CadenaHolder.ep_codigo = llenarControles.fnObtenerCodigoUsuario(usuario)
            txtUsuario.setText("")
            txtContrasena.setText("")
            fnOcultarTeclado()
            (requireActivity() as? MainActivity)?.mostrarDashboard()
        }else{
            showToast("Usuario o cantraseña incorrectos")
        }
    }

    fun fnOcultarTeclado() {
        val inputMethodManager =
            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    object CadenaHolder {
        var ep_codigo: Int = 0
        var us_login: String = ""
    }



}
