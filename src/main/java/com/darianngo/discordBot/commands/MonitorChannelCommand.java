package com.darianngo.discordBot.commands;

import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MonitorChannelCommand {
    private static Set<String> monitoredChannelIds = new HashSet<>();

    public static void monitorChannel(MessageReceivedEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage("You do not have permission to use this command.").queue();
            return;
        }
        String channelId = event.getChannel().getId();
        if (monitoredChannelIds.contains(channelId)) {
            event.getChannel().sendMessage("This channel is already being monitored.").queue();
        } else {
            monitoredChannelIds.add(channelId);
            event.getChannel().sendMessage("Channel is now being monitored.").queue();
        }
    }

    public static boolean isChannelMonitored(String channelId) {
        return monitoredChannelIds.contains(channelId);
    }

    public static Set<String> getMonitoredChannels() {
        return monitoredChannelIds;
    }
}
