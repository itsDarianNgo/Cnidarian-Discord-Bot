package com.darianngo.discordBot.embeds;

import java.awt.Color;
import java.text.DecimalFormat;

import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class LoLProfileEmbed {

	public static MessageEmbed createProfileEmbed(UserDTO user, String discordName) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		DecimalFormat df = new DecimalFormat("#");

		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setTitle("League of Legends Profile");
		embedBuilder.addField("Discord Name", discordName, true);
		embedBuilder.addField("Summoner Name", user.getSummonerName(), true);
		embedBuilder.addField("Server", user.getRegion(), true);
		embedBuilder.addField("Roles", user.getMainRole() + " / " + user.getSecondaryRole(), true);
		String elo = user.getElo() != null ? df.format(user.getElo()) : "unranked";
		embedBuilder.addField("ELO", elo, true);

		return embedBuilder.build();
	}
}
