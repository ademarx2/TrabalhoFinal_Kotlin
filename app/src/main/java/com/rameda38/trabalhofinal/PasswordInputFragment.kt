package com.rameda38.trabalhofinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rameda38.trabalhofinal.databinding.FragmentInputPasswordBinding

class PasswordInputFragment : Fragment() {
    private var _binding: FragmentInputPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInputPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun getPassword(): String = _binding?.etPassword?.text?.toString() ?: ""

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
