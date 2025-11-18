package com.danielrothmann.randomuser.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danielrothmann.randomuser.R
import com.danielrothmann.randomuser.databinding.ItemListUsersBinding
import com.danielrothmann.randomuser.domain.model.User

class UsersAdapter(
    private val onItemClick: (User) -> Unit,
    private val onMoreClick: (User) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemListUsersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemListUsersBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvUserItemFirstName.text = user.fullName.split(" ").getOrNull(1) ?: ""
                tvUserItemLastName.text = user.fullName.split(" ").lastOrNull() ?: ""
                tvPhone.text = user.phone
                tvCountry.text = user.country

                Glide.with(itemView.context)
                    .load(user.pictureUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imageUserItem)

                // Загрузка флага страны (опционально)
                val countryCode = user.nationality.lowercase()
                val flagUrl = "https://flagcdn.com/w80/$countryCode.png"

                Glide.with(itemView.context)
                    .load(flagUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imageCountry)

                root.setOnClickListener {
                    onItemClick(user)
                }

                imageMore.setOnClickListener {
                    onMoreClick(user)
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}