package com.darianngo.discordBot.listeners;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.SetupLoLProfileCommand;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class SlashCommandListener extends ListenerAdapter {
	private final UserService userService;

	public SlashCommandListener(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		if (event.getName().equals("setup_lol_profile")) {
			SetupLoLProfileCommand command = new SetupLoLProfileCommand(userService);
			command.execute(event);
		} else {
			event.reply("Unknown command.").queue();
		}
	}
}
