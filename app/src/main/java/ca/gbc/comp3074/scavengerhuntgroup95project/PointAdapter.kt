package ca.gbc.comp3074.scavengerhuntgroup95project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PointAdapter(
    private val items: List<Point>,
    private val onClick: (Point) -> Unit
) : RecyclerView.Adapter<PointAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val address: TextView = view.findViewById(R.id.txtAddress)
        val tags: TextView = view.findViewById(R.id.txtTags)
        val rating: RatingBar = view.findViewById(R.id.ratingBar)
        val done: CheckBox = view.findViewById(R.id.chkDone)
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
    }

    override fun getItemCount() = items.size
}