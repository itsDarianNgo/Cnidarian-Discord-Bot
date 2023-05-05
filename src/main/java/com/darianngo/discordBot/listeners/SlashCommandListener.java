package com.darianngo.discordBot.listeners;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.CreateCustomGameCommand;
import com.darianngo.discordBot.commands.LeaderboardCommand;
import com.darianngo.discordBot.commands.SetupLoLProfileCommand;
import com.darianngo.discordBot.commands.ShowLoLProfileCommand; // Add the import
import com.darianngo.discordBot.embeds.LeaderboardEmbed;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class SlashCommandListener extends ListenerAdapter {
	private final UserService userService;
	private final SetupLoLProfileCommand setupLoLProfileCommand;
	private final ShowLoLProfileCommand showLoLProfileCommand;
	private final CreateCustomGameCommand createCustomGameCommand;
	private final LeaderboardCommand leaderboardCommand;

	public SlashCommandListener(UserService userService) {
		LeaderboardEmbed leaderboardEmbed = new LeaderboardEmbed();
		this.userService = userService;

		
		this.setupLoLProfileCommand = new SetupLoLProfileCommand(userService);
		this.showLoLProfileCommand = new ShowLoLProfileCommand(userService);
		this.createCustomGameCommand = new CreateCustomGameCommand();
		this.leaderboardCommand = new LeaderboardCommand(userService, leaderboardEmbed);
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		if (event.getName().equals("setup_lol_profile")) {
			setupLoLProfileCommand.execute(event);
		} else if (event.getName().equals("show_lol_profile")) {
			showLoLProfileCommand.execute(event);
		} else if (event.getName().equals("create_custom_game")) {
			createCustomGameCommand.execute(event);
		} else if (event.getName().equals("leaderboard")) {
			leaderboardCommand.execute(event);
		} else {
			event.reply("Unknown command.").queue();
		}
	}
}
