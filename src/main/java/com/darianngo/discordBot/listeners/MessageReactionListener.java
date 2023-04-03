package com.darianngo.discordBot.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.CreateReactionMessageCommand;
import com.darianngo.discordBot.commands.MonitorChannelCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

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

//		When "👍" reaction is added, checks if message was created by bot and if there are 11 "👍" reactions. 
//		If both true, sends message with list of users
	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
//		Checks if the added reaction is a "👍"
		if (event.getReactionEmote().getName().equals("👍")) {
//			Retrieves the message that the reaction was added to, using the channel and message ID from the MessageReactionAddEvent
			RestAction<Message> messageRetrievalAction = event.getChannel().retrieveMessageById(event.getMessageId());
//			Executes asynchronously
			messageRetrievalAction.queue(message -> {
				if (message.getAuthor().isBot()) {
					long thumbsUpCount = message.getReactions().stream()
//							Filters the reactions to only include those with the "👍" emoji
							.filter(reaction -> reaction.getReactionEmote().getName().equals("👍"))
//							Maps stream of filtered reactions to a stream of integers by extracting the count of each "👍" reaction
							.mapToInt(MessageReaction::getCount).sum();

					if (thumbsUpCount == 11) {
//	            		Store the names of users who reacted with "👍"
						List<String> userList = new ArrayList<>();
//	            		Retrieves the users who reacted with "👍" and queues the result to be executed asynchronously	
						event.getReaction().retrieveUsers().queue(users -> {
//	                		Filters out bot users from the retrieved users, iterates through the remaining users, and adds their Discord tags to the userList
							users.stream().filter(user -> !user.isBot()).forEach(user -> userList.add(user.getAsTag()));
//	                		Joins the names of the users in the userList into a single string, separated by commas	
							String userListString = String.join(", ", userList);
//	                		Sends a message to the channel containing the list of users who reacted with "👍"	
							event.getChannel().sendMessage("Users who reacted with 👍: " + userListString).queue();
						});
					}
				}
			});
		}
	}
}
