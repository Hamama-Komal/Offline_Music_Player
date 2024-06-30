package com.cal.offlinemusicplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentTheme
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.adapters.MusicAdapter
import com.cal.offlinemusicplayer.adapters.PlaylistAdapter
import com.cal.offlinemusicplayer.databinding.ActivityPlaylistDetailBinding
import com.cal.offlinemusicplayer.domains.MusicPlaylist
import com.cal.offlinemusicplayer.domains.checkPlaylist
import com.cal.offlinemusicplayer.domains.exitApplication
import com.cal.offlinemusicplayer.domains.setDialogBtnBackground
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlaylistDetailBinding
    lateinit var adapter: MusicAdapter

    companion object{
       var cuurentPlaylistPos : Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme[themeIndex])
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        cuurentPlaylistPos = intent.extras?.get("index") as Int
        adapter = MusicAdapter(this, PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist, true)
        PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist = checkPlaylist(PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist)
        /*try{
            PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist = checkPlaylist(PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist)
        } catch (e: Exception) {
            Toast.makeText(this@PlaylistDetailActivity, e.toString(), Toast.LENGTH_SHORT).show()
        }*/
        PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist = checkPlaylist(PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist)

        // Back Button
        binding.backBtnPD.setOnClickListener {
            finish()
        }

        // Shuffle Button
        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this@PlaylistDetailActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailActivity")
            startActivity(intent)
        }


        // Add Button
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this@PlaylistDetailActivity, SelectionActivity::class.java))
        }

        // Remove button
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Confirmation")
                .setMessage("Do you really want to delete all songs?")
                .setPositiveButton("Yes"){dialog,_ ->
                   PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist.clear()
                    adapter.refreshSongs()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            setDialogBtnBackground(this, customDialog)
        }

        // Recycler View
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        binding.playlistDetailsRV.adapter = adapter

    }

    override fun onResume() {
        super.onResume()

        binding.playlistNamePD.text = PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].name
        binding.moreInfoPD.text = "Total Songs : ${adapter.itemCount} Songs.\n" + "Description: ${PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].createBy}\n" + "Created On:\n ${PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].createdOn}"

        if(adapter.itemCount > 0){
            Glide.with(this).load(PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                .into(binding.playlistImgPD)
            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE).edit()
        // For Storing Playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlayListActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPlaylist)
        editor.apply()
    }
}