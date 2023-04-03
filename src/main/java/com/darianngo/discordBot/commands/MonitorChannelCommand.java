package com.darianngo.discordBot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.Set;

public class MonitorChannelCommand {
    private static Set<String> monitoredChannelIds = new HashSet<>();

    public static void monitorChannel(MessageReceivedEvent event) {
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
