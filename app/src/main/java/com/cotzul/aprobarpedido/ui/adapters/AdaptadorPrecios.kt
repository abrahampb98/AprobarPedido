package com.cotzul.aprobarpedido.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.cotzul.aprobarpedido.R


class AdaptadorPrecio(
    private val datos: MutableList<Precios>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdaptadorPrecio.ViewHolder>() {

    private var selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detalle_precios, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.bind(item)
        // Determinar si el elemento actual está seleccionado
        val isSelected = selectedPositions.contains(position)

        // Actualiza el estado de selección de la vista
        holder.itemView.isSelected = isSelected

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }

        holder.btnVerMasP.setOnClickListener {
            itemClickListener.btnVerMas(item)
        }

    }

    override fun getItemCount() = datos.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtReferenciaP: TextView = view.findViewById(R.id.txtReferenciaP)
        val btnVerMasP: ImageButton = view.findViewById(R.id.btnVerMasP)


        fun bind(item: Precios) {
            txtReferenciaP.text = item.referencia
        }
    }


    interface OnItemClickListener {
        fun onItemClick(item: Precios)
        fun btnVerMas(item: Precios)
    }

    fun clearItems() {
        datos.clear()  // Limpia la lista de datos
        selectedPositions.clear()  // Limpia las selecciones
        notifyDataSetChanged()  // Notifica al adaptador del cambio
    }
}

data class Precios(
    val referencia: String,
    val stock: Double,
    val precioSub: Double,
    val precioCont: Double,
    val precioCred: Double,
    val codigo: String,
    val descripcion: String,
    val unidadCE: Double,
    val costoProm: Double,
    val pv_precio7: Double
)


