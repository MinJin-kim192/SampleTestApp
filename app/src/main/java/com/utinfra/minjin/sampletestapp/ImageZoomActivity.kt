package com.utinfra.minjin.sampletestapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.appcompat.app.AppCompatActivity
import com.utinfra.minjin.sampletestapp.databinding.ActivityImageZoomBinding

class ImageZoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageZoomBinding
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mScaleFactor: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        mScaleGestureDetector?.onTouchEvent(event)

        return super.onTouchEvent(event)
    }

    inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            // ScaleGestureDetector에서 factor를 받아 변수로 선언한 factor에 넣고
            mScaleFactor *= scaleGestureDetector.scaleFactor

            // 최대 10배, 최소 10배 줌 한계 설정
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f))

            // 이미지뷰 스케일에 적용
            binding.imageSample.setScaleX(mScaleFactor)
            binding.imageSample.setScaleY(mScaleFactor)

            return true
        }
    }


}