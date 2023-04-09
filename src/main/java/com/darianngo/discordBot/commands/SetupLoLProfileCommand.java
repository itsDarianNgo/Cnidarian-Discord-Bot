package com.darianngo.discordBot.commands;

import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.embeds.LoLProfileEmbed;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SetupLoLProfileCommand {
	public static final CommandData COMMAND_DATA = new CommandData("setup_lol_profile",
			"Setup your League of Legends profile")
			.addOptions(new OptionData(OptionType.STRING, "summoner_name", "Your Summoner name", true))
			.addOptions(new OptionData(OptionType.STRING, "region", "Your region", true)
				.addChoice("Europe West", "EUW")
				.addChoice("North America", "NA"))
			.addOptions(new OptionData(OptionType.STRING, "main_role", "Your main role", true)
				.addChoice("TOP", "TOP")
				.addChoice("JUNGLE", "JUNGLE")
				.addChoice("MID", "MID")
				.addChoice("BOT", "BOT")
				.addChoice("SUPPORT", "SUPPORT"))
			.addOptions(new OptionData(OptionType.STRING, "secondary_role", "Your secondary role", true)
				.addChoice("TOP", "TOP")
				.addChoice("JUNGLE", "JUNGLE")
				.addChoice("MID", "MID")
				.addChoice("BOT", "BOT")
				.addChoice("SUPPORT", "SUPPORT"));

	private final UserService userService;

	public SetupLoLProfileCommand(UserService userService) {
		this.userService = userService;
	}

	public void execute(SlashCommandEvent event) {
		String discordId = event.getUser().getId();
		String discordName = event.getUser().getAsTag();
		String summonerName = event.getOption("summoner_name").getAsString();
		String region = event.getOption("region").getAsString().toUpperCase();
		String mainRole = event.getOption("main_role").getAsString().toUpperCase();
		String secondaryRole = event.getOption("secondary_role").getAsString().toUpperCase();

		if (mainRole.equalsIgnoreCase(secondaryRole)) {
			event.reply("Your main and secondary roles cannot be the same").queue();
			return;
		}

		UserDTO existingUser = userService.getUserById(discordId);
	    if (existingUser == null) {
	        UserDTO newUser = new UserDTO(discordId, discordName, summonerName, null, mainRole, secondaryRole, region);
	        userService.createUser(newUser);
	        event.reply("Your League of Legends profile has been set up.").queue();
	        event.getChannel().sendMessage(LoLProfileEmbed.createProfileEmbed(newUser, "<@" + discordId + ">")).queue();
	    } else {
	        existingUser.setSummonerName(summonerName);
	        existingUser.setMainRole(mainRole);
	        existingUser.setSecondaryRole(secondaryRole);
	        existingUser.setRegion(region);
	        userService.updateUser(existingUser);
	        event.reply("Your League of Legends profile has been updated.").queue();
	        event.getChannel().sendMessage(LoLProfileEmbed.createProfileEmbed(existingUser, "<@" + discordId + ">")).queue();
	    }
	}

}
