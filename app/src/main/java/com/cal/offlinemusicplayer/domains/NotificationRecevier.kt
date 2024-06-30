package com.cal.offlinemusicplayer.domains

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.binding
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.isFavorite
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.isPlaying
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.musicService
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.playerList
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.songPosition
import com.cal.offlinemusicplayer.fargments.NowPlayingFragment
import com.cal.offlinemusicplayer.fargments.NowPlayingFragment.Companion
import kotlin.system.exitProcess

class NotificationRecevier : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
       when(intent?.action) {
           ApplicationClass.PREVIOUS -> previousNextSong(increment = false, context = context!!)
           ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic()  else playMusic()
           ApplicationClass.NEXT -> previousNextSong(increment = true, context = context!!)
           ApplicationClass.EXIT -> {
               PlayerActivity.musicService!!.stopForeground(true)
               PlayerActivity.musicService!!.mediaPlayer!!.release()
               PlayerActivity.musicService = null
               exitProcess(1)
           }
       }

    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    }

    private fun previousNextSong(increment : Boolean, context: Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context).load(playerList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop()
            .into(binding.songImgPA)

        binding.songNamePA.text = playerList[songPosition].title

        Glide.with(context).load(PlayerActivity.playerList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop()
            .into(NowPlayingFragment.binding.songImgNP)
        NowPlayingFragment.binding.songNameNP.text = playerList[songPosition].title

        playMusic()

        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.playerList[songPosition].id)
        if(isFavorite) binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
        else binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
    }



}