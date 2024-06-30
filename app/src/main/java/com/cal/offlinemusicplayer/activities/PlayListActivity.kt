package com.cal.offlinemusicplayer.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentTheme
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.adapters.PlaylistAdapter
import com.cal.offlinemusicplayer.databinding.ActivityPlayListBinding
import com.cal.offlinemusicplayer.databinding.AddPlaylistDialogBinding
import com.cal.offlinemusicplayer.domains.MusicPlaylist
import com.cal.offlinemusicplayer.domains.Playlist
import com.cal.offlinemusicplayer.domains.setDialogBtnBackground
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlayListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayListBinding
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(currentTheme[themeIndex])
        binding = ActivityPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(musicPlaylist.ref.isNotEmpty()) binding.instructionPA.visibility = View.GONE

        // Back Button
        binding.backBtnPLA.setOnClickListener {
            finish()
        }

        // Add PlayList Button
        binding.addPlaylistBtn.setOnClickListener {
            customAlertDialog()
        }



        // Recycler View initialization
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this,2)
        playlistAdapter = PlaylistAdapter(this@PlayListActivity,  musicPlaylist.ref)
        binding.playlistRV.adapter = playlistAdapter

    }

    private fun customAlertDialog() {
        val customDialog = LayoutInflater.from(this@PlayListActivity).inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
            .setView(customDialog)
            .setTitle("Add New PlayList")
            .setPositiveButton("Add") { dialog, _ ->
                // Create Playlist
                val playlistName = binder.playlistName.text.toString()
                val playlistDes = binder.yourDescription.text.toString()
                if (playlistName.isNotEmpty() && playlistDes.isNotEmpty()) {
                    addPlaylist(playlistName, playlistDes)
                }
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
        setDialogBtnBackground(this, alertDialog)
    }

    private fun addPlaylist(playlistName: String, playlistDes: String) {
        var playlistExists = false
        for(i in musicPlaylist.ref){
            if(playlistName.equals(i.name)) {
                playlistExists = true
                break
            }
        }

        if(playlistExists) Toast.makeText(this, "Playlist Exists!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = playlistName
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createBy = playlistDes
            val calendar = Calendar.getInstance().time
            val sdf  = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH )
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }

    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }


}