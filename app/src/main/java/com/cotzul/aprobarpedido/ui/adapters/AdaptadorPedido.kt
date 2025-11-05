package com.cotzul.aprobarpedido.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cotzul.aprobarpedido.R


class AdaptadorPedido(
    private val datos: MutableList<Pedidos>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorPedido.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_pedidos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)
        //println("Elemento en la posición $position seleccionado: ${selectedPositions.contains(position)}")

        // Determinar si el elemento actual está seleccionado
        val isSelected = selectedPositions.contains(position)

        // Actualiza el estado de selección de la vista
        holder.itemView.isSelected = isSelected

        // Configura el OnClickListener para manejar los clics en los elementos
        holder.itemView.setOnClickListener {
            // Eliminar todas las selecciones anteriores
            selectedPositions.clear()
            // Marcar el elemento actual como seleccionado
            selectedPositions.add(position)
            // Notificar al adaptador de los cambios en la selección
            notifyDataSetChanged()
            itemClickListener.onItemClick(item) // Llama al método onItemClick del itemClickListener
        }

    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvDocumento: TextView = view.findViewById(R.id.tvDocumento)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        //val tvVendedor: TextView = view.findViewById(R.id.tvVendedor)
        val tvValor: TextView = view.findViewById(R.id.tvValor)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)



        fun bind(item: Pedidos) {
            tvDocumento.text = item.Documento.toString()
            tvCliente.text = item.Cliente
            //tvVendedor.text = item.Cliente
            tvValor.text = "$ ${item.Valor}"
            tvEstado.text = item.Estado
        }
    }


    interface OnItemClickListener {
        fun onItemClick(item: Pedidos)
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }
}

data class Pedidos(
    val Documento: Int,
    val Codigo: Int,
    val Cliente: String,
    val Vendedor: String,
    val Estado: String,
    val Valor: Double,
    val Ubicacion: String,
    val Cupo: Double,
    val CupoD: Double
)


