package eu.mokrzycki.learndigits.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.backbase.deferredresources.DeferredText
import eu.mokrzycki.learndigits.R

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<DeferredText>().apply {
        value = DeferredText.Resource(R.string.home_labels_introduction)
    }
    val text: LiveData<DeferredText> = _text
}