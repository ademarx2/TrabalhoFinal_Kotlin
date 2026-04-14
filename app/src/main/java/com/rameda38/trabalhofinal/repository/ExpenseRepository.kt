package com.rameda38.trabalhofinal.repository

import androidx.lifecycle.LiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rameda38.trabalhofinal.database.ExpenseDao
import com.rameda38.trabalhofinal.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://trabalho-final-android-2a320-default-rtdb.firebaseio.com/").reference.child("expenses")

    fun getAllExpenses(userId: String): LiveData<List<Expense>> {
        return expenseDao.getAll(userId)
    }

    suspend fun insert(expense: Expense) {
        val userId = expense.userId
        if (userId.isEmpty()) {
            expenseDao.insert(expense)
            return
        }

        val ref = database.child(userId).push()
        val key = ref.key ?: ""
        val expenseWithKey = expense.copy(id = key)

        ref.setValue(expenseWithKey)
            .addOnFailureListener { e ->
                android.util.Log.e("ExpenseRepository", "Erro ao salvar no Firebase: ${e.message}")
            }

        expenseDao.insert(expenseWithKey)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
        
        if (expense.id.isNotEmpty() && expense.userId.isNotEmpty()) {
            database.child(expense.userId).child(expense.id).setValue(expense)
                .addOnFailureListener { e ->
                    android.util.Log.e("ExpenseRepository", "Erro ao atualizar no Firebase: ${e.message}")
                }
        }
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
        if (expense.id.isNotEmpty()) {
            database.child(expense.userId).child(expense.id).removeValue()
        }
    }

    suspend fun syncExpenses(expenses: List<Expense>) {
        for (expense in expenses) {
            
            expenseDao.insert(expense) 
        }
    }
}
