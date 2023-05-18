package eu.mokrzycki.learndigits.ui.game

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton
import eu.mokrzycki.learndigits.R
import eu.mokrzycki.learndigits.databinding.FragmentGameBinding
import java.io.IOException
import kotlin.random.Random

class GameViewModel(
    private val assetManager: AssetManager,
    private val vibrator: Vibrator,
) : ViewModel() {

    private val growDuration = 400L
    private var selectedSoundDigit = 0
    private var selectedSoundWrong = 0
    private var selectedSoundSuccess = 0

    private fun setNullToButtonsOnClickListeners(fragmentGameBinding: FragmentGameBinding) {
        fragmentGameBinding.buttonsContainer.forEach { view ->
            if (view is MaterialButton) {
                view.setOnClickListener(null)
            }
        }
    }

    private fun hideLottiePlay(fragmentGameBinding: FragmentGameBinding) {
        fragmentGameBinding.lottiePlay.translationX = fragmentGameBinding.root.width.toFloat()
    }

    private fun setPlayContainerVisibility(
        fragmentGameBinding: FragmentGameBinding,
        startValue: Int,
        endValue: Int,
        onAnimationFinished: (() -> Unit)? = null,
    ) {
        val view = fragmentGameBinding.playContainer
        val context = view.context

        val alphaAnimator = ValueAnimator.ofInt(startValue, endValue)
        alphaAnimator.duration = 1000

        alphaAnimator.addUpdateListener { animator ->
            val alpha = animator.animatedValue as Int
            val initialColor2 = context.getColor(R.color.forest_green)

            view.setBackgroundColor(
                Color.argb(
                    alpha,
                    Color.red(initialColor2),
                    Color.green(initialColor2),
                    Color.blue(initialColor2)
                )
            )

            if (animator.animatedValue == endValue) {
                onAnimationFinished?.invoke()
            }
        }

        alphaAnimator.start()
    }

    private fun resetLottiePlantProperties(fragmentGameBinding: FragmentGameBinding) {
        fragmentGameBinding.lottiePlant.progress = 0f
        fragmentGameBinding.lottiePlant.speed = 1f
    }

    private fun setButtonsOnClickListeners(
        fragmentGameBinding: FragmentGameBinding,
        action: (Int) -> Unit,
    ) {
        fragmentGameBinding.buttonsContainer.forEach {
            if (it is MaterialButton) {
                it.setOnClickListener { view ->
                    action.invoke((view.tag as String).toInt())
                }
            }
        }
    }

    private fun setButtonsClickable(fragmentGameBinding: FragmentGameBinding, clickable: Boolean) {
        fragmentGameBinding.buttonsContainer.forEach {
            if (it is MaterialButton) {
                it.isClickable = clickable
            }
        }
    }

    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(50)
        }
    }

    private fun playPlantAnimation(
        fragmentGameBinding: FragmentGameBinding,
        speed: Float,
        onAnimationComplete: (Float) -> Unit,
    ) {
        // Prevent plant to shrink if the plant hasn't grown yet
        if (fragmentGameBinding.lottiePlant.progress == 0f && speed == -1f) {
            onAnimationComplete.invoke(fragmentGameBinding.lottiePlant.progress)
            return
        }

        when (fragmentGameBinding.lottiePlant.progress) {
            0f -> {
                fragmentGameBinding.lottiePlant.speed = speed
                fragmentGameBinding.lottiePlant.playAnimation()
                pauseGrowDelayed(fragmentGameBinding, onAnimationComplete, growDuration)
            }

            else -> {
                fragmentGameBinding.lottiePlant.speed = speed
                fragmentGameBinding.lottiePlant.resumeAnimation()
                pauseGrowDelayed(fragmentGameBinding, onAnimationComplete, growDuration)
            }
        }
    }

    private fun pauseGrowDelayed(
        fragmentGameBinding: FragmentGameBinding,
        onAnimationComplete: (Float) -> Unit,
        delayed: Long,
    ) {
        Handler().postDelayed({
            fragmentGameBinding.lottiePlant.pauseAnimation()
            onAnimationComplete.invoke(fragmentGameBinding.lottiePlant.progress)
        }, delayed)
    }

    private fun revertPlayPlantAnimation(
        fragmentGameBinding: FragmentGameBinding, onAnimationEnd: () -> Unit,
    ) {
        fragmentGameBinding.lottiePlant.speed = -2f
        fragmentGameBinding.lottiePlant.resumeAnimation()

        fragmentGameBinding.lottiePlant.addAnimatorListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.invoke()

                fragmentGameBinding.lottiePlant.removeAllAnimatorListeners()
            }
        })
    }

    private fun selectRandomDigit(lang: String) {
        val path = "$lang/digits"
        Log.d(TAG, "selectRandomDigit: path:$path")
        val list = assetManager.list(path)
        val files = list.orEmpty()
        Log.d(TAG, "selectRandomDigit: list:${files.map { it.toString() }}")
        selectedSoundDigit = Random.Default.nextInt(files.size)
    }

    private fun selectRandomSoundWrong(lang: String) {
        val path = "$lang/wrong"
        Log.d(TAG, "selectRandomSoundWrong: path:$path")
        val list = assetManager.list(path)
        val files = list.orEmpty()
        Log.d(TAG, "selectRandomSoundWrong: list:${files.map { it.toString() }}")
        selectedSoundWrong = Random.Default.nextInt(files.size)
    }

    private fun selectRandomSoundSuccess(lang: String) {
        val path = "$lang/success"
        Log.d(TAG, "selectRandomSoundSuccess: path:$path")
        val list = assetManager.list(path)
        val files = list.orEmpty()
        Log.d(TAG, "selectRandomSoundSuccess: list:${files.map { it.toString() }}")
        selectedSoundSuccess = Random.Default.nextInt(files.size)
    }

    private fun askForDigit(
        digit: Int,
        lang: String,
        onCompletionListener: (() -> Unit)? = null,
    ) {
        try {
            val afd = assetManager.openFd("$lang/digits/_$digit.mp3")
            playSound(afd, onCompletionListener)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun playSoundWrong(
        digit: Int,
        lang: String,
        onPlayComplete: (() -> Unit)? = null,
    ) {
        try {
            val afd = assetManager.openFd("$lang/wrong/_$digit.mp3")
            playSound(afd, onPlayComplete)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun playSound(
        afd: AssetFileDescriptor,
        onCompletionListener: (() -> Unit)?,
    ) {
        MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            prepare()
            start()
            setOnCompletionListener {
                onCompletionListener?.invoke()

                it.stop()
                it.release()
            }
        }
    }

    private fun playSoundSuccess(
        digit: Int,
        lang: String,
        onCompletionListener: (() -> Unit)? = null,
    ) {
        try {
            val afd = assetManager.openFd("$lang/success/_$digit.mp3")
            playSound(afd, onCompletionListener)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun onContinueGame(fragmentGameBinding: FragmentGameBinding, language: String) {
        setButtonsClickable(fragmentGameBinding, false)
        selectRandomDigit(language)
        askForDigit(selectedSoundDigit, language) {
            setButtonsClickable(fragmentGameBinding, true)
        }
    }

    private fun onStartGame(fragmentGameBinding: FragmentGameBinding, language: String) {
        setNullToButtonsOnClickListeners(fragmentGameBinding)
        hideLottiePlay(fragmentGameBinding)
        setPlayContainerVisibility(fragmentGameBinding, 255, 0) {
            fragmentGameBinding.playContainer.isVisible = false
        }

        revertPlayPlantAnimation(fragmentGameBinding) {
            resetLottiePlantProperties(fragmentGameBinding)

            val action: (Int) -> Unit = { idTag ->
                setButtonsClickable(fragmentGameBinding, false)
                vibrate()

                if (idTag != selectedSoundDigit) {
                    selectRandomSoundWrong(language)
                    playSoundWrong(selectedSoundWrong,
                        language, onPlayComplete = {
                            playPlantAnimation(fragmentGameBinding, -1f) {
                                onContinueGame(fragmentGameBinding, language)
                            }
                        })
                } else {
                    playPlantAnimation(fragmentGameBinding, 1f) { progress ->
                        if (progress == 1f) {
                            onFinishGame(fragmentGameBinding, language)
                        } else {
                            onContinueGame(fragmentGameBinding, language)
                        }
                    }
                }
            }

            setButtonsOnClickListeners(fragmentGameBinding, action)

            onContinueGame(fragmentGameBinding, language)
        }
    }

    private fun onFinishGame(fragmentGameBinding: FragmentGameBinding, language: String) {
        // #1
        selectRandomSoundSuccess(language)
        playSoundSuccess(selectedSoundSuccess, language)

        // #2
        fragmentGameBinding.playContainer.isVisible = true
        setPlayContainerVisibility(fragmentGameBinding, 0, 255)

        // #3
        fragmentGameBinding.lottieAnimationSuccess.translationX = 0f
        fragmentGameBinding.lottieAnimationSuccess.addAnimatorListener(object :
            SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                // #5
                hideLottieAnimationSuccess(fragmentGameBinding)

                // #6
                fragmentGameBinding.lottiePlay.translationX = 0f
                fragmentGameBinding.lottiePlay.loop(true)
                fragmentGameBinding.lottiePlay.playAnimation()

                fragmentGameBinding.lottieAnimationSuccess.removeAllAnimatorListeners()
            }
        })

        // #4
        fragmentGameBinding.lottieAnimationSuccess.playAnimation()
    }

    private fun hideLottieAnimationSuccess(fragmentGameBinding: FragmentGameBinding) {
        fragmentGameBinding.lottieAnimationSuccess.translationX =
            fragmentGameBinding.root.width.toFloat()
    }

    fun setUpPlayClickListener(fragmentGameBinding: FragmentGameBinding, language: String) {
        fragmentGameBinding.lottiePlay.setOnClickListener {
            onStartGame(fragmentGameBinding, language)
        }
    }

    fun plantPlayAnimation(fragmentGameBinding: FragmentGameBinding) {
        fragmentGameBinding.lottiePlant.playAnimation()
    }

    companion object {
        private const val TAG = "GameViewModel"
    }

}