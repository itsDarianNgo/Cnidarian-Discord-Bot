package com.darianngo.discordBot.embeds;

import java.util.List;
import java.util.Random;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class TeamBalancerEmbed {

	public static MessageEmbed createEmbed(List<UserDTO> team1, List<UserDTO> team2, int eloDifference, Long matchId) {
		// Randomize team colors
		Random random = new Random();
		boolean blueTeamFirst = random.nextBoolean();

		// Build the embed
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Queue Popped!");
		embedBuilder.addField("MATCH ID:", matchId.toString(), false);
		embedBuilder.addField("Elo difference:", String.valueOf(eloDifference), false);

		StringBuilder team1Builder = new StringBuilder();
		for (UserDTO userDTO : team1) {
			team1Builder.append("<@").append(userDTO.getDiscordId()).append("> (").append(userDTO.getRanking())
					.append(")\n");
		}

		StringBuilder team2Builder = new StringBuilder();
		for (UserDTO userDTO : team2) {
			team2Builder.append("<@").append(userDTO.getDiscordId()).append("> (").append(userDTO.getRanking())
					.append(")\n");
		}

		if (blueTeamFirst) {
			embedBuilder.addField("🟦 " + "Team 1", team1Builder.toString(), true);
			embedBuilder.addField("🟥 " + "Team 2", team2Builder.toString(), true);
		} else {
			embedBuilder.addField("🟥 " + "Team 1", team1Builder.toString(), true);
			embedBuilder.addField("🟦 " + "Team 2", team2Builder.toString(), true);
		}

		return embedBuilder.build();
	}
}
