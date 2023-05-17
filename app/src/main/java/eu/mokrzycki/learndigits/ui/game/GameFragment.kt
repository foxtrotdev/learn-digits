package eu.mokrzycki.learndigits.ui.game

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import eu.mokrzycki.learndigits.R
import eu.mokrzycki.learndigits.databinding.FragmentGameBinding
import java.io.IOException
import kotlin.random.Random


class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null

    private val binding get() = _binding!!

    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val lang by lazy {
        requireContext().resources.getString(R.string.assets_folder_lang)
    }

    val gameViewModel by lazy {
        ViewModelProvider(this)[GameViewModel::class.java]
    }

    private var selectedSoundDigit = 0
    private var selectedSoundWrong = 0
    private var selectedSoundSuccess = 0
    private val growDuration = 400L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lottiePlant.playAnimation()

        binding.lottiePlay.setOnClickListener {
            onStartGame()
        }
    }

    private fun onStartGame() {
        setNullToButtonsOnClickListeners()

        hideLottiePlay()

        setPlayContainerVisibility(255, 0) {
            binding.playContainer.isVisible = false
        }

        revertPlayPlantAnimation {
            resetLottiePlantProperties()

            setButtonsOnClickListeners { idTag ->
                setButtonsClickable(false)

                vibrate()

                if (idTag != selectedSoundDigit) {
                    selectRandomSoundWrong(lang)
                    playSoundWrong(selectedSoundWrong, lang, onPlayComplete = {
                        playPlantAnimation(-1f, onAnimationComplete = {
                            onContinueGame()
                        })
                    })
                } else {
                    playPlantAnimation(1f, onAnimationComplete = { progress ->
                        if (progress == 1f) {
                            onFinishGame()
                        } else {
                            onContinueGame()
                        }
                    })
                }
            }

            onContinueGame()
        }
    }

    private fun onContinueGame() {
        setButtonsClickable(false)
        selectRandomDigit(lang)
        askForDigit(selectedSoundDigit, lang) {
            setButtonsClickable(true)
        }
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

    private val assetManager: AssetManager by lazy {
        requireContext().assets
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

    private fun resetLottiePlantProperties() {
        binding.lottiePlant.progress = 0f
        binding.lottiePlant.speed = 1f
    }

    private fun revertPlayPlantAnimation(onAnimationEnd: () -> Unit) {
        binding.lottiePlant.speed = -2f
        binding.lottiePlant.resumeAnimation()

        binding.lottiePlant.addAnimatorListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.invoke()

                binding.lottiePlant.removeAllAnimatorListeners()
            }
        })
    }

    private fun setButtonsOnClickListeners(action: (Int) -> Unit) {
        binding.container2.forEach { v ->
            if (v is MaterialButton) {
                v.setOnClickListener {
                    action.invoke((v.tag as String).toInt())
                }
            }
        }
    }

    private fun setNullToButtonsOnClickListeners() {
        binding.container2.forEach {
            if (it is MaterialButton) {
                it.setOnClickListener(null)
            }
        }
    }

    private fun setButtonsClickable(clickable: Boolean) {
        binding.container2.forEach {
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

    private fun setPlayContainerVisibility(
        startValue: Int,
        endValue: Int,
        onAnimationFinished: (() -> Unit)? = null,
    ) {
        val view = binding.playContainer

        val alphaAnimator = ValueAnimator.ofInt(startValue, endValue)
        alphaAnimator.duration = 1000

        alphaAnimator.addUpdateListener { animator ->
            val alpha = animator.animatedValue as Int
            val initialColor2 = view.context.getColor(R.color.forest_green)

            view.setBackgroundColor(
                Color.argb(
                    alpha,
                    Color.red(initialColor2),
                    Color.green(initialColor2),
                    Color.blue(initialColor2)
                )
            )

            if (animator.animatedValue == endValue)
                onAnimationFinished?.invoke()
        }

        alphaAnimator.start()
    }

    private fun hideLottiePlay() {
        binding.lottiePlay.translationX = binding.root.width.toFloat()
    }

    private fun playPlantAnimation(speed: Float, onAnimationComplete: (Float) -> Unit) {
        if (binding.lottiePlant.progress == 0f && speed == -1f) {
            onAnimationComplete.invoke(binding.lottiePlant.progress)
            return
        }

        when (binding.lottiePlant.progress) {
            0f -> {
                binding.lottiePlant.speed = speed
                binding.lottiePlant.playAnimation()

                Handler().postDelayed({
                    binding.lottiePlant.pauseAnimation()
                    onAnimationComplete.invoke(binding.lottiePlant.progress)
                }, growDuration)
            }

            else -> {
                binding.lottiePlant.speed = speed
                binding.lottiePlant.resumeAnimation()

                Handler().postDelayed({
                    binding.lottiePlant.pauseAnimation()
                    onAnimationComplete.invoke(binding.lottiePlant.progress)
                }, growDuration)
            }
        }
    }

    private fun onFinishGame() {
        // #1
        selectRandomSoundSuccess(lang)
        playSoundSuccess(selectedSoundSuccess, lang)

        // #2
        binding.playContainer.isVisible = true
        setPlayContainerVisibility(0, 255) {}

        // #3
        binding.lottieAnimationSuccess.translationX = 0f
        binding.lottieAnimationSuccess.addAnimatorListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                // #5
                binding.lottieAnimationSuccess.translationX = binding.root.width.toFloat()

                // #6
                binding.lottiePlay.translationX = 0f
                binding.lottiePlay.loop(true)
                binding.lottiePlay.playAnimation()

                binding.lottieAnimationSuccess.removeAllAnimatorListeners()
            }
        })

        // #4
        binding.lottieAnimationSuccess.playAnimation()
    }

    private fun setLottieBgLayerVisibility(startValue: Int, endValue: Int) {
        val alphaAnimator = ValueAnimator.ofInt(startValue, endValue)
        alphaAnimator.duration = 1000
        val initialColor = (binding.lottieBgLayer.background as ColorDrawable).color

        alphaAnimator.addUpdateListener { animator ->
            val alpha = animator.animatedValue as Int
            binding.lottieBgLayer.setBackgroundColor(
                Color.argb(
                    alpha,
                    Color.red(initialColor),
                    Color.green(initialColor),
                    Color.blue(initialColor)
                )
            )
        }

        alphaAnimator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "GameFragment"
    }
}