package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielrothmann.randomuser.databinding.ActivityListUsersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.danielrothmann.randomuser.presentation.adapters.UsersAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListUsersBinding
    private val viewModel: ListUsersViewModel by viewModel()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
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
                viewModel.deleteUser(user)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListUsersActivity)
            adapter = this@ListUsersActivity.adapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.users.collectLatest { users ->
                adapter.submitList(users)

                // Показываем пустое состояние если нет пользователей
                binding.recyclerView.isVisible = users.isNotEmpty()
                // Здесь можно добавить TextView для пустого состояния
                // binding.emptyState.isVisible = users.isEmpty()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddUser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}