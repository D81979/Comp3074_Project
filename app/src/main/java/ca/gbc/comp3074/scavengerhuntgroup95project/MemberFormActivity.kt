package ca.gbc.comp3074.scavengerhuntgroup95project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MemberFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_form)
        setupBackToolbar(R.string.title_member_form)
    }
}