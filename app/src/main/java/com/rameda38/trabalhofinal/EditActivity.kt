package com.rameda38.trabalhofinal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.rameda38.trabalhofinal.databinding.ActivityEditBinding
import com.rameda38.trabalhofinal.model.Expense
import com.rameda38.trabalhofinal.viewmodel.ExpenseViewModel

import android.util.Log

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: ExpenseViewModel
    private var expense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        viewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)
        
        
        @Suppress("DEPRECATION")
        expense = intent.getSerializableExtra("EXPENSE") as? Expense

        if (expense != null) {
            
            
            binding.tvTitle.text = getString(R.string.title_edit_expense)
            binding.etDescription.setText(expense?.description)
            binding.etValue.setText(expense?.value.toString())
            binding.btnDelete.visibility = android.view.View.VISIBLE
        } else {
            
            
            binding.tvTitle.text = getString(R.string.title_add_expense)
            binding.btnDelete.visibility = android.view.View.GONE
        }

        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnDelete.setOnClickListener {
            deleteExpense()
        }
    }

    private fun deleteExpense() {
        expense?.let {
            
            viewModel.delete(it)
            Toast.makeText(this, "Despesa excluída com sucesso", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveExpense() {
        val description = binding.etDescription.text.toString()
        val valueStr = binding.etValue.text.toString()

        
        if (description.isEmpty() || valueStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_error_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val value = valueStr.toDoubleOrNull() ?: 0.0
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (expense == null) {
            
            val newExpense = Expense(
                description = description,
                value = value,
                userId = userId
            )
            
            viewModel.insert(newExpense)
        } else {
            
            val updatedExpense = expense!!.copy(
                description = description,
                value = value
            )
            
            viewModel.update(updatedExpense)
        }

        Toast.makeText(this, "Dados salvos!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
