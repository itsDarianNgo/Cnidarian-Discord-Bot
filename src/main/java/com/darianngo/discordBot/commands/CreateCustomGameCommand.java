package com.darianngo.discordBot.commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CreateCustomGameCommand {

	public static final CommandData COMMAND_DATA = new CommandData("create_custom_game", "Create a custom game")
			.addOptions(new OptionData(OptionType.INTEGER, "hour", "Hour to play", true).addChoice("1", 1)
					.addChoice("2", 2).addChoice("3", 3).addChoice("4", 4).addChoice("5", 5).addChoice("6", 6)
					.addChoice("7", 7).addChoice("8", 8).addChoice("9", 9).addChoice("10", 10).addChoice("11", 11)
					.addChoice("12", 12))
			.addOptions(new OptionData(OptionType.STRING, "minute", "Minute to play", true).addChoice(":00", ":00")
					.addChoice(":30", ":30"))
			.addOptions(new OptionData(OptionType.STRING, "am_pm", "AM or PM", true).addChoice("AM", "AM")
					.addChoice("PM", "PM"));

	public void execute(SlashCommandEvent event) {
		int hourToPlay = (int) event.getOption("hour").getAsLong();
		String minuteToPlay = event.getOption("minute").getAsString();
		String amPm = event.getOption("am_pm").getAsString();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Custom Game");
		embedBuilder.setDescription("Time to play: **" + hourToPlay + minuteToPlay + " " + amPm + " CT" + "**");
		embedBuilder.setColor(Color.BLUE);

		event.reply("@everyone").addEmbeds(embedBuilder.build()).queue(interactionHook -> {
			interactionHook.retrieveOriginal().queue(message -> {
				message.addReaction("👍").queue();
				message.addReaction("👎").queue();
			});
		});
	}

}
