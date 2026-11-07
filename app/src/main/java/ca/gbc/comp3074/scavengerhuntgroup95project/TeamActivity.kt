package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDao
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.TeamMember
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class TeamActivity : AppCompatActivity() {

    private lateinit var adapter: TeamMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        // Toolbar

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        // Recycler View and Buttons

        val huntDao = HuntDatabase.getDatabase(this).huntDao()

        adapter = TeamMemberAdapter(
            onEmailClick = ::email,
            onSmsClick = ::smsMember,
            onCallClick = ::callMember
        )

        val recyclerView =
            findViewById<RecyclerView>(R.id.rvTeam)

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        // Update view
        lifecycleScope.launch {
            huntDao.observeMembers().collect { members ->
                adapter.submitList(members)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun email(member: TeamMember) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:${member.email}".toUri()
        }
        startActivity(intent)
    }

    private fun smsMember(member: TeamMember) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:${member.phone}".toUri()
        }

        startActivity(intent)
    }

    private fun callMember(member: TeamMember) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:${member.phone}".toUri()
        }

        startActivity(intent)
    }
}