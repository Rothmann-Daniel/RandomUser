package com.danielrothmann.randomuser.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.danielrothmann.randomuser.databinding.DialogDeleteConfirmationBinding
import com.danielrothmann.randomuser.databinding.ItemListUsersBinding
import com.danielrothmann.randomuser.domain.model.User
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersAdapter(
    private val onUserClick: (User) -> Unit,
    private val onUserDelete: (User) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemListUsersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick, onUserDelete)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    class UserViewHolder(
        private val binding: ItemListUsersBinding,
        private val onUserClick: (User) -> Unit,
        private val onUserDelete: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentUser: User? = null

        init {
            binding.root.setOnClickListener {
                currentUser?.let { user ->
                    onUserClick(user)
                }
            }

            binding.imageMore.setOnClickListener {
                currentUser?.let { user ->
                    showDeleteBottomSheetDialog(user)
                }
            }
        }

        fun bind(user: User) {
            currentUser = user

            binding.imageUserItem.load(user.pictureUrl) {
                crossfade(true)
                placeholder(com.danielrothmann.randomuser.R.drawable.placeholder)
            }

            val nameParts = user.fullName.split(" ")
            binding.tvUserItemFirstName.text = nameParts.getOrNull(1) ?: ""
            binding.tvUserItemLastName.text = nameParts.lastOrNull() ?: ""
            binding.tvPhone.text = user.phone
            binding.tvCountry.text = user.country

            // Загрузка флага страны
            binding.imageCountry.load("https://flagcdn.com/w320/${user.nationality.toLowerCase()}.png") {
                crossfade(true)
                placeholder(com.danielrothmann.randomuser.R.drawable.placeholder)
            }
        }

        private fun showDeleteBottomSheetDialog(user: User) {
            val context = binding.root.context
            val dialog = BottomSheetDialog(context)
            val dialogBinding = DialogDeleteConfirmationBinding.inflate(LayoutInflater.from(context))

            dialogBinding.tvDeleteMessage.text = "Are you sure you want to delete ${user.fullName}? This action cannot be undone."

            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnDelete.setOnClickListener {
                onUserDelete(user)
                dialog.dismiss()
            }

            dialog.setContentView(dialogBinding.root)
            dialog.show()
        }

        // Альтернативный метод с обычным AlertDialog (оставляем на выбор)
        private fun showDeleteConfirmationDialog(user: User) {
            val context = binding.root.context
            android.app.AlertDialog.Builder(context)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete ${user.fullName}?")
                .setPositiveButton("Delete") { dialog, which ->
                    onUserDelete(user)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setIcon(com.danielrothmann.randomuser.R.drawable.ic_warning)
                .create()
                .show()
        }
    }

    object UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}