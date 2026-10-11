package ca.gbc.comp3074.scavengerhuntgroup95project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)
        setupBackToolbar(R.string.title_team)
    }
}