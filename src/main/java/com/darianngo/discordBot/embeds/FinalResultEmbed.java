package com.darianngo.discordBot.embeds;

import java.awt.Color;

import com.darianngo.discordBot.dtos.MatchResultDTO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FinalResultEmbed {

	public static MessageEmbed createEmbed(MatchResultDTO matchResult) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Final Match Result");
		embedBuilder.setDescription("The final result for match " + matchResult.getMatchId() + " is:");
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamId(), false);
		embedBuilder.addField("Score", matchResult.getWinningScore() + "-" + matchResult.getLosingScore(), false);
		embedBuilder.setColor(Color.GREEN);

		return embedBuilder.build();
	}
}
