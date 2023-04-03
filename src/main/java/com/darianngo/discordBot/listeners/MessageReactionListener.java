package com.darianngo.discordBot.listeners;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.CreateReactionMessageCommand;
import com.darianngo.discordBot.commands.MonitorChannelCommand;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.startsWith("!monitor")) {
            MonitorChannelCommand.monitorChannel(event);
        } else if (messageContent.startsWith("!createReactionMessage")) {
            String content = messageContent.substring("!createReactionMessage".length()).trim();
            CreateReactionMessageCommand.createReactionMessage(event, content);
        }
    }
}
