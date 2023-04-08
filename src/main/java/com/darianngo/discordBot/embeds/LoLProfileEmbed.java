package com.darianngo.discordBot.embeds;

import java.awt.Color;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LoLProfileEmbed {

	public static MessageEmbed createProfileEmbed(UserDTO user, String discordName) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setTitle("League of Legends Profile");
		embedBuilder.addField("Discord Name", discordName, true);
		embedBuilder.addField("Summoner Name", user.getSummonerName(), true);
		embedBuilder.addField("Server", user.getRegion(), true);
		embedBuilder.addField("Roles", user.getMainRole() + " / " + user.getSecondaryRole(), true);
		embedBuilder.addField("ELO", user.getRanking() != null ? Integer.toString(user.getRanking()) : "unranked",
				true);

		return embedBuilder.build();
	}
}
