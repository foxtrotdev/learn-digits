package eu.mokrzycki.learndigits.ui.game

import android.content.res.AssetManager
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(private val assetManager: AssetManager, private val vibrator : Vibrator) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(assetManager, vibrator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}