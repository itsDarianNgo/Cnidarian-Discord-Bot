package com.darianngo.discordBot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MonitorChannelCommand extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
			String[] messageParts = event.getMessage().getContentDisplay().split("\\s+");
//            Extracts channel ID from second part of message and removes any non-numeric characters
			if (messageParts.length >= 2 && messageParts[0].equalsIgnoreCase("!monitor")) {
				String channelId = messageParts[1].replaceAll("[^0-9]", "");

				if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
					TextChannel channel = event.getGuild().getTextChannelById(channelId);

					if (channel != null) {
						event.getChannel().sendMessage("Now monitoring channel: " + channel.getAsMention() + ".")
								.queue();
					} else {
						event.getChannel().sendMessage("Unable to find the specified channel.").queue();
					}
				} else {
					event.getChannel().sendMessage("You don't have permission to use this command.").queue();
				}
			}
		}
	}
}
