package ca.gbc.comp3074.scavengerhuntgroup95project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PointAdapter(
    private var items: List<Point>,
    private val onClick: (Point) -> Unit,
    private val onEdit: (Point) -> Unit,
    private val onDelete: (Point) -> Unit
) : RecyclerView.Adapter<PointAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val address: TextView = view.findViewById(R.id.txtAddress)
        val tags: TextView = view.findViewById(R.id.txtTags)
        val rating: RatingBar = view.findViewById(R.id.ratingBar)
        val done: CheckBox = view.findViewById(R.id.chkDone)
        val edit: TextView = view.findViewById(R.id.txtEdit)
        val delete: TextView = view.findViewById(R.id.txtDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val point = items[position]
        holder.name.text = point.name
        holder.address.text = point.address
        holder.tags.text = point.tags.joinToString("  |  ")
        holder.rating.rating = point.rating.toFloat()
        holder.done.isChecked = point.done
        holder.itemView.setOnClickListener { onClick(point) }
        holder.edit.setOnClickListener { onEdit(point) }
        holder.delete.setOnClickListener { onDelete(point) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Point>) {
        items = newItems
        notifyDataSetChanged()
    }
}