package com.cal.offlinemusicplayer.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cal.offlinemusicplayer.R
import com.cal.offlinemusicplayer.adapters.MusicAdapter
import com.cal.offlinemusicplayer.databinding.ActivityMainBinding
import com.cal.offlinemusicplayer.databinding.AddPlaylistDialogBinding
import com.cal.offlinemusicplayer.domains.Music
import com.cal.offlinemusicplayer.domains.MusicPlaylist
import com.cal.offlinemusicplayer.domains.exitApplication
import com.cal.offlinemusicplayer.domains.setDialogBtnBackground
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit  var musicList : ArrayList<Music>
        var themeIndex: Int = 0
        val currentTheme = arrayOf(R.style.coolPink, R.style.coolBlue, R.style.coolPurple, R.style.coolGreen, R.style.coolBlack)
        val currentThemeNav = arrayOf(R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav, R.style.coolGreenNav, R.style.coolBlackNav)
        val currentGradient = arrayOf(R.drawable.gradient_pink, R.drawable.gradient_blue, R.drawable.gradient_purple, R.drawable.gradient_green, R.drawable.gradient_black)
        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.SIZE + " DESC")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentThemeNav[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // setSupportActionBar(binding.toolbar)

        if(requestRunTimePermission()){
            initializeUI()

            FavoriteActivity.favList = ArrayList()
            // For Retrieving Favorite Songs
            val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE)
            val jsonString = editor.getString("FavoriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
            if(jsonString != null){
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                FavoriteActivity.favList.addAll(data)
            }

            // For Retrieving Playlist Songs
            PlayListActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("PlaylistSongs", null)
            if(jsonStringPlaylist != null){
                val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
                PlayListActivity.musicPlaylist = dataPlaylist
            }


        }


        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, FavoriteActivity::class.java))
        }

        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlayListActivity::class.java))
        }

        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navSettings -> startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navExit -> {
                    val customDialog = LayoutInflater.from(this@MainActivity).inflate(R.layout.add_playlist_dialog, binding.root, false)
                    val binder = AddPlaylistDialogBinding.bind(customDialog)
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you really want to exit?")
                        .setPositiveButton("Yes"){_,_ ->
                           exitApplication()
                        }
                        .setNegativeButton("No"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val alertDialog = builder.create()
                    alertDialog.show()
                    setDialogBtnBackground(this, alertDialog)
                }
            }
            true
        }


    }

    @SuppressLint("Range")
    private fun getStorageAudios() : ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " !=0 "
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortingList[sortOrder] , null)

        if(cursor!=null){
            if(cursor.moveToFirst()){
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()

                    val music = Music(id = idC, title = titleC, artist = artistC, album = albumC, path = pathC, duration = durationC, artUri = artUriC)
                    val file = File(music.path)

                    if(file.exists())
                        tempList.add(music)

                }while (cursor.moveToNext())

                cursor.close()
            }
        }
        return tempList
    }

    private fun initializeUI() {

        // Drawer initialization
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


       /* val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)*/
        // findViewById<LinearLayout>(R.id.linearLayoutNav)?.setBackgroundColor(currentGradient[themeIndex])
        // list of songs
        musicList = getStorageAudios()

        // Recycler View initialization
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, musicList)
        binding.musicRV.adapter = musicAdapter

        binding.totalSongs.text = "Total Songs : ${musicAdapter.itemCount}"


    }

    private fun requestRunTimePermission() : Boolean {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                initializeUI()
            }
            else
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
           exitApplication()
        }
    }

    override fun onResume() {
        super.onResume()
        // For Storing Favorite Songs
        val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavoriteActivity.favList)
        editor.putString("FavoriteSongs", jsonString)
        // For Storing Playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlayListActivity.musicPlaylist)
        editor.putString("PlaylistSongs", jsonStringPlaylist)
        editor.apply()
        // For Storing Playlist
        /*val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if(sortOrder != sortValue){
            sortOrder = sortValue
            musicList = getStorageAudios()
            musicAdapter.updateMusicList(musicList)*/
        }


    }


    // Search not working
  /*  override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.cal.offlinemusicplayer.R.menu.search_view_menu, menu)
        val searchView = menu.findItem(com.cal.offlinemusicplayer.R.id.searchView).actionView as SearchView?
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Toast.makeText(this@MainActivity, newText, Toast.LENGTH_SHORT).show()
                return true
            }
        })
        return true
    }*/

