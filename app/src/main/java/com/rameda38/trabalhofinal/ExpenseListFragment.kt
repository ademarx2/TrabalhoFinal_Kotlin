package com.rameda38.trabalhofinal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rameda38.trabalhofinal.adapter.ExpenseAdapter
import com.rameda38.trabalhofinal.databinding.FragmentExpenseListBinding
import com.rameda38.trabalhofinal.viewmodel.ExpenseViewModel

class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)
        setupRecyclerView()
        observeExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { expense ->
            val intent = Intent(requireContext(), EditActivity::class.java)
            intent.putExtra("EXPENSE", expense)
            startActivity(intent)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun observeExpenses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.getAllExpenses(userId).observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            binding.tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
