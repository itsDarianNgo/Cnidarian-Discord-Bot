package com.darianngo.discordBot.embeds;

import java.awt.Color;
import java.util.List;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Component
public class LeaderboardEmbed {

	public MessageEmbed generateLeaderboardEmbed(List<UserDTO> leaderboard, int currentPage) {
		int itemsPerPage = 10;
		int startIndex = currentPage * itemsPerPage;
		int endIndex = Math.min(leaderboard.size(), startIndex + itemsPerPage);

		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Leaderboard");
		embed.setColor(Color.CYAN);

		StringBuilder leaderboardBuilder = new StringBuilder();
		for (int i = startIndex; i < endIndex; i++) {
			UserDTO user = leaderboard.get(i);
			int winningStreak = user.getWinningStreak() != null ? user.getWinningStreak() : 0;
			String streakEmoji = winningStreak >= 2 ? "🔥 " + winningStreak : "";
			double elo = user.getElo() != null ? user.getElo() : 0;
			int totalMatches = user.getTotalMatches() != null ? user.getTotalMatches() : 0;
			int wins = user.getWins() != null ? user.getWins() : 0;
			int losses = user.getLosses() != null ? user.getLosses() : 0;

			leaderboardBuilder.append(String.format("**%d. <@%s>** %s\nElo: **%.0f** | %d matches\n🏆 %dW - %dL\n\n",
					i + 1, user.getDiscordId(), streakEmoji, elo, totalMatches, wins, losses));
		}

		embed.setDescription(leaderboardBuilder.toString());
		return embed.build();
	}

}
