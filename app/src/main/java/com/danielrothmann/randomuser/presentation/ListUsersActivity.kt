package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielrothmann.randomuser.databinding.ActivityListUsersBinding
import com.danielrothmann.randomuser.domain.model.Resource
import com.danielrothmann.randomuser.presentation.adapters.UsersAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
        onBackPressedDispatcher.onBackPressed()
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Список пользователей
                launch {
                    viewModel.users.collectLatest { users ->
                        adapter.submitList(users)
                        binding.recyclerView.isVisible = users.isNotEmpty()
                    }
                }

                // Состояние загрузки
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.btnAddUser.isEnabled = !isLoading
                    }
                }

                // Состояние генерации пользователя
                launch {
                    viewModel.generatedUserState.collectLatest { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    val intent = Intent(this@ListUsersActivity, DetailsActivity::class.java).apply {
                                        putExtra("USER_UUID", user.uuid)
                                    }
                                    viewModel.clearGeneratedUserState()
                                    startActivity(intent)
                                }
                            }
                            is Resource.Error -> {
                                Snackbar.make(binding.root, "Error: ${resource.message}", Snackbar.LENGTH_LONG).show()
                                viewModel.clearGeneratedUserState()
                            }
                            is Resource.Loading -> {
                                // Loading state
                            }
                            null -> {
                                // Состояние сброшено
                            }
                        }
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