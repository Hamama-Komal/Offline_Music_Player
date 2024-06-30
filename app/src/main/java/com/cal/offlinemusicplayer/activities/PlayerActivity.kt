package com.cal.offlinemusicplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.currentTheme
import com.cal.offlinemusicplayer.activities.MainActivity.Companion.themeIndex
import com.cal.offlinemusicplayer.databinding.ActivityPlayerBinding
import com.cal.offlinemusicplayer.domains.Music
import com.cal.offlinemusicplayer.domains.MusicService
import com.cal.offlinemusicplayer.domains.exitApplication
import com.cal.offlinemusicplayer.domains.favouriteChecker
import com.cal.offlinemusicplayer.domains.formatDuration
import com.cal.offlinemusicplayer.domains.setDialogBtnBackground
import com.cal.offlinemusicplayer.domains.setSongPosition
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("DEPRECATION")
class PlayerActivity : AppCompatActivity(), ServiceConnection , OnCompletionListener{



    companion object {
        lateinit var playerList : ArrayList<Music>
        var songPosition : Int = 0
        var isPlaying : Boolean = false
        var musicService : MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat : Boolean = false
        var min15 : Boolean = false
        var min30 : Boolean = false
        var min60 : Boolean = false
        var nowPlayingId : String = ""
        var isFavorite : Boolean = false
        var fIndex : Int = -1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(currentTheme[themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*// For Starting Service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)*/

        if (intent.data?.scheme.contentEquals("content")){}
        else initializeLayout()

        // Play Pause Button
        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying) pauseMusic() else playMusic()
        }

        // Next Button
        binding.nextBtnPA.setOnClickListener {
            prevNextSong(increment = true)
        }

        // Previous Button
        binding.previousBtnPA.setOnClickListener {
             prevNextSong(increment = false)
        }

        // SeekBar
        binding.seekBarPA.setOnSeekBarChangeListener(object  : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?)  = Unit

        })

        // Repeat Button
        binding.repeatBtnPA.setOnClickListener {
            if(!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
                }
            else{
                repeat = false
                val typedValue = TypedValue()
                theme.resolveAttribute(R.attr.themeColor, typedValue, true)
                val color = typedValue.data
                binding.repeatBtnPA.setColorFilter(color)
            }
        }

        // Back Button
        binding.backBtnPA.setOnClickListener {
            finish()
        }

        // Equalizer Button
        binding.equalizerBtnPA.setOnClickListener{
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            }
            catch (e : Exception){
                Toast.makeText(this, "Sorry, this feature is not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Timer Button
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if(!timer) showBottomDialog()
            else{
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you really want to stop timer?")
                    .setPositiveButton("Yes"){_,_ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        val typedValue = TypedValue()
                        theme.resolveAttribute(R.attr.themeColor, typedValue, true)
                        val color = typedValue.data
                        binding.timerBtnPA.setColorFilter(color)
                    }
                    .setNegativeButton("No"){dialog, _ ->
                        dialog.dismiss()
                    }



                val customDialog = builder.create()
                customDialog.show()

                setDialogBtnBackground(this, customDialog)

            }
        }

        // Share Button
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(playerList[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File"))

        }

        // Favourite Button
        binding.favouriteBtnPA.setOnClickListener {
            if(isFavorite) {
                isFavorite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
                FavoriteActivity.favList.removeAt(fIndex)
            }
            else{
               isFavorite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
                FavoriteActivity.favList.add(playerList[songPosition])
            }
        }

    }

    private fun prevNextSong(increment: Boolean) {

        if(increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }

    }


    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()

    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {

            "MusicAdapter" -> {
                // For Starting Service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                playerList = ArrayList()
                playerList.addAll(MainActivity.musicList)
                setLayout()
            }

            "MainActivity" -> {
                // For Starting Service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                playerList = ArrayList()
                playerList.addAll(MainActivity.musicList)
                playerList.shuffle()
                setLayout()
            }

            "NowPlaying" -> {

                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                else binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
            }

            "FavoriteAdapter" -> {

                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                playerList = ArrayList()
                playerList.addAll(FavoriteActivity.favList)
                setLayout()

            }

            "FavoriteActivity" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                playerList = ArrayList()
                playerList.addAll(FavoriteActivity.favList)
                playerList.shuffle()

                setLayout()
            }

             "PlaylistDetailAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                playerList = ArrayList()
                playerList.addAll(PlayListActivity.musicPlaylist.ref[PlaylistDetailActivity.cuurentPlaylistPos].playlist)

                setLayout()
            }

            "PlaylistDetailActivity" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                playerList = ArrayList()
                playerList.addAll(PlayListActivity.musicPlaylist.ref[PlaylistDetailActivity.cuurentPlaylistPos].playlist)
                playerList.shuffle()

                setLayout()
            }


        }

    }

    private fun createMediaPlayer() {
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
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)

            // SeekBar
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

            nowPlayingId = playerList[songPosition].id

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions if necessary
        }
    }

    private fun setLayout() {

        fIndex = favouriteChecker(playerList[songPosition].id)
        Glide.with(this).load(playerList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .centerCrop()
            .into(binding.songImgPA)

        binding.songNamePA.text = playerList[songPosition].title

        if (repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
        if(min15 || min30 || min60) binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
        if(isFavorite) binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
        else binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder = service as MusicService.MyBinder
        musicService = myBinder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetUp()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
           // e.printStackTrace()
            return
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK){
            return
        }
    }

    private fun showBottomDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
            min15 = true
            Thread{
                Thread.sleep(15 *  60000)
                if(min15) exitApplication()
            }.start()

            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
            min30 = true
            Thread{
                Thread.sleep(30 *60000)
                if(min30) exitApplication()
            }.start()

            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)!!.setOnClickListener {
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.yellow))
            min60 = true
            Thread{
                Thread.sleep(60 * 60000)
                if(min60) exitApplication()
            }.start()

            dialog.dismiss()
        }
    }


    /*override fun onDestroy() {
        super.onDestroy()
        // Release the media player when the activity is destroyed to free up resources
        mediaPlayer?.release()
        mediaPlayer = null
    }*/
}