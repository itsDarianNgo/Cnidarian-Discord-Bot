package com.darianngo.discordBot.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.CreateReactionMessageCommand;
import com.darianngo.discordBot.commands.MonitorChannelCommand;
import com.darianngo.discordBot.commands.SetUserRankingCommand;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageReactionListener extends ListenerAdapter {

	private final UserService userService;

	public MessageReactionListener(UserService userService) {
		this.userService = userService;
	}

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
		} else if (messageContent.startsWith("!setRanking")) {
			String content = messageContent.substring("!setRanking".length()).trim();
			SetUserRankingCommand.setUserRanking(event, userService, content);
		}
	}

//		When "👍" reaction is added, checks if message was created by bot and if there are 11 "👍" reactions. 
//		If both true, sends message with list of users
	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}

		String channelId = event.getChannel().getId();

		if (MonitorChannelCommand.isChannelMonitored(channelId)) {
	        event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
	            int reactionCount = message.getReactions().stream().mapToInt(MessageReaction::getCount).sum();

	            // Subtract 1 from the reactionCount to exclude the bot
	            int realUsersCount = reactionCount - 1;

	            // Check if the number of real users who reacted is 10 or less
	            if (realUsersCount == 10) {
	                balanceTeams(event, message);
	            }
	        });
	    }
	}

	private void balanceTeams(MessageReactionAddEvent event, Message message) {
		List<UserDTO> usersReacted = Collections.synchronizedList(new ArrayList<>());
		CountDownLatch latch = new CountDownLatch(message.getReactions().size());

		for (MessageReaction reaction : message.getReactions()) {
			reaction.retrieveUsers().queue(users -> {
				for (User user : users) {
					if (!user.isBot()) {
						UserDTO userDTO = userService.getUserById(user.getId());
						if (userDTO != null) {
							usersReacted.add(userDTO);
						}
					}
				}
				latch.countDown();
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (usersReacted.size() == 10) {
			Collections.sort(usersReacted, Comparator.comparingInt(UserDTO::getRanking).reversed());
			List<UserDTO> team1 = new ArrayList<>();
			List<UserDTO> team2 = new ArrayList<>();

			for (UserDTO userDTO : usersReacted) {
				int team1RankingSum = team1.stream().mapToInt(UserDTO::getRanking).sum();
				int team2RankingSum = team2.stream().mapToInt(UserDTO::getRanking).sum();

				if (team1RankingSum <= team2RankingSum) {
					team1.add(userDTO);
				} else {
					team2.add(userDTO);
				}
			}

			StringBuilder response = new StringBuilder("Balanced teams:\n\nTeam 1:\n");
			for (UserDTO userDTO : team1) {
				response.append(userDTO.getName()).append(" (").append(userDTO.getRanking()).append(")\n");
			}
			response.append("\nTeam 2:\n");
			for (UserDTO userDTO : team2) {
				response.append(userDTO.getName()).append(" (").append(userDTO.getRanking()).append(")\n");
			}

			event.getChannel().sendMessage(response.toString()).queue();
		} else {
			event.getChannel().sendMessage("Not enough users with ranking information.").queue();
		}
	}
}
