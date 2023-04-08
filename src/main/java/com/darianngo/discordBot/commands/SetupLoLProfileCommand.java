package com.darianngo.discordBot.commands;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SetupLoLProfileCommand {
	public static final CommandData COMMAND_DATA = new CommandData("setup_lol_profile",
			"Setup your League of Legends profile")
			.addOptions(new OptionData(OptionType.STRING, "summoner_name", "Your Summoner name", true))
			.addOptions(new OptionData(OptionType.STRING, "region", "Your region", true))
			.addOptions(new OptionData(OptionType.STRING, "main_role", "Your main role", true))
			.addOptions(new OptionData(OptionType.STRING, "secondary_role", "Your secondary role", true));

	private final UserService userService;

	public SetupLoLProfileCommand(UserService userService) {
		this.userService = userService;
	}

	public void execute(SlashCommandEvent event) {
		String discordId = event.getUser().getId();
		String discordName = event.getUser().getAsTag().toLowerCase();
		String summonerName = event.getOption("summoner_name").getAsString().toLowerCase();
		String region = event.getOption("region").getAsString().toLowerCase();
		String mainRole = event.getOption("main_role").getAsString().toLowerCase();
		String secondaryRole = event.getOption("secondary_role").getAsString().toLowerCase();

		UserDTO existingUser = userService.getUserById(discordId);
		if (existingUser == null) {
			UserDTO newUser = new UserDTO(discordId, discordName, summonerName, null, mainRole, secondaryRole, region);
			userService.createUser(newUser);
			event.reply("Your League of Legends profile has been set up.").setEphemeral(true).queue();
		} else {
			existingUser.setSummonerName(summonerName);
			existingUser.setMainRole(mainRole);
			existingUser.setSecondaryRole(secondaryRole);
			existingUser.setRegion(region);
			userService.updateUser(existingUser);
			event.reply("Your League of Legends profile has been updated.").setEphemeral(true).queue();
		}
	}

}
