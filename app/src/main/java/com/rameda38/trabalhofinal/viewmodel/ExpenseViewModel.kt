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
    private val database = FirebaseDatabase.getInstance().getReference("expenses")

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
                            
                            val id = child.child("id").value?.toString() ?: child.key ?: ""
                            val description = child.child("description").value?.toString() ?: ""
                            val value = (child.child("value").value as? Number)?.toDouble() ?: 0.0
                            val userId = child.child("userId").value?.toString() ?: ""
                            
                            val expense = Expense(
                                id = id,
                                description = description,
                                value = value,
                                userId = userId
                            )
                            firebaseExpenses.add(expense)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    repository.syncExpenses(firebaseExpenses)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
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
