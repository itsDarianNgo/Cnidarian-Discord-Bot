package com.darianngo.discordBot.embeds;

import java.util.List;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class TeamBalancerEmbed {

	public static MessageEmbed createEmbed(List<UserDTO> team1, List<UserDTO> team2, int eloDifference, Long matchId) {
		// Calculate average Elos
		int team1EloSum = (int) team1.stream().mapToDouble(UserDTO::getElo).sum();
		int team2EloSum = (int) team2.stream().mapToDouble(UserDTO::getElo).sum();

		float team1AverageElo = (float) team1EloSum / team1.size();
		float team2AverageElo = (float) team2EloSum / team2.size();

		// Calculate the average Elo difference
		float averageEloDifference = Math.abs(team1AverageElo - team2AverageElo);

		// Build the embed
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Queue Popped!");
		embedBuilder.addField("MATCH ID:", matchId.toString(), false);
		embedBuilder.addField("Elo difference:", String.format("%.1f", averageEloDifference), false);

		StringBuilder team1Builder = new StringBuilder();
		for (UserDTO userDTO : team1) {
			team1Builder.append("<@").append(userDTO.getDiscordId()).append("> (").append(Math.round(userDTO.getElo()))
					.append(")\n");
		}

		StringBuilder team2Builder = new StringBuilder();
		for (UserDTO userDTO : team2) {
			team2Builder.append("<@").append(userDTO.getDiscordId()).append("> (").append(Math.round(userDTO.getElo()))
					.append(")\n");
		}

		embedBuilder.addField("🟦 " + "Team 1 (Avg Elo: " + String.format("%.1f", team1AverageElo) + ")",
				team1Builder.toString(), true);
		embedBuilder.addField("🟥 " + "Team 2 (Avg Elo: " + String.format("%.1f", team2AverageElo) + ")",
				team2Builder.toString(), true);

		return embedBuilder.build();
	}

}
