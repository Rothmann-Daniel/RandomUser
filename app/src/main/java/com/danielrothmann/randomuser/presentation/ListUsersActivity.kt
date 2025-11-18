package com.danielrothmann.randomuser.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielrothmann.randomuser.R
import com.danielrothmann.randomuser.databinding.ActivityListUsersBinding
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
            onItemClick = { user ->
                val intent = Intent(this, DetailsActivity::class.java).apply {
                    putExtra("USER", user)
                }
                startActivity(intent)
            },
            onMoreClick = { user ->
                showDeleteDialog(user)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListUsersActivity)
            adapter = this@ListUsersActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(this) { users ->
            adapter.submitList(users)
        }
    }

    private fun setupClickListeners() {
        binding.btnAddUser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDeleteDialog(user: com.danielrothmann.randomuser.domain.model.User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.fullName}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteUser(user)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}