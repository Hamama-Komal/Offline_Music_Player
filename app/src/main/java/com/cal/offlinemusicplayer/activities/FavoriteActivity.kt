package com.cal.offlinemusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentTheme
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentThemeNav
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.adapters.FavoriteAdapter
import com.cal.offlinemusicplayer.databinding.ActivityFavoriteBinding
import com.cal.offlinemusicplayer.domains.Music
import com.cal.offlinemusicplayer.domains.checkPlaylist

class FavoriteActivity : AppCompatActivity() {

    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var binding: ActivityFavoriteBinding

    companion object {
        var favList: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(currentTheme[themeIndex])
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        favList = checkPlaylist(favList)

        // Back Button
        binding.backBtnFA.setOnClickListener {
            finish()
        }



        binding.favouriteRV.setHasFixedSize(true)
        binding.favouriteRV.setItemViewCacheSize(13)
        binding.favouriteRV.layoutManager = GridLayoutManager(this@FavoriteActivity, 3)
        favoriteAdapter = FavoriteAdapter(this@FavoriteActivity, favList)
        binding.favouriteRV.adapter = favoriteAdapter



        if(favList.size < 1)
        {
            binding.shuffleBtnFA.visibility = View.INVISIBLE
            binding.instructionFV.visibility = View.VISIBLE
        }

        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this@FavoriteActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavoriteActivity")
            startActivity(intent)
        }

    }
}