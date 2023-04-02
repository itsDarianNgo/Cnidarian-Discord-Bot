package com.darianngo.discordBot.commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CreateReactionMessageCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
        	
            String[] messageParts = event.getMessage().getContentDisplay().split("\\s+");
            
            if (messageParts.length >= 2 && messageParts[0].equalsIgnoreCase("!createReactionMessage")) {
                String messageContent = String.join(" ", java.util.Arrays.copyOfRange(messageParts, 1, messageParts.length));

                event.getChannel().sendMessage(messageContent).queue((Message message) -> {
                    // Add reactions to the message here:
                    message.addReaction("👍").queue();
                    message.addReaction("👎").queue();
                    
                });
            }
        }
    }
}
