package com.darianngo.discordBot.commands;

import java.util.List;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.embeds.LeaderboardEmbed;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

public class LeaderboardCommand {
	public static final CommandData COMMAND_DATA = new CommandData("leaderboard",
			"Displays the leaderboard sorted by Elo");
	private final LeaderboardEmbed leaderboardEmbed;

	private final UserService userService;

	public LeaderboardCommand(UserService userService, LeaderboardEmbed leaderboardEmbed) {
		this.userService = userService;
		this.leaderboardEmbed = leaderboardEmbed;
	}

	public void execute(SlashCommandEvent event) {
		List<UserDTO> leaderboard = userService.getLeaderboard();

		MessageEmbed embed = leaderboardEmbed.generateLeaderboardEmbed(leaderboard, 0);
		Button prevButton = Button.primary("prev_page:", "Previous").asDisabled();
		Button nextButton = Button.primary("next_page:", "Next");
		ActionRow actionRow = ActionRow.of(prevButton, nextButton);

		event.replyEmbeds(embed).addActionRows(actionRow).setEphemeral(false).queue(interactionHook -> {
			interactionHook.retrieveOriginal().queue(message -> {
				// Register a listener in the ButtonClickListener class
				// The listener should handle the button clicks for pagination
			});
		});
	}
}
