package com.rameda38.trabalhofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rameda38.trabalhofinal.databinding.FragmentButtonBinding

class ButtonFragment : Fragment() {
    private var _binding: FragmentButtonBinding? = null
    private val binding get() = _binding!!

    private var buttonText: String? = null
    private var clickListener: View.OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonText?.let { binding.btnFragment.text = it }
        clickListener?.let { binding.btnFragment.setOnClickListener(it) }
    }

    fun setButtonText(text: String) {
        buttonText = text
        if (_binding != null) {
            binding.btnFragment.text = text
        }
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
        if (_binding != null) {
            binding.btnFragment.setOnClickListener(listener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
