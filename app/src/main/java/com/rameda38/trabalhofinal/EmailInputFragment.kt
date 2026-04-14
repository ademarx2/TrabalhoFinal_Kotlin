package com.rameda38.trabalhofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rameda38.trabalhofinal.databinding.FragmentInputEmailBinding

class EmailInputFragment : Fragment() {
    private var _binding: FragmentInputEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun getEmail(): String = _binding?.etEmail?.text?.toString() ?: ""

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
