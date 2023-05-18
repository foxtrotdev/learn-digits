package eu.mokrzycki.learndigits.ui.game

import android.content.Context
import android.content.res.AssetManager
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import eu.mokrzycki.learndigits.R
import eu.mokrzycki.learndigits.databinding.FragmentGameBinding


class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null

    private val binding get() = _binding!!

    private val assetManager: AssetManager by lazy {
        requireContext().assets
    }

    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val lang by lazy {
        requireContext().resources.getString(R.string.assets_folder_lang)
    }

    private val gameViewModel by lazy {
        ViewModelProvider(
            this,
            GameViewModelFactory(assetManager, vibrator)
        )[GameViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gameViewModel.plantPlayAnimation(binding)
        gameViewModel.setUpPlayClickListener(binding, lang)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "GameFragment"
    }
}