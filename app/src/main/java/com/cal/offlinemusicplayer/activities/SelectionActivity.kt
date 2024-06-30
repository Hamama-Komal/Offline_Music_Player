package com.cal.offlinemusicplayer.activities

import android.os.Bundle
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentTheme
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.activities.PlaylistDetailActivity.Companion.cuurentPlaylistPos
import com.cal.offlinemusicplayer.adapters.MusicAdapter
import com.cal.offlinemusicplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(currentTheme[themeIndex])
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back Button
        binding.backBtnSA.setOnClickListener {
            finish()
        }

        // Recycler View
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.musicList, selectionActivity = true)
        binding.selectionRV.adapter = adapter

        // Search View
     /*   binding.searchViewSA.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicListSearch = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in MainActivity.MusicListMA)
                        if(song.title.lowercase().contains(userInput))
                            MainActivity.musicListSearch.add(song)
                    MainActivity.search = true
                    adapter.updateMusicList(searchList = MainActivity.musicListSearch)
                }
                return true
            }
        })*/

    }
}