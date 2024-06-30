package com.cal.offlinemusicplayer.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentThemeNav
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(currentThemeNav[themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.aboutText.text = aboutText()

    }

    private fun aboutText(): String{
        return "Developed By: Hamama Komal" +
                "\n\nIf you want to provide feedback, I will love to hear that."
    }
}