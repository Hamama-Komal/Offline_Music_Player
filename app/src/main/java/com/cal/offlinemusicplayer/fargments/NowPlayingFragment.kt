package com.cal.offlinemusicplayer.fargments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.playerList
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.songPosition
import com.cal.offlinemusicplayer.databinding.FragmentNowPlayingBinding
import com.cal.offlinemusicplayer.domains.setSongPosition


class NowPlayingFragment : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.songNameNP.isSelected = true
        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }

        binding.nextBtnNP.setOnClickListener {

            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()

            Glide.with(this).load(PlayerActivity.playerList[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo))
                .centerCrop()
                .into(NowPlayingFragment.binding.songImgNP)
            NowPlayingFragment.binding.songNameNP.text = playerList[songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            playMusic()
        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(),  intent,null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE

            Glide.with(this).load(PlayerActivity.playerList[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo))
                .centerCrop()
                .into(binding.songImgNP)
            binding.songNameNP.text = playerList[songPosition].title

            if(PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            else binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        }
    }

    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }


    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.nextBtnPA.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }
}