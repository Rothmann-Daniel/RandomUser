package com.danielrothmann.randomuser.presentation

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.danielrothmann.randomuser.R
import com.danielrothmann.randomuser.databinding.ActivityDetailsBinding
import com.danielrothmann.randomuser.domain.model.User
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("USER", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("USER")
        }

        setupToolbar()
        setupTabs()
        currentUser?.let { displayUserData(it) }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.apply {
            addTab(newTab().setText("Profile").setIcon(R.drawable.ic_person))
            addTab(newTab().setText("Call").setIcon(R.drawable.ic_phone_white))
            addTab(newTab().setText("Email").setIcon(R.drawable.ic_email_white))
            addTab(newTab().setText("Location").setIcon(R.drawable.ic_location))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showProfileContent()
                    1 -> showCallContent()
                    2 -> showEmailContent()
                    3 -> showLocationContent()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun displayUserData(user: User) {
        binding.apply {
            tvName.text = user.fullName

            Glide.with(this@DetailsActivity)
                .load(user.pictureUrl)
                .placeholder(R.drawable.placeholder)
                .into(ivAvatar)

            // Profile tab
            val nameParts = user.fullName.split(" ")
            tvFirstName.text = "First name: ${nameParts.getOrNull(1) ?: ""}"
            tvLastName.text = "Last name: ${nameParts.lastOrNull() ?: ""}"
            tvGender.text = "Gender: ${user.gender}"
            tvAge.text = "Age: ${user.age}"
            tvDob.text = "Date of birth: ${user.registeredDate.substringBefore("T")}"

            // Call tab
            tvPhone.text = user.phone
            tvCell.text = user.cell

            // Email tab
            tvEmail.text = user.email

            // Location tab
            tvAddress.text = "${user.address}\n${user.city}, ${user.state}\n${user.country}"
            tvCoordinates.text = "Lat: ${user.coordinates.latitude}, Lng: ${user.coordinates.longitude}"
        }
    }

    private fun showProfileContent() {
        binding.apply {
            contentProfile.visibility = View.VISIBLE
            contentCall.visibility = View.GONE
            contentEmail.visibility = View.GONE
            contentLocation.visibility = View.GONE
        }
    }

    private fun showCallContent() {
        binding.apply {
            contentProfile.visibility = View.GONE
            contentCall.visibility = View.VISIBLE
            contentEmail.visibility = View.GONE
            contentLocation.visibility = View.GONE
        }
    }

    private fun showEmailContent() {
        binding.apply {
            contentProfile.visibility = View.GONE
            contentCall.visibility = View.GONE
            contentEmail.visibility = View.VISIBLE
            contentLocation.visibility = View.GONE
        }
    }

    private fun showLocationContent() {
        binding.apply {
            contentProfile.visibility = View.GONE
            contentCall.visibility = View.GONE
            contentEmail.visibility = View.GONE
            contentLocation.visibility = View.VISIBLE
        }
    }
}