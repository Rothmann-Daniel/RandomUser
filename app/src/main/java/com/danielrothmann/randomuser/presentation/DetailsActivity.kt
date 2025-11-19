package com.danielrothmann.randomuser.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import coil.load
import com.danielrothmann.randomuser.databinding.ActivityDetailsBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: DetailsViewModel by viewModel {
        parametersOf(intent.getStringExtra("USER_UUID") ?: "")
    }

    private var isNewUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем, является ли это новым пользователем
        isNewUser = intent.getBooleanExtra("IS_NEW_USER", false)
        Log.d("DetailsActivity", "onCreate: isNewUser = $isNewUser")

        setupToolbar()
        setupTabs()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                Log.d("DetailsActivity", "Back button clicked")
                handleBackNavigation()
            }
            title = ""
            subtitle = ""
        }

        binding.collapsingToolbar.title = ""
        binding.collapsingToolbar.isTitleEnabled = false
    }

    private fun handleBackNavigation() {
        if (isNewUser) {
            // Если это новый пользователь, отправляем результат
            Log.d("DetailsActivity", "Setting result for new user")
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("USER_ADDED", true)
            })
        }
        finish()
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Profile").setIcon(com.danielrothmann.randomuser.R.drawable.ic_person))
            addTab(newTab().setText("Call").setIcon(com.danielrothmann.randomuser.R.drawable.ic_phone_white))
            addTab(newTab().setText("Email").setIcon(com.danielrothmann.randomuser.R.drawable.ic_email_white))
            addTab(newTab().setText("Location").setIcon(com.danielrothmann.randomuser.R.drawable.ic_location))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTabContent(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Наблюдаем за состоянием загрузки
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.isVisible = isLoading
                        binding.nestedScrollView.isVisible = !isLoading
                        binding.tvError.isVisible = false
                    }
                }

                // Наблюдаем за данными пользователя
                launch {
                    viewModel.userState.collectLatest { user ->
                        user?.let {
                            displayUserData(it)
                            binding.nestedScrollView.isVisible = true
                            binding.tvError.isVisible = false
                        }
                    }
                }

                // Наблюдаем за ошибками
                launch {
                    viewModel.errorState.collectLatest { error ->
                        error?.let {
                            binding.tvError.isVisible = true
                            binding.tvError.text = error
                            binding.nestedScrollView.isVisible = false
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            }
        }
    }

    private fun displayUserData(user: com.danielrothmann.randomuser.domain.model.User) {
        binding.ivAvatar.load(user.pictureUrl) {
            crossfade(true)
            placeholder(com.danielrothmann.randomuser.R.drawable.placeholder)
        }

        binding.tvName.text = user.fullName

        val nameParts = user.fullName.split(" ")
        binding.tvFirstName.text = "First name: ${nameParts.getOrNull(1) ?: ""}"
        binding.tvLastName.text = "Last name: ${nameParts.lastOrNull() ?: ""}"
        binding.tvGender.text = "Gender: ${user.gender}"
        binding.tvAge.text = "Age: ${user.age}"
        binding.tvDob.text = "Date of birth: ${formatDate(user.registeredDate)}"

        binding.tvPhone.text = user.phone
        binding.tvCell.text = user.cell

        binding.tvEmail.text = user.email

        binding.tvAddress.text = "${user.address}\n${user.city}, ${user.state} ${user.postcode}\n${user.country}"
        binding.tvCoordinates.text = "Lat: ${user.coordinates.latitude}, Lng: ${user.coordinates.longitude}"
    }

    private fun formatDate(dateString: String): String {
        return try {
            dateString.substring(0, 10)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun updateTabContent(position: Int) {
        binding.contentProfile.isVisible = position == 0
        binding.contentCall.isVisible = position == 1
        binding.contentEmail.isVisible = position == 2
        binding.contentLocation.isVisible = position == 3
    }
}