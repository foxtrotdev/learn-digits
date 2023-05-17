package eu.mokrzycki.learndigits.ui.exit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import eu.mokrzycki.learndigits.databinding.FragmentExitBinding
import kotlin.system.exitProcess

class ExitFragment : Fragment() {

    private var _binding: FragmentExitBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        exitProcess(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}