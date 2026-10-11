package ca.gbc.comp3074.scavengerhuntgroup95project

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setupBackToolbar(titleRes: Int) {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(titleRes)
    toolbar.setNavigationOnClickListener { finish() }
}