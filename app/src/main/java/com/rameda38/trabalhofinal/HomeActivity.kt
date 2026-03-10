package com.rameda38.trabalhofinal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rameda38.trabalhofinal.adapter.ExpenseAdapter
import com.rameda38.trabalhofinal.databinding.ActivityHomeBinding
import com.rameda38.trabalhofinal.model.Expense
import com.rameda38.trabalhofinal.viewmodel.ExpenseViewModel
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

import androidx.appcompat.app.AppCompatDelegate

import android.util.Log

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(ExpenseViewModel::class.java)

        
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Usuário")
        binding.toolbar.title = "Olá, $userName"

        
        loadFinancialTip()
        showAppInfoFromRaw()

        val adapter = ExpenseAdapter { expense ->
            
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("EXPENSE", expense)
            startActivity(intent)
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        
        val userId = auth.currentUser?.uid ?: ""
        viewModel.getAllExpenses(userId).observe(this) { expenses ->
            
            adapter.submitList(expenses)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditActivity::class.java))
        }
    }

    
    private fun showAppInfoFromRaw() {
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.app_info)
            val info = inputStream.bufferedReader().use { it.readText() }
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Erro ao ler Raw", e)
        }
    }

    
    private fun loadFinancialTip() {
        try {
            val inputStream: InputStream = assets.open("financial_tip.txt")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val tip = String(buffer, Charset.forName("UTF-8"))
            binding.tvFinancialTip.text = tip
        } catch (e: IOException) {
            Log.e("HomeActivity", "Erro ao ler Assets", e)
            binding.cardTip.visibility = android.view.View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_theme -> {
                val currentMode = AppCompatDelegate.getDefaultNightMode()
                if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                return true
            }
            R.id.action_logout -> {
                auth.signOut()
                
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
