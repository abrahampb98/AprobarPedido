package com.cotzul.aprobarpedido.ui.fragment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.cotzul.aprobarpedido.R
import com.cotzul.aprobarpedido.network.SolicitudSoap
import com.cotzul.aprobarpedido.utils.cls.ClsLLenarControles
import com.cotzul.aprobarpedido.utils.cls.Cupo
import com.cotzul.aprobarpedido.utils.cls.DetallePedido
import com.cotzul.aprobarpedido.utils.cls.Factura
import com.cotzul.aprobarpedido.utils.cls.Observacion
import com.cotzul.aprobarpedido.utils.parser.XmlAprobarPedido
import com.google.android.material.button.MaterialButton

class EditarPedidoFragment : Fragment(R.layout.fragment_editar_pedido) {

    private lateinit var llenarControles: ClsLLenarControles
    private lateinit var txtClienteEP: TextView
    private lateinit var txtDocumEP: TextView
    private lateinit var txtVendedorEP: TextView
    private lateinit var txtTotalEP: TextView
    private lateinit var txtClienteC: TextView
    private lateinit var txtUbicacionC: TextView
    private lateinit var txtTotalCupoC: TextView
    private lateinit var txtCupoDispC: TextView
    private lateinit var lblCupo: TextView
    private lateinit var lblObservaciones: TextView
    private var documento: Int = 0
    private var cl_codigo: Int = 0
    private var us_login: String = ""
    private var ep_codigo: Int = 0
    lateinit var solicitudSoap: SolicitudSoap
    private lateinit var ClaseXmlPedido: XmlAprobarPedido
    private lateinit var txtOrdenGN: TextView
    private lateinit var txtVentasGN: TextView
    private lateinit var txtCostosGN: TextView
    private lateinit var txtOrdenCP: TextView
    private lateinit var txtVentasCP: TextView
    private lateinit var txtCostosCP: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        documento = arguments?.getInt("documento") ?: 0
        us_login = arguments?.getString("usuario") ?: ""
        ep_codigo = arguments?.getInt("codigo") ?: 0
        llenarControles = ClsLLenarControles(requireContext())

        txtClienteEP = view.findViewById(R.id.txtClienteEP)
        txtDocumEP = view.findViewById(R.id.txtDocumEP)
        txtVendedorEP = view.findViewById(R.id.txtVendedorEP)
        txtTotalEP = view.findViewById(R.id.txtTotalEP)
        txtClienteC = view.findViewById(R.id.txtClienteC)
        txtUbicacionC = view.findViewById(R.id.txtUbicacionC)
        txtTotalCupoC = view.findViewById(R.id.txtTotalCupoC)
        txtCupoDispC = view.findViewById(R.id.txtCupoDispC)
        lblCupo = view.findViewById(R.id.lblCupo)
        lblObservaciones = view.findViewById(R.id.lblObservaciones)
        txtOrdenGN = view.findViewById(R.id.txtOrdenGN)
        txtVentasGN = view.findViewById(R.id.txtVentasGN)
        txtCostosGN = view.findViewById(R.id.txtCostosGN)
        txtOrdenCP = view.findViewById(R.id.txtOrdenCP)
        txtVentasCP = view.findViewById(R.id.txtVentasCP)
        txtCostosCP = view.findViewById(R.id.txtCostosCP)

        val btnRechazar = view.findViewById<MaterialButton>(R.id.btnRechazar)
        val btnAprobar = view.findViewById<MaterialButton>(R.id.btnAprobar)
        val btnSaldos = view.findViewById<MaterialButton>(R.id.btnDetalleSaldo)

        fnLLenarCabecera()
        val listaFacturas = llenarControles.fnCargarFacturas()
        fnMostrarFacturas(view, listaFacturas)
        val cupoCliente = llenarControles.fnCupoCliente(documento)
        fnMostrarCupo(view, cupoCliente)
        val observacion = llenarControles.fnCargarObservaciones()
        fnMostrarObservaciones(view,observacion)
        val detallesPedidos = llenarControles.fnCargarDetallesPedido(documento)
        fnMostrarDetallesPedido(view,detallesPedidos)
        fnRentabilidades()

        btnSaldos.setOnClickListener {
            fnMostrarDialogoSaldos()
        }

        btnRechazar.setOnClickListener {
                fnMostrarDialogoObservacion(requireContext()) { observacion ->
                    fnAprobarRechazarPedido("D",observacion)
                }
        }

        btnAprobar.setOnClickListener {
            fnAprobarRechazarPedido("P","")
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) { // true = activa el bloqueo
                override fun handleOnBackPressed() {
                    // No haces nada aquí => botón atrás bloqueado
                    Toast.makeText(requireContext(), "Retroceso desactivado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun fnLLenarCabecera() {
        val pedidos = llenarControles.fnCargarEdicionPedido(documento)
        if (pedidos.isNotEmpty()) {
            val item = pedidos[0]
            cl_codigo = item.Codigo
            txtClienteEP.text = item.Cliente
            txtDocumEP.text = item.Documento.toString()
            txtVendedorEP.text = item.Vendedor
            txtTotalEP.text = String.format("%.2f", item.Valor)
            txtClienteC.text = item.Cliente
            txtUbicacionC.text = item.Ubicacion
            txtTotalCupoC.text = item.Cupo.toString()
            txtCupoDispC.text = item.CupoD.toString()
        }
    }

    private fun fnMostrarFacturas(root: View, listaFacturas: List<Factura>) {
        val contenedor = root.findViewById<LinearLayout>(R.id.contenedorFacturas)
        contenedor.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        for (factura in listaFacturas) {
            val facturaView = inflater.inflate(R.layout.fragment_facturas, contenedor, false)
            facturaView.findViewById<TextView>(R.id.SriC).text = "SRI: ${factura.sri}"
            facturaView.findViewById<TextView>(R.id.FacturaC).text = "FECHA : ${factura.fecha}"
            facturaView.findViewById<TextView>(R.id.txtVendedor).text = "VENDEDOR: ${factura.vendedor}"
            facturaView.findViewById<TextView>(R.id.txtTipoDoc).text = factura.tipoDoc
            facturaView.findViewById<TextView>(R.id.txtValorFact).text = "$ ${factura.valor}"
            facturaView.findViewById<TextView>(R.id.txtSaldoFact).text = "$ ${factura.saldo}"
            facturaView.findViewById<TextView>(R.id.txtDetCheque).text = factura.detCheque
            facturaView.findViewById<TextView>(R.id.txtDiasFactura).text = factura.dias

            val btnDetalles = facturaView.findViewById<Button>(R.id.btnDetalles)
            if (factura.botton) {
                btnDetalles.visibility = View.VISIBLE
            } else {
                btnDetalles.visibility = View.GONE
            }

            btnDetalles.setOnClickListener {
                // Aquí puedes abrir un fragmento, diálogo o actividad con los detalles
                //Toast.makeText(context, "Detalles de ${factura.sri}", Toast.LENGTH_SHORT).show()
                val soporte = llenarControles.fnCargarSoporteFacturas(factura.sri)
                fnMostrarDialogoSoporteFacturas(requireContext(), soporte, factura.valor)

            }

            contenedor.addView(facturaView)
        }
    }


    fun fnMostrarDialogoSoporteFacturas(context: Context, listaFacturas: List<Factura>, valorF: Double) {
        // 1️⃣ Crear el contenedor que irá dentro del AlertDialog
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_soporte_facturas, null)
        val contenedor = dialogView.findViewById<LinearLayout>(R.id.layoutContenedorFacturas)

        // 2️⃣ Llenar las facturas dentro del contenedor
        for (factura in listaFacturas) {
            val facturaView = inflater.inflate(R.layout.fragment_facturas, contenedor, false)

            facturaView.findViewById<TextView>(R.id.SriC).text = "SRI: ${factura.sri}"
            facturaView.findViewById<TextView>(R.id.FacturaC).text = "FECHA: ${factura.fecha}"
            facturaView.findViewById<TextView>(R.id.txtVendedor).text = "VENDEDOR: ${factura.vendedor}"
            facturaView.findViewById<TextView>(R.id.txtTipoDoc).text = factura.tipoDoc
            facturaView.findViewById<TextView>(R.id.txtValorFact).text = "$ ${valorF}"
            facturaView.findViewById<TextView>(R.id.txtSaldoFact).text = "$ ${factura.valor}"
            facturaView.findViewById<TextView>(R.id.txtDetCheque).text = factura.detCheque
            facturaView.findViewById<TextView>(R.id.txtDiasFactura).text = factura.dias
            facturaView.findViewById<TextView>(R.id.lblTipo).text = "TIPO DOCUMENTO"
            facturaView.findViewById<TextView>(R.id.lblValorFactura).text = "VALOR  FACTURA"
            facturaView.findViewById<TextView>(R.id.lblValorDoc).text = "VALOR DOCUMENTO"
            facturaView.findViewById<TextView>(R.id.lblCheque).text = "DET.  CHEQUE"

            val btnDetalles = facturaView.findViewById<Button>(R.id.btnDetalles)
            btnDetalles.visibility = if (factura.botton) View.VISIBLE else View.GONE

            btnDetalles.setOnClickListener {
                Toast.makeText(context, "Detalles de ${factura.sri}", Toast.LENGTH_SHORT).show()
            }

            contenedor.addView(facturaView)
        }

        // 3️⃣ Crear el AlertDialog
        val dialog = AlertDialog.Builder(context)
            .setTitle("Soporte de Facturas")
            .setView(dialogView)
            .setPositiveButton("Cerrar") { dlg, _ -> dlg.dismiss() }
            .create()

        // 4️⃣ Mostrar el diálogo
        dialog.show()
    }


    private fun fnMostrarCupo(root: View, listaCupos: List<Cupo>) {
        // 1️⃣ Contenedor donde se agregan las tarjetas de cupo
        val contenedor = root.findViewById<LinearLayout>(R.id.contenedorCupo)
        contenedor.removeAllViews()

        // 2️⃣ Inflador de vistas
        val inflater = LayoutInflater.from(requireContext())

        // 3️⃣ Recorrer los registros
        for (cupo in listaCupos) {
            // Inflar una vista individual del cupo
            val cupoView = inflater.inflate(R.layout.fragment_cupo, contenedor, false)

            // 4️⃣ Llenar los valores en los campos correspondientes
            cupoView.findViewById<TextView>(R.id.txtCupoC).text = "$ %.2f".format(cupo.total)
            cupoView.findViewById<TextView>(R.id.txtCupoUtl).text = "$ %.2f".format(cupo.utilizado)
            cupoView.findViewById<TextView>(R.id.txtCupoDisponible).text = "$ %.2f".format(cupo.disponible)
            cupoView.findViewById<TextView>(R.id.txtPatrimonioC).text = "$ %.2f".format(cupo.patrimonio)
            cupoView.findViewById<TextView>(R.id.txtDescripcionC).text = cupo.descripcion
            cupoView.findViewById<TextView>(R.id.txtValorB).text = "$ %.2f".format(cupo.valorBase)
            cupoView.findViewById<TextView>(R.id.txtValorDesc).text = "$ %.2f".format(cupo.valorDescuento)
            cupoView.findViewById<TextView>(R.id.txtValorSeg).text = "$ %.2f".format(cupo.valorSeguro)
            cupoView.findViewById<TextView>(R.id.txtValorIva).text = "$ %.2f".format(cupo.valorIva)
            cupoView.findViewById<TextView>(R.id.txtValorFlete).text = "$ %.2f".format(cupo.valorFlete)
            cupoView.findViewById<TextView>(R.id.txtValorTot).text = "$ %.2f".format(cupo.valorTotal)
            lblCupo.text = cupo.observacion

            // 6️⃣ Agregar la vista al contenedor
            contenedor.addView(cupoView)
        }
    }

    private fun fnMostrarObservaciones(root: View, listaObservaciones: List<Observacion>) {
        val contenedor = root.findViewById<LinearLayout>(R.id.contenedorObservaciones)
        contenedor.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        for (obs in listaObservaciones) {
            val viewObs = inflater.inflate(R.layout.fragment_observaciones, contenedor, false)

            lblObservaciones.text = "Historial Observaciones"
            lblObservaciones.setPadding(6, 6, 6, 6)

            viewObs.findViewById<TextView>(R.id.txtSecD).text = "${obs.secuencia}"
            viewObs.findViewById<TextView>(R.id.txtObservacionD).text = obs.observacion
            viewObs.findViewById<TextView>(R.id.txtUsuarioD).text = "Usuario: ${obs.usuario}"
            viewObs.findViewById<TextView>(R.id.txtFechaD).text = "Fecha: ${obs.fecha}"

            contenedor.addView(viewObs)
        }
    }

    private fun fnMostrarDetallesPedido(root: View, listaDetalles: List<DetallePedido>) {
        // 1️⃣ Contenedor donde se agregan las tarjetas
        val contenedor = root.findViewById<LinearLayout>(R.id.contenedordetalles)
        contenedor.removeAllViews()

        // 2️⃣ Inflador de vistas
        val inflater = LayoutInflater.from(requireContext())

        // 3️⃣ Recorrer los registros
        for (detalle in listaDetalles) {
            // Inflar la vista individual de detalle (usa tu XML de arriba)
            val detalleView = inflater.inflate(R.layout.fragment_detalles, contenedor, false)

            // 4️⃣ Llenar los valores en los campos correspondientes
            detalleView.findViewById<TextView>(R.id.txtReferenciaD).text = detalle.descripcion
            detalleView.findViewById<TextView>(R.id.txtTituloD).text = detalle.titulo
            detalleView.findViewById<TextView>(R.id.txtMarcaD).text = detalle.marca
            detalleView.findViewById<TextView>(R.id.txtSubFamiliaD).text = detalle.subFamilia
            detalleView.findViewById<TextView>(R.id.txtCantidadD).text = "%.2f".format(detalle.cantidad)
            detalleView.findViewById<TextView>(R.id.txtVPrecioD).text = "$ %.3f".format(detalle.precioPedido)
            detalleView.findViewById<TextView>(R.id.txtPrecioUniD).text = "$ %.3f".format(detalle.precioUnitario)
            detalleView.findViewById<TextView>(R.id.txtCostoPromD).text = "$ %.3f".format(detalle.costoProm)
            detalleView.findViewById<TextView>(R.id.txtSubtotalD).text = "$ %.2f".format(detalle.subtotal)
            detalleView.findViewById<TextView>(R.id.txtTotalD).text = "$ %.2f".format(detalle.cantidad * detalle.precioUnitario)
            detalleView.findViewById<TextView>(R.id.txtMargenD).text = "%.2f".format(detalle.cantidad * detalle.precioUnitario / detalle.subtotal)


            // Obtén las referencias a los TextView
            val txtVPrecioD = detalleView.findViewById<TextView>(R.id.txtVPrecioD)
            val txtPrecioUniD = detalleView.findViewById<TextView>(R.id.txtPrecioUniD)

            // Limpia los textos eliminando $ y espacios
            val valor1 = txtVPrecioD.text.toString().replace("$", "").trim()
            val valor2 = txtPrecioUniD.text.toString().replace("$", "").trim()

            // Compara y aplica fondo si son diferentes
            if (valor1 != valor2) {
                txtVPrecioD.setBackgroundColor(Color.YELLOW)
                txtPrecioUniD.setBackgroundColor(Color.YELLOW)
            } else {
                txtVPrecioD.setBackgroundColor(Color.TRANSPARENT)
                txtPrecioUniD.setBackgroundColor(Color.TRANSPARENT)
            }


            // 5️⃣ CheckBox
            val chkAceptarPrecio = detalleView.findViewById<CheckBox>(R.id.chkAceptarPrecio)
            chkAceptarPrecio.isChecked = detalle.aceptarPrecio
            chkAceptarPrecio.setOnCheckedChangeListener { _, isChecked ->
                detalle.aceptarPrecio = isChecked

                // Referencias locales (solo una vez)
                val txtPrecio = detalleView.findViewById<TextView>(R.id.txtPrecioUniD)
                val txtTotal = detalleView.findViewById<TextView>(R.id.txtTotalD)
                val txtMargen = detalleView.findViewById<TextView>(R.id.txtMargenD)

                // Si se marca el check, usar precio del vendedor; si no, mantener el unitario actual
                val precioUsado = if (isChecked) detalle.precioPedido else detalle.precioUnitario

                // Actualizar precio unitario en pantalla
                txtPrecio.text = "$ %.3f".format(precioUsado)

                // Calcular total
                val total = detalle.cantidad * precioUsado
                txtTotal.text = "$ %.2f".format(total)

                // Calcular margen (seguro ante división por 0)
                val margen = if (detalle.costoProm > 0) total / (detalle.costoProm * detalle.cantidad) else 0.0
                txtMargen.text = "%.2f".format(margen)

                llenarControles.fnActualizarPrecioDetalle(detalle.codigo, precioUsado)
            }


            // 6️⃣ Botón "Cambiar"
            val btnCambiar = detalleView.findViewById<Button>(R.id.btnCambiarD)
            btnCambiar.setOnClickListener {
                val precio =  detalleView.findViewById<TextView>(R.id.txtPrecioUniD).text.toString()
                    .replace("$", "").trim().toDoubleOrNull() ?: 0.0

                fnMostrarDialogoCambiarPrecio(root.context, precio) { nuevoPrecio ->
                    detalleView.findViewById<TextView>(R.id.txtPrecioUniD).text = "$ %.2f".format(nuevoPrecio)
                    detalleView.findViewById<TextView>(R.id.txtTotalD).text = "$ %.2f".format(detalle.cantidad * nuevoPrecio)
                    detalleView.findViewById<TextView>(R.id.txtMargenD).text = "%.2f".format((detalle.cantidad * nuevoPrecio) / (detalle.costoProm * detalle.cantidad))
                    llenarControles.fnActualizarPrecioDetalle(detalle.codigo, nuevoPrecio)
                }
            }

            // 7️⃣ Agregar la vista al contenedor
            contenedor.addView(detalleView)
        }
    }

    private fun fnMostrarDialogoCambiarPrecio(
        context: Context,
        precio: Double,
        onPrecioCambiado: (Double) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cambiar_precio, null)
        val inputPrecio = dialogView.findViewById<EditText>(R.id.inputPrecio)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptar)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrar)

        inputPrecio.setText(precio.toString())

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        btnAceptar.setOnClickListener {
            val nuevoPrecio = inputPrecio.text.toString().toDoubleOrNull()
            if (nuevoPrecio != null) {
                onPrecioCambiado(nuevoPrecio)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Ingrese un valor válido", Toast.LENGTH_SHORT).show()
            }
        }

        btnCerrar.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun fnMostrarDialogoObservacion(context: Context, onGuardar: (String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rechazar_pedido, null)
        val inputObs = dialogView.findViewById<EditText>(R.id.inputObservacion)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarObs)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarObs)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        btnGuardar.setOnClickListener {
            val texto = inputObs.text.toString().trim()
            if (texto.isNotEmpty()) {
                onGuardar(texto)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Debe ingresar una observación", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    fun fnAprobarRechazarPedido(vlpEstado: String, vlpOpservacion: String){

        llenarControles.fnAuditoriaPedido(documento,us_login,vlpOpservacion)
        llenarControles.fnActualizarEstadoPedido(vlpEstado,documento)
        ClaseXmlPedido = XmlAprobarPedido(requireContext())
        solicitudSoap = SolicitudSoap(requireContext())
        val progressDialog = fnShowProgressDialog()
        MiAsyncTaskArprobarRechazarPedido(progressDialog).execute()

    }

    private fun fnShowProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Enviando Pedido...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }


    private inner class MiAsyncTaskArprobarRechazarPedido (private val progressDialog: ProgressDialog) :
        AsyncTask<Void, Void, String>() {

        private lateinit var database: SQLiteDatabase

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): String? {

            val cadena = ClaseXmlPedido.fnObtenerPedidosXML(ep_codigo,us_login)

            solicitudSoap.initializeVariables(getString(R.string.str_AprobarPedidos).toInt(), cadena)
            var pedido: String = ""
            val inputStream = solicitudSoap.realizarSolicitudSoap()
            val result = inputStream?.bufferedReader()?.use { it.readText() }
            return pedido
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            setFragmentResult("pedidoEditadoKey", bundleOf("actualizado" to true))


            parentFragmentManager.popBackStack()
            requireActivity().findViewById<View>(R.id.overlay_container).visibility = View.GONE
        }

    }


    fun fnMostrarDialogoSaldos() {
        val listaCxc = llenarControles.fnObtenerSaldos()
        val inflater = LayoutInflater.from(context)

        // Inflar el layout principal con una sola cabecera
        val dialogView = inflater.inflate(R.layout.dialog_saldos, null)
        val contenedor = dialogView.findViewById<LinearLayout>(R.id.layoutContenedorSaldos)

        // Agregar las filas dinámicamente
        for (item in listaCxc) {
            val itemView = inflater.inflate(R.layout.detalle_saldo, contenedor, false)

            itemView.findViewById<TextView>(R.id.txtSecS).text = item.cc_sec.toString()
            itemView.findViewById<TextView>(R.id.txtDescripcionS).text = item.cc_descripcion
            itemView.findViewById<TextView>(R.id.txtSaldoS).text =
                "$ ${String.format("%.2f", item.cl_saldo)}"

            contenedor.addView(itemView)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("Detalle Saldos")
            .setView(dialogView)
            .setPositiveButton("Cerrar") { dlg, _ -> dlg.dismiss() }
            .create()
        dialog.show()
    }



    fun fnRentabilidades() {
        val datos = llenarControles.fnObtenerRentabilidades(documento)

        for (rentabilidades in datos) {

            // Rentabilidad bruta y costo promedio
            txtOrdenGN.setText(
                "%.2f".format(
                    rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal - rentabilidades.dp_subtotalcn
                )
            )
            txtOrdenCP.setText(
                "%.2f".format(
                    rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal - rentabilidades.dp_subtotalcp
                )
            )

            // Rentabilidad porcentual CN
            val rentabilidadCN = rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal - rentabilidades.dp_subtotalcn
            val divisorCN =
                if ((rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal) == 0.0)
                    1.0 else (rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal)

            val porcentajeCN = (rentabilidadCN / divisorCN) * 100
            txtVentasGN.setText("%.2f".format(porcentajeCN))

            // Rentabilidad porcentual CP
            val rentabilidadCP = rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal - rentabilidades.dp_subtotalcp
            val divisorCP =
                if ((rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal) == 0.0)
                    1.0 else (rentabilidades.dp_subtotalReal - rentabilidades.descuentoReal)

            val porcentajeCP = (rentabilidadCP / divisorCP) * 100
            txtVentasCP.setText("%.2f".format(porcentajeCP))

            // Costo general relativo (evita división por 0)
            val subtotalReal = rentabilidades.dp_subtotalReal
            val descuentoReal = rentabilidades.descuentoReal
            val rentabilidadGeneral = rentabilidades.dp_subtotalcn
            val divisorGeneral = if (rentabilidadGeneral == 0.0) 1.0 else rentabilidadGeneral

            val resultado = ((subtotalReal - descuentoReal) / divisorGeneral)
            txtCostosGN.setText("%.2f".format(resultado))

            // Costo general relativo (evita división por 0)
            val rentabilidadGeneralCP = rentabilidades.dp_subtotalcp
            val divisorGeneralCP = if (rentabilidadGeneralCP == 0.0) 1.0 else rentabilidadGeneralCP

            val resultadoCP = ((subtotalReal - descuentoReal) / divisorGeneralCP)
            txtCostosCP.setText("%.2f".format(resultadoCP))
        }
    }




}
