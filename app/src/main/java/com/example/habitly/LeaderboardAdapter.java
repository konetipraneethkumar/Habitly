package com.example.habitly;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.databinding.ItemLeaderboardBinding;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<Friend> friends;

    public static class Friend {
        String name;
        int streak;
        public Friend(String name, int streak) {
            this.name = name;
            this.streak = streak;
        }
    }

    public LeaderboardAdapter(List<Friend> friends) {
        this.friends = friends;
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
        holder.binding.tvRank.setText(String.valueOf(position + 1));
        if (position == 0) holder.binding.tvRank.setText("🥇");
        else if (position == 1) holder.binding.tvRank.setText("🥈");
        else if (position == 2) holder.binding.tvRank.setText("🥉");
        
        holder.binding.tvName.setText(friend.name);
        holder.binding.tvStreak.setText("🔥 " + friend.streak);
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