package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielrothmann.randomuser.databinding.ActivityListUsersBinding
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.presentation.adapters.UsersAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListUsersBinding
    private val viewModel: ListUsersViewModel by viewModel()
    private lateinit var adapter: UsersAdapter

    private var deletedUser: com.danielrothmann.randomuser.domain.model.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(com.danielrothmann.randomuser.R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        adapter = UsersAdapter(
            onUserClick = { user ->
                val intent = Intent(this, DetailsActivity::class.java).apply {
                    putExtra("USER_UUID", user.uuid)
                }
                startActivity(intent)
            },
            onUserDelete = { user ->
                deletedUser = user
                viewModel.deleteUser(user)
                showUndoSnackbar()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListUsersActivity)
            adapter = this@ListUsersActivity.adapter
        }
    }

    private fun showUndoSnackbar() {
        deletedUser?.let { user ->
            Snackbar.make(binding.root, "User ${user.fullName} deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    deletedUser?.let {
                        viewModel.restoreUser(it)
                    }
                }
                .show()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.users.collectLatest { users ->
                adapter.submitList(users)
                binding.recyclerView.isVisible = users.isNotEmpty()
                // Здесь можно добавить TextView для пустого состояния
                // binding.emptyState.isVisible = users.isEmpty()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnAddUser.isEnabled = !isLoading
                // Можно добавить ProgressBar на кнопку
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.generatedUserState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { user ->
                            // Переходим к деталям сгенерированного пользователя
                            val intent = Intent(this@ListUsersActivity, DetailsActivity::class.java).apply {
                                putExtra("USER_UUID", user.uuid)
                            }
                            startActivity(intent)
                        }
                    }
                    is Resource.Error -> {
                        Snackbar.make(binding.root, "Error: ${resource.message}", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Loading -> {
                        // Loading state
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddUser.setOnClickListener {
            viewModel.generateAndSaveUser()
        }
    }
}