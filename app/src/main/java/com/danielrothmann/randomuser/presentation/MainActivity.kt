package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.danielrothmann.randomuser.R
import com.danielrothmann.randomuser.databinding.ActivityMainBinding
import com.danielrothmann.randomuser.domain.model.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private val nationalities = listOf(
        "AU" to "Australian",
        "BR" to "Brazilian",
        "CA" to "Canadian",
        "CH" to "Swiss",
        "DE" to "German",
        "DK" to "Danish",
        "ES" to "Spanish",
        "FI" to "Finnish",
        "FR" to "French",
        "GB" to "British",
        "IE" to "Irish",
        "IN" to "Indian",
        "IR" to "Iranian",
        "MX" to "Mexican",
        "NL" to "Dutch",
        "NO" to "Norwegian",
        "NZ" to "New Zealander",
        "RS" to "Serbian",
        "TR" to "Turkish",
        "UA" to "Ukrainian",
        "US" to "American"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupGenderDropdown()
        setupNationalityDropdown()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.appBarLayout.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupGenderDropdown() {
        val genders = listOf("Select gender", "Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.dropdownSelectGender.setAdapter(adapter)
    }

    private fun setupNationalityDropdown() {
        val nationalityNames = listOf("Select nationality (optional)") + nationalities.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        binding.dropdownSelectNationality.setAdapter(adapter)
    }

    private fun setupObservers() {
        viewModel.usersState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnGenerateUser.isEnabled = false
                    binding.btnGenerateUser.text = "Loading..."
                }
                is Resource.Success -> {
                    binding.btnGenerateUser.isEnabled = true
                    binding.btnGenerateUser.text = getString(R.string.generate)

                    val user = resource.data.firstOrNull()
                    if (user != null) {
                        val intent = Intent(this, DetailsActivity::class.java).apply {
                            putExtra("USER", user)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No user data received", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.btnGenerateUser.isEnabled = true
                    binding.btnGenerateUser.text = getString(R.string.generate)
                    showErrorDialog(resource.message)
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            // Дополнительные сообщения об ошибках
        }
    }

    private fun setupClickListeners() {
        binding.btnGenerateUser.setOnClickListener {
            val gender = binding.dropdownSelectGender.text.toString()
            val nationality = binding.dropdownSelectNationality.text.toString()

            val genderParam = when {
                gender == "Male" -> "male"
                gender == "Female" -> "female"
                else -> null
            }

            // Не передаем nationality если выбран default или пусто
            val nationalityParam = when {
                nationality.isEmpty() ||
                        nationality == "Select nationality (optional)" -> null
                else -> nationalities.find { it.second == nationality }?.first
            }

            viewModel.generateUser(genderParam, nationalityParam)
        }

        binding.btnViewList.setOnClickListener {
            val intent = Intent(this, ListUsersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                // Повторить последний запрос
                binding.btnGenerateUser.performClick()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Try without nationality") { _, _ ->
                // Очистить выбор национальности и попробовать снова
                binding.dropdownSelectNationality.setText("", false)
                binding.btnGenerateUser.performClick()
            }
            .show()
    }
}
