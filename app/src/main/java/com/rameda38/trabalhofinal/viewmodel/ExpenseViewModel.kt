package com.rameda38.trabalhofinal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rameda38.trabalhofinal.database.AppDatabase
import com.rameda38.trabalhofinal.model.Expense
import com.rameda38.trabalhofinal.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val database = FirebaseDatabase.getInstance("https://trabalho-final-android-2a320-default-rtdb.firebaseio.com/").getReference("expenses")

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    fun getAllExpenses(userId: String): LiveData<List<Expense>> {
        syncWithFirebase(userId)
        return repository.getAllExpenses(userId)
    }

    private fun syncWithFirebase(userId: String) {
        if (userId.isEmpty()) return
        
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val firebaseExpenses = mutableListOf<Expense>()
                    for (child in snapshot.children) {
                        try {
                            val expense = child.getValue(Expense::class.java)
                            if (expense != null) {
                                firebaseExpenses.add(expense)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ExpenseViewModel", "Erro ao converter despesa: ${e.message}")
                        }
                    }
                    repository.syncExpenses(firebaseExpenses)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.w("ExpenseViewModel", "Erro ao sincronizar: ${error.message}")
            }
        })
    }

    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun update(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
}
