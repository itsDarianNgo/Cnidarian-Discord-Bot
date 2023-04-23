package com.darianngo.discordBot.embeds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.dtos.UserDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FinalResultEmbed {

	public static MessageEmbed createEmbed(MatchResultDTO matchResult, MatchDTO match) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Final Match Result");
		embedBuilder.setDescription("The final result for match " + matchResult.getMatchId() + " is:");
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamNumber(), false);
		embedBuilder.addField("Score", matchResult.getWinningScore() + "- " + matchResult.getLosingScore(), false);

		// Sort teams based on their IDs
		List<TeamDTO> sortedTeams = new ArrayList<>(match.getTeams());
		sortedTeams.sort(Comparator.comparing(TeamDTO::getId));

		StringBuilder[] teamMembers = { new StringBuilder(), new StringBuilder() };
		for (int i = 0; i < sortedTeams.size(); i++) {
			for (UserDTO user : sortedTeams.get(i).getMembers()) {
				teamMembers[i].append("<@" + user.getDiscordId() + "> " + Math.round(user.getElo()) + " ("
						+ (Math.round(user.getElo()) - Math.round(user.getRecentEloChange()))
						+ (user.getRecentEloChange() > 0 ? " +" : " ") + " " + Math.round(user.getRecentEloChange())
						+ ")\n");
			}
		}

		embedBuilder.addField("Team 1", teamMembers[0].toString(), true);
		embedBuilder.addField("Team 2", teamMembers[1].toString(), true);

		embedBuilder.setColor(Color.GREEN);
		return embedBuilder.build();
	}
}
