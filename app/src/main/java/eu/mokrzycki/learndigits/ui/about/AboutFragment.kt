package eu.mokrzycki.learndigits.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import eu.mokrzycki.learndigits.BuildConfig
import eu.mokrzycki.learndigits.databinding.FragmentAboutBinding
import java.util.Locale


class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.aboutVersion.text = String.format(
            Locale.getDefault(),
            getString(eu.mokrzycki.learndigits.R.string.about_version),
            BuildConfig.VERSION_NAME
        )

        binding.aboutSource.setOnClickListener {
            val intent = Intent()
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://github.com/foxtrotdev/learn-digits"))

            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}