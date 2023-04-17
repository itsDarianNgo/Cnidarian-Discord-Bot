package com.darianngo.discordBot.embeds;

import java.awt.Color;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FinalResultEmbed {

	public static MessageEmbed createEmbed(MatchResultDTO matchResult, MatchDTO match) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Final Match Result");
		embedBuilder.setDescription("The final result for match " + matchResult.getMatchId() + " is:");
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamNumber(), false);
		embedBuilder.addField("Score", matchResult.getWinningScore() + "-" + matchResult.getLosingScore(), false);

		StringBuilder team1Members = new StringBuilder();
		for (UserDTO user : match.getTeams().get(0).getMembers()) {
			team1Members.append("<@" + user.getDiscordId() + "> " + Math.round(user.getElo()) + " ("
					+ (Math.round(user.getElo()) - Math.round(user.getRecentEloChange()))
					+ (user.getRecentEloChange() > 0 ? " +" : " ") + " " + Math.round(user.getRecentEloChange())
					+ ")\n");
		}

		StringBuilder team2Members = new StringBuilder();
		for (UserDTO user : match.getTeams().get(1).getMembers()) {
			team2Members.append("<@" + user.getDiscordId() + "> " + Math.round(user.getElo()) + " ("
					+ (Math.round(user.getElo()) - Math.round(user.getRecentEloChange()))
					+ (user.getRecentEloChange() > 0 ? " +" : " ") + " " + Math.round(user.getRecentEloChange())
					+ ")\n");
		}

		embedBuilder.addField("Team 1", team1Members.toString(), true);
		embedBuilder.addField("Team 2", team2Members.toString(), true);

		embedBuilder.setColor(Color.GREEN);
		return embedBuilder.build();
	}
}
