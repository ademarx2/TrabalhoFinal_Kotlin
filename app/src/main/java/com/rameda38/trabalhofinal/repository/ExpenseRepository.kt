package com.rameda38.trabalhofinal.repository

import androidx.lifecycle.LiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rameda38.trabalhofinal.database.ExpenseDao
import com.rameda38.trabalhofinal.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("expenses")

    fun getAllExpenses(userId: String): LiveData<List<Expense>> {
        return expenseDao.getAll(userId)
    }

    suspend fun insert(expense: Expense) {
        
        val ref = database.child(expense.userId).push()
        val key = ref.key ?: ""
        val expenseWithKey = expense.copy(id = key)
        
        
        ref.setValue(expenseWithKey)
        
        
        expenseDao.insert(expenseWithKey)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
        
        if (expense.id.isNotEmpty()) {
            database.child(expense.userId).child(expense.id).setValue(expense)
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
