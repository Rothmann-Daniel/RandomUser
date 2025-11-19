package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.danielrothmann.randomuser.databinding.ActivityMainBinding
import com.danielrothmann.randomuser.domain.model.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()

    private val nationalities = listOf(
        "AU", "BR", "CA", "CH", "DE", "DK", "ES", "FI", "FR", "GB", "IE", "IN", "IR", "MX", "NL", "NO", "NZ", "RS", "TR", "UA", "US"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()
        setupObservers()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        // Gender dropdown
        val genders = resources.getStringArray(com.danielrothmann.randomuser.R.array.gender_array)
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.dropdownSelectGender.setAdapter(genderAdapter)

        // Nationality dropdown
        val nationalityNames = nationalities.map { getNationalityName(it) }
        val nationalityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalityNames)
        binding.dropdownSelectNationality.setAdapter(nationalityAdapter)
    }

    private fun getNationalityName(code: String): String {
        return when (code) {
            "AU" -> "Australia"
            "BR" -> "Brazil"
            "CA" -> "Canada"
            "CH" -> "Switzerland"
            "DE" -> "Germany"
            "DK" -> "Denmark"
            "ES" -> "Spain"
            "FI" -> "Finland"
            "FR" -> "France"
            "GB" -> "United Kingdom"
            "IE" -> "Ireland"
            "IN" -> "India"
            "IR" -> "Iran"
            "MX" -> "Mexico"
            "NL" -> "Netherlands"
            "NO" -> "Norway"
            "NZ" -> "New Zealand"
            "RS" -> "Serbia"
            "TR" -> "Turkey"
            "UA" -> "Ukraine"
            "US" -> "United States"
            else -> code
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnGenerateUser.isEnabled = !isLoading
                binding.btnGenerateUser.text = if (isLoading) "Generating..." else "Generate"
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.hasUsers.collectLatest { hasUsers ->
                binding.btnViewList.isVisible = hasUsers
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.generatedUserState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            // Переходим к деталям сгенерированного пользователя
                            val intent = Intent(this@MainActivity, DetailsActivity::class.java).apply {
                                putExtra("USER_UUID", user.uuid)
                            }
                            startActivity(intent)
                        }
                    }
                    is Resource.Error -> {
                        Snackbar.make(binding.root, "Error: ${resource.message}", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading -> {
                        // Loading state уже обрабатывается в isLoading flow
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGenerateUser.setOnClickListener {
            val gender = getSelectedGender()
            val nationality = getSelectedNationality()

            viewModel.generateSingleUser(gender, nationality)
        }

        binding.btnViewList.setOnClickListener {
            val intent = Intent(this, ListUsersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getSelectedGender(): String? {
        val selectedGender = binding.dropdownSelectGender.text.toString()
        return when (selectedGender) {
            "Male" -> "male"
            "Female" -> "female"
            else -> null
        }
    }

    private fun getSelectedNationality(): String? {
        val selectedNationality = binding.dropdownSelectNationality.text.toString()
        return nationalities.find { code -> getNationalityName(code) == selectedNationality }
    }
}