package com.cal.offlinemusicplayer.domains

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AlertDialog
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.activities.FavoriteActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.playerList
import com.cal.offlinemusicplayer.activities.PlayerActivity.Companion.songPosition
import com.google.android.material.color.MaterialColors
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


data class Music(val id: String, val title: String, val artist: String, val album: String, val duration: Long = 0, val path: String, val artUri : String)

// Class for Creating Playlists
class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createBy: String
    lateinit var createdOn: String
}

// To get Created Playlists
class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

// Creating a function for formatting
fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture

}


fun setSongPosition(increment: Boolean) {
    if(!PlayerActivity.repeat) {
        if (increment) {
            if (playerList.size - 1 == songPosition)
                songPosition = 0
            else
                ++songPosition
        } else {
            if (0 == songPosition)
                songPosition = playerList.size - 1
            else
                --songPosition
        }
    }
}


fun exitApplication() {
    if(PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)

}

fun favouriteChecker(id: String) : Int{

    PlayerActivity.isFavorite = false
    FavoriteActivity.favList.forEachIndexed{ index, music ->
        if(id == music.id){
            PlayerActivity.isFavorite = true
            return index
        }

    }
    return -1
}

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if(!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}

fun setDialogBtnBackground(context: Context, dialog: AlertDialog){
    //setting button text
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )

    //setting button background
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.BLUE)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.BLUE)
    )
}
