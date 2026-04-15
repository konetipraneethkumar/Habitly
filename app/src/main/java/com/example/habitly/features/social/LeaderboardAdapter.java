package com.example.habitly.features.social;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.databinding.ItemLeaderboardBinding;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<Friend> friends;
    private final String currentUserName;

    public static class Friend {
        public String name;
        public int exp;
        public Friend(String name, int exp) {
            this.name = name;
            this.exp = exp;
        }
    }

    public LeaderboardAdapter(List<Friend> friends, String currentUserName) {
        this.friends = friends;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        
        // Highlight logic to show it's real
        if (friend.name.equals(currentUserName)) {
            holder.binding.getRoot().setBackgroundColor(0x2200FF00); // Light green tint
            holder.binding.tvName.setText(friend.name + " (You)");
        } else {
            holder.binding.getRoot().setBackgroundColor(0x00000000);
            holder.binding.tvName.setText(friend.name);
        }

        holder.binding.tvRank.setText(String.valueOf(position + 1));
        if (position == 0) holder.binding.tvRank.setText("🥇");
        else if (position == 1) holder.binding.tvRank.setText("🥈");
        else if (position == 2) holder.binding.tvRank.setText("🥉");
        
        holder.binding.tvStreak.setText(friend.exp + " EXP");
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderboardBinding binding;
        public ViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}