package com.cal.offlinemusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.databinding.FavoriteViewBinding
import com.cal.offlinemusicplayer.domains.Music

class FavoriteAdapter(private val context: Context, private val musicList: ArrayList<Music>) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    class ViewHolder(binding: FavoriteViewBinding) : RecyclerView.ViewHolder(binding.root) {

        val image = binding.songImgFV
        val name = binding.songNameFV
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAdapter.ViewHolder {
        return ViewHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: FavoriteAdapter.ViewHolder, position: Int) {
        holder.name.text = musicList[position].title
        Glide.with(context).load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop().into(holder.image)

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavoriteAdapter")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}