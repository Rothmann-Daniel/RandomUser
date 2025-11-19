// presentation/adapters/UsersAdapter.kt
package com.danielrothmann.randomuser.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.danielrothmann.randomuser.databinding.ItemListUsersBinding
import com.danielrothmann.randomuser.domain.model.User

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
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class UserViewHolder(
        private val binding: ItemListUsersBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val user = getItem(adapterPosition)
                onUserClick(user)
            }

            binding.imageMore.setOnClickListener {
                val user = getItem(adapterPosition)
                onUserDelete(user)
            }
        }

        fun bind(user: User) {
            binding.imageUserItem.load(user.pictureUrl) {
                crossfade(true)
                placeholder(com.danielrothmann.randomuser.R.drawable.placeholder)
            }

            binding.tvUserItemFirstName.text = user.fullName.split(" ").getOrNull(1) ?: ""
            binding.tvUserItemLastName.text = user.fullName.split(" ").lastOrNull() ?: ""
            binding.tvPhone.text = user.phone
            binding.tvCountry.text = user.country

            // Загрузка флага страны можно реализовать через другую библиотеку
             binding.imageCountry.load("https://flagcdn.com/w320/${user.nationality.toLowerCase()}.png")
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