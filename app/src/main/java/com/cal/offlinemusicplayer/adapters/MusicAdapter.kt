package com.cal.offlinemusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.PlayListActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.activities.PlaylistDetailActivity
import com.cal.offlinemusicplayer.activities.PlaylistDetailActivity.Companion.cuurentPlaylistPos
import com.cal.offlinemusicplayer.databinding.PlaylistItemBinding
import com.cal.offlinemusicplayer.domains.Music
import com.cal.offlinemusicplayer.domains.formatDuration

class MusicAdapter(private val context: Context, private var musicList: ArrayList<Music>, private val playlistDetails : Boolean = false, private val selectionActivity : Boolean = false) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    class ViewHolder(binding: PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.ViewHolder {
        return ViewHolder(PlaylistItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    private fun addSong(song: Music): Boolean {
        PlayListActivity.musicPlaylist.ref[PlaylistDetailActivity.cuurentPlaylistPos].playlist.forEachIndexed { index, music ->
            if(song.id == music.id){
                PlayListActivity.musicPlaylist.ref[PlaylistDetailActivity.cuurentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlayListActivity.musicPlaylist.ref[PlaylistDetailActivity.cuurentPlaylistPos].playlist.add(song)
        return true

    }

    /*fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }*/

    fun refreshSongs(){
        musicList = ArrayList()
        musicList = PlayListActivity.musicPlaylist.ref[cuurentPlaylistPos].playlist
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: MusicAdapter.ViewHolder, position: Int) {

        holder.name.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        Glide.with(context).load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop()
            .into(holder.image)

        when{
            playlistDetails -> {
                holder.root.setOnClickListener{

                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("index", position)
                    intent.putExtra("class", "PlaylistDetailAdapter")
                    ContextCompat.startActivity(context,  intent,null)
                }
            }

            selectionActivity ->{
                holder.root.setOnClickListener {
                    if(addSong(musicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.my_yellow))
                    else
                       holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
            }

            else ->
            {
                holder.root.setOnClickListener {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("index", position)
                    intent.putExtra("class", "MusicAdapter")
                    ContextCompat.startActivity(context,  intent,null)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}