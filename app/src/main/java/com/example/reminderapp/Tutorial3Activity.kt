package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.reminderapp.databinding.ActivityAddBinding
import com.example.reminderapp.databinding.ActivityTutorial3Binding

class Tutorial3Activity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorial3Binding
    private lateinit var btn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorial3Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        btn = binding.btnAdd
        btn.setOnClickListener {
            startActivity(
                    Intent(applicationContext, LoginActivity::class.java))
            finish()
        }

    }
}