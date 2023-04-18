package com.darianngo.discordBot.embeds;

import java.util.List;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MissingEloEmbed {
	public static MessageEmbed createEmbed(List<UserDTO> usersWithMissingElo) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Users with missing Elo");
		embedBuilder.setDescription("Please set up your LoL profile using \"/setup_lol_profile\".");

		StringBuilder usersList = new StringBuilder();
		for (UserDTO user : usersWithMissingElo) {
			usersList.append("<@").append(user.getDiscordId()).append(">\n");
		}

		embedBuilder.addField("Users:", usersList.toString(), false);
		embedBuilder.setColor(0xff3923);

		return embedBuilder.build();
	}
}
