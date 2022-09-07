package com.example.raise_developer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.graphqlsample.queries.GithubCommitQuery

class GrassPageFragment(githubDataArray: List<String>, githubData: List<GithubCommitQuery.Week>?, playTime: Int, position:Int): Fragment() {
    val position = position
    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var grassHarvestMoney = 0
    var githubDataArray = githubDataArray // grassPageActivity 에서 해당 페이지의 날짜 정보 배열을 가져옴 (22,08,12) 이런식으로
    var githubData = githubData //깃허브 정보
    var playTime = playTime
    var isThreadStop = false
    var grassMaxValue = arrayListOf<Int>()

    lateinit var fragmentToActivityGrassMoney: FragmentToActivityGrassMoney

    interface FragmentToActivityGrassMoney {
        fun onReceivedMoney(Money: Int)
    }

    // 해당 월의 날짜 정보 및 잔디 정보
    var numberOfDateArray = ArrayList<String>()
    var grassColorArray = ArrayList<String>()
    var contributionCountArray = ArrayList<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentToActivityGrassMoney = activity as FragmentToActivityGrassMoney
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.grass_page_view_pager_layout, container, false)
        gridLayoutSetting(view)
        pref = requireActivity().getSharedPreferences("fragmentPlayTime",0)
        editor = pref.edit()
        Log.d("grassPageFragment${position}","${playTime}")
        // 프리퍼런스 값이 있으면
        if(pref.getInt("fragment${position}",-1) != -1){
            Log.d("첫단계${position}","${pref.getInt("fragment${position}",0)}")
            playTime -= pref.getInt("fragment${position}",0)
        }
        val thread = Thread(PlayTime())
        thread.start()
        return view
    }

    fun divideGithubDataInfo(){ //깃허브 정보 구분하기
        for(index in 0 until githubData?.size!!){
            for (index1 in 0 until githubData?.get(index)?.contributionDays?.size!!){
                var grassColor = githubData?.get(index)?.contributionDays?.get(index1)?.color.toString() // 해당일의 잔디 색깔
                var date = githubData?.get(index)?.contributionDays?.get(index1)?.date.toString() // 해당일의 날짜
                var contributionCount = githubData?.get(index)?.contributionDays?.get(index1)?.contributionCount.toString()
                var dateArray = date.split("-")
                var tempYear = dateArray[0] // 해당일의 년도
                var tempMonth = dateArray[1] // 해당일의 월
                //grassPage에서 받은 날짜의 년도, 월을 비교해서 같은 정보만 배열에 저장
                if (tempYear == githubDataArray[0] && tempMonth == githubDataArray[1]){
                    numberOfDateArray.add(date)
                    grassColorArray.add(grassColor)
                    contributionCountArray.add(contributionCount)
                }
            }
        }
    }

    inner class PlayTime: Runnable {
        override fun run() {
//            Log.d("grassFragment${position}","${playTime}")
            while (!isThreadStop) {

                playTime+=1
                Thread.sleep(1000)
            }
        }
    }

    fun getDeviceDpi(): Int { // 현재 dpi 구하는 함수
        val density = resources.displayMetrics.density
        val result = when {
            density >= 4.0 -> 640 // "xxxhdpi"
            density >= 3.0 -> 480 // "xxhdpi"
            density >= 2.0 -> 320 // "xhdpi"
            density >= 1.5 -> 240 // "hdpi"
            density >= 1.0 -> 160 // "mdpi"
            else -> 120 // "ldpi"
        }
        return result
    }

    fun gridLayoutSetting(view: View) {
        divideGithubDataInfo() // 각각 배열의 size는 8월이면 31, 2월이면 28, 이렇게 저장되어 있을 것임
        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        for (index in 0 until numberOfDateArray.size) {

            var maxValue = 0
            val customView = layoutInflater.inflate(R.layout.grass_page_custom_view,gridLayout,false)
            val grassImage = customView.findViewById<ImageView>(R.id.grass)
            val coinImage = customView.findViewById<ImageView>(R.id.coin)
            if (numberOfDateArray.size >= 7) {
                val param = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED,1f),GridLayout.spec(GridLayout.UNDEFINED,1f))

                customView.layoutParams = param
            }

            ObjectAnimator.ofFloat(coinImage, "translationY", -15f).apply {
                duration = 800
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
            // 잔디 색깔에 따라 이미지를 씌움움
           if(grassColorArray[index] == "#9be9a8"){

               maxValue = 80
               grassMaxValue.add(maxValue)
               grassImage.setImageResource(R.mipmap.grass_one)
            }

            else if(grassColorArray[index] == "#40c463"){
               maxValue = 100
               grassMaxValue.add(maxValue)
               grassImage.setImageResource(R.mipmap.grass_two)
            }

            else if(grassColorArray[index] == "#30a14e"){
               maxValue = 120
               grassMaxValue.add(maxValue)
               grassImage.setImageResource(R.mipmap.grass_three)
            }

            else if(grassColorArray[index] == "#216e39"){
               maxValue = 140
               grassMaxValue.add(maxValue)
               grassImage.setImageResource(R.mipmap.grass_four)
            }

            else{
                maxValue = 0
                grassMaxValue.add(maxValue)
                grassImage.setImageResource(R.mipmap.empty_grass)

            }

            customView.setOnClickListener { // 클릭하면 잔디 상점 다이알로그 띄움
                val grassInfoDialog = GrassInfoDialog(numberOfDateArray[index], contributionCountArray[index], grassColorArray[index], playTime)
                grassInfoDialog.show(parentFragmentManager,"grassShopDialog")
            }
            gridLayout.addView(customView)
        }
        val harvestCoinButton = view.findViewById<Button>(R.id.harvestCoinButton)
        harvestCoinButton.setOnClickListener {
            for (index in 0 until numberOfDateArray.size){
                if (grassMaxValue[index] != 0){
                    if (playTime >= grassMaxValue[index]) {
                        grassHarvestMoney += grassMaxValue[index]
                        fragmentToActivityGrassMoney.onReceivedMoney(grassHarvestMoney)
                    }
                    else{
                        grassHarvestMoney += playTime
                        fragmentToActivityGrassMoney.onReceivedMoney(grassHarvestMoney)
                    }
                }
            }
            if (pref.getInt("fragment${position}",-1) == -1){
                editor.putInt("fragment${position}",playTime).apply()
                playTime = 0
            }
            else{
                val pastPreferenceValue = pref.getInt("fragment${position}",-1)
                editor.remove("fragment${position}").apply()
                editor.putInt("fragment${position}",pastPreferenceValue + playTime).apply()
                playTime = 0
            }
            for (index in 0 until gridLayout.childCount){
                val child = gridLayout.getChildAt(index)
                val coin = child.findViewById<ImageView>(R.id.coin)
                ObjectAnimator.ofFloat(coin,"translationY", -30f).apply{
                    duration = 1000
                    addListener(object: AnimatorListenerAdapter(){

                        override fun onAnimationEnd(animation: Animator?) {
                            ObjectAnimator.ofFloat(coin,"rotation",360f, 0f).apply{
                                duration = 500
                                addListener(object: AnimatorListenerAdapter(){
                                    override fun onAnimationEnd(animation: Animator?) {
                                        coin.visibility = View.INVISIBLE
                                    }
                                })
                                start()
                            }
                        }
                    })
                    start()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isThreadStop = true
    }
}