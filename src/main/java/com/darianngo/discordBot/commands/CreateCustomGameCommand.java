package com.darianngo.discordBot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CreateCustomGameCommand {

    public static void createCustomGame(MessageReceivedEvent event, String content) {
        String channelId = event.getChannel().getId();
        if (MonitorChannelCommand.isChannelMonitored(channelId)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Custom Game");
            builder.setDescription(content);
            event.getChannel().sendMessage(builder.build()).queue(message -> {
                // Add reactions to the message here:
                message.addReaction("👍").queue();
                message.addReaction("👎").queue();
            });
        } else {
            event.getChannel().sendMessage("This channel is not being monitored. Use !monitor to start monitoring.").queue();
        }
    }
}
