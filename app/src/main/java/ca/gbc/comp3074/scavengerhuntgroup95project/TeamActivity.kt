package ca.gbc.comp3074.scavengerhuntgroup95project

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.comp3074.scavengerhuntgroup95project.data.HuntDatabase
import ca.gbc.comp3074.scavengerhuntgroup95project.data.TeamMember
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TeamActivity : AppCompatActivity() {

    private lateinit var adapter: TeamMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        // Set up the title and back arrow.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setTitle(R.string.title_team)
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val huntDao = HuntDatabase.getDatabase(
            applicationContext
        ).huntDao()

        // Keep the existing team member adapter.
        adapter = TeamMemberAdapter(
            onEmailClick = ::emailMember,
            onSmsClick = ::smsMember,
            onCallClick = ::callMember,
            onEditClick = { member ->
                openMemberForm(member.memberId)
            }
        )

        findViewById<RecyclerView>(R.id.rvTeam).apply {
            layoutManager = LinearLayoutManager(this@TeamActivity)
            adapter = this@TeamActivity.adapter
        }

        // Add opens the same form without a member ID.
        findViewById<FloatingActionButton>(R.id.float_btn_add)
            .setOnClickListener {
                openMemberForm()
            }

        // Refresh the list when saved members change.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                huntDao.observeMembers().collect { members ->
                    adapter.submitList(members)
                }
            }
        }
    }

    private fun openMemberForm(memberId: Long = 0L) {
        val manager = supportFragmentManager

        // Avoid opening the dialog twice.
        if (
            manager.isStateSaved ||
            manager.findFragmentByTag(
                MemberFormDialogFragment.TAG
            ) != null
        ) {
            return
        }

        MemberFormDialogFragment.newInstance(memberId)
            .showNow(manager, MemberFormDialogFragment.TAG)
    }

    private fun emailMember(member: TeamMember) {
        if (member.email.isBlank()) {
            showMessage("This member has no email address")
            return
        }

        openContactApp(
            Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", member.email, null)
            )
        )
    }

    private fun smsMember(member: TeamMember) {
        if (member.phone.isBlank()) {
            showMessage("This member has no phone number")
            return
        }

        openContactApp(
            Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("smsto", member.phone, null)
            )
        )
    }

    private fun callMember(member: TeamMember) {
        if (member.phone.isBlank()) {
            showMessage("This member has no phone number")
            return
        }

        // Open the dialler without placing a call.
        openContactApp(
            Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", member.phone, null)
            )
        )
    }

    private fun openContactApp(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            showMessage("No compatible app is installed")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}