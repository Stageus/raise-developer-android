package com.example.raise_developer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.graphqlsample.queries.GithubCommitQuery

class MyService : Service() {
    var githubContributionData: List<GithubCommitQuery.Week>? = null
    var player: MediaPlayer? = null

    val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this,R.raw.music)
    }

    inner class LocalBinder : Binder() {
        fun getService(): MyService {
            return this@MyService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun musicStop(){ //음악 멈추기
        player?.release()
        player = null
    }

    fun musicStart(){ //음악 재생
        Log.d("음악","재생")
        if (player?.isPlaying == null){
            player = MediaPlayer.create(this,R.raw.music)
        }
        player?.isLooping = true
        player?.start()
    }

    override fun onDestroy() {  //서비스 종료
        super.onDestroy()
        Log.d("서비스 종ㅇ료","ㅠ")
        player?.release()
        player = null
    }

    fun githubInfoActivityToService(data: List<GithubCommitQuery.Week>?){
        githubContributionData = data
        Log.d("ActivityToService","${githubContributionData}")
    }

    fun githubInfoServiceToActivity() : List<GithubCommitQuery.Week>? {
        Log.d("ServiceToActivity","${githubContributionData}")
        return githubContributionData
    }


}