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
import com.cal.offlinemusicplayer.activities.PlayListActivity.Companion.musicPlaylist
import com.cal.offlinemusicplayer.activities.PlaylistDetailActivity
import com.cal.offlinemusicplayer.activities.PlaylistDetailActivity.Companion.cuurentPlaylistPos
import com.cal.offlinemusicplayer.databinding.PlaylistViewBinding
import com.cal.offlinemusicplayer.domains.Music
import com.cal.offlinemusicplayer.domains.Playlist
import com.cal.offlinemusicplayer.domains.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(private val context: Context, private var playList: ArrayList<Playlist>) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {

        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
        val delete = binding.playlistDeleteBtn

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistAdapter.ViewHolder {
        return ViewHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistAdapter.ViewHolder, position: Int) {
        holder.name.text = playList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playList[position].name)
                .setMessage("Do you really want to delete this playlist?")
                .setPositiveButton("Yes"){dialog,_ ->
                    musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
        }
       /* Glide.with(context).load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop().into(holder.image)
*/
        holder.root.setOnClickListener {

            val intent = Intent(context, PlaylistDetailActivity::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }

        if(musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context).load(musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                .into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return playList.size
    }

    fun refreshPlaylist() {
        playList = ArrayList()
        playList.addAll(PlayListActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}