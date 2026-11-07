package ca.gbc.comp3074.scavengerhuntgroup95project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhuntgroup95project.data.PointWithTags

class PointAdapter(
    private var items: List<PointWithTags>,
    private val onClick: (PointWithTags) -> Unit,
    private val onEdit: (PointWithTags) -> Unit,
    private val onDelete: (PointWithTags) -> Unit
) : RecyclerView.Adapter<PointAdapter.PointViewHolder>() {

    class PointViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val txtName: TextView =
            view.findViewById(R.id.txtName)

        val txtAddress: TextView =
            view.findViewById(R.id.txtAddress)

        val txtTags: TextView =
            view.findViewById(R.id.txtTags)

        val ratingBar: RatingBar =
            view.findViewById(R.id.ratingBar)

        val chkDone: CheckBox =
            view.findViewById(R.id.chkDone)

        val txtEdit: TextView =
            view.findViewById(R.id.txtEdit)

        val txtDelete: TextView =
            view.findViewById(R.id.txtDelete)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_point,
                parent,
                false
            )

        return PointViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PointViewHolder,
        position: Int
    ) {
        val item = items[position]
        val point = item.point

        holder.txtName.text = point.name
        holder.txtAddress.text = point.address

        holder.txtTags.text = item.tags.joinToString(
            separator = "  |  "
        ) { tag ->
            tag.name
        }

        // Show saved values on the list.
        holder.ratingBar.rating =
            (point.rating ?: 0).toFloat()

        holder.ratingBar.setIsIndicator(true)
        holder.ratingBar.isFocusable = false

        holder.chkDone.setOnCheckedChangeListener(null)
        holder.chkDone.isChecked = point.isCompleted
        holder.chkDone.isClickable = false
        holder.chkDone.isFocusable = false

        // Open Details to change rating or completion.
        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.txtEdit.setOnClickListener {
            onEdit(item)
        }

        holder.txtDelete.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateList(newItems: List<PointWithTags>) {
        items = newItems
        notifyDataSetChanged()
    }
}