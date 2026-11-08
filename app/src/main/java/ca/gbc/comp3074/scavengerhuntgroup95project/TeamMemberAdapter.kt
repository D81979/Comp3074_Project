package ca.gbc.comp3074.scavengerhuntgroup95project

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhuntgroup95project.data.TeamMember

class TeamMemberAdapter(
    private val onEmailClick: (TeamMember) -> Unit,
    private val onSmsClick: (TeamMember) -> Unit,
    private val onCallClick: (TeamMember) -> Unit,
    private val onEditClick: (TeamMember) -> Unit
) : RecyclerView.Adapter<TeamMemberAdapter.MemberViewHolder>() {

    private var members = emptyList<TeamMember>()

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name = itemView.findViewById<TextView>(R.id.txtName)
        val email = itemView.findViewById<TextView>(R.id.txtEmail)
        val phone = itemView.findViewById<TextView>(R.id.txtPhone)
        val btnEmail = itemView.findViewById<Button>(R.id.btnEmail)
        val btnSms = itemView.findViewById<Button>(R.id.btnSms)
        val btnCall = itemView.findViewById<Button>(R.id.btnCall)
        val btnEdit = itemView.findViewById<Button>(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_team,
                parent,
                false
            )
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]

        holder.name.text = member.name
        holder.email.text = member.email
        holder.phone.text = member.phone

        holder.btnEmail.setOnClickListener { onEmailClick(member) }
        holder.btnSms.setOnClickListener { onSmsClick(member) }
        holder.btnCall.setOnClickListener { onCallClick(member) }
        holder.btnEdit.setOnClickListener { onEditClick(member) }
    }

    override fun getItemCount() = members.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<TeamMember>) {
        members = list
        notifyDataSetChanged()
    }
}