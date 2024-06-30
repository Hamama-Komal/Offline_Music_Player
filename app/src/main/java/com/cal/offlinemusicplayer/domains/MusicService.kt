package com.cal.offlinemusicplayer.domains

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.binding
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.isPlaying
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.musicService
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.nowPlayingId
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.playerList
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.songPosition
import com.cal.offlinemusicplayer.fargments.NowPlayingFragment
import java.util.logging.Handler


class MusicService : Service(), AudioManager.OnAudioFocusChangeListener{


    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager


    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("ForegroundServiceType")
    fun showNotification(playPauseBtn: Int) {

        val previousIntent = Intent(baseContext, NotificationRecevier::class.java).setAction(ApplicationClass.PREVIOUS)
        val prePendingIntent = PendingIntent.getBroadcast(baseContext, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationRecevier::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationRecevier::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationRecevier::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imgArt = getImgArt(PlayerActivity.playerList[PlayerActivity.songPosition].path)
        val image = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt,0, imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.logo)
        }

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayerActivity.playerList[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.playerList[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", prePendingIntent)
            .addAction(R.drawable.play_icon, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .build()


       // Log.d("MusicService", "showNotification: starting foreground service with notification")
        startForeground(13, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       // Log.d("MusicService", "onStartCommand: called")
        showNotification(R.drawable.pause_icon)
        return START_NOT_STICKY
    }

    fun createMediaPlayer() {
        try {
            // Stop and release the previous media player if it exists
            musicService!!.mediaPlayer?.stop()
            musicService!!.mediaPlayer?.release()
            musicService!!.mediaPlayer = null

            // Create a new media player instance
            musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(playerList[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)

            // SeekBar
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

            nowPlayingId = playerList[songPosition].id


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions if necessary
        }
    }

    fun seekBarSetUp(){
        runnable = Runnable {
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
            android.os.Handler(Looper.getMainLooper()).postDelayed(runnable, 200)

        }
        android.os.Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0){
            binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
            musicService!!.showNotification(R.drawable.play_icon)
            isPlaying = false
            musicService!!.mediaPlayer!!.pause()
        }
        else{
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            isPlaying = true
            musicService!!.mediaPlayer!!.start()
        }
    }

}