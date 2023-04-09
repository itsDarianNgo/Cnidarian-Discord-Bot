package com.darianngo.discordBot.commands;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.embeds.LoLProfileEmbed;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ShowLoLProfileCommand {
	public static final CommandData COMMAND_DATA = new CommandData("show_lol_profile",
			"Show your League of Legends profile");

	private final UserService userService;

	public ShowLoLProfileCommand(UserService userService) {
		this.userService = userService;
	}

	public void execute(SlashCommandEvent event) {
		String discordId = event.getUser().getId();

		UserDTO existingUser = userService.getUserById(discordId);
		if (existingUser != null) {
			event.replyEmbeds(LoLProfileEmbed.createProfileEmbed(existingUser, "<@" + discordId + ">")).queue();
		} else {
			event.reply("You haven't set up your League of Legends profile yet.").setEphemeral(true).queue();
		}
	}

}