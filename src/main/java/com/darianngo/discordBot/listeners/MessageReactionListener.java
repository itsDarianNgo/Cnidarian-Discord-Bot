package com.darianngo.discordBot.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.CreateCustomGameCommand;
import com.darianngo.discordBot.commands.MonitorChannelCommand;
import com.darianngo.discordBot.commands.SetUserRankingCommand;
import com.darianngo.discordBot.commands.SetUserRolesCommand;
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
	private static final List<String> validRoles = Arrays.asList("top", "jungle", "mid", "adc", "support");

	public MessageReactionListener(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}

		String messageContent = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
		if (messageContent.startsWith("!monitor")) {
			MonitorChannelCommand.monitorChannel(event);
		} else if (messageContent.startsWith("!createcustomgame")) {
			String content = messageContent.substring("!createcustomgame".length()).trim();
			CreateCustomGameCommand.createCustomGame(event, content);
		} else if (messageContent.startsWith("!setranking")) {
			String content = messageContent.substring("!setranking".length()).trim();
			SetUserRankingCommand.setUserRanking(event, userService, content);
		} else if (messageContent.startsWith("!setroles")) {
			String content = messageContent.substring("!setroles".length()).trim();
			SetUserRolesCommand.setUserRoles(event, userService, content);
		}
	}

	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}

		String channelId = event.getChannel().getId();

	    if (MonitorChannelCommand.isChannelMonitored(channelId)) {
	        event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
	            int reactionCount = message.getReactions().stream()
	                    .filter(r -> r.getReactionEmote().getEmoji().equals("👍"))
	                    .mapToInt(MessageReaction::getCount).sum();

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
		            if (!user.isBot() && reaction.getReactionEmote().getEmoji().equals("👍")) {
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
			// Sort users based on their rank
			Collections.sort(usersReacted, Comparator.comparingInt(UserDTO::getRanking).reversed());

			List<UserDTO> team1 = new ArrayList<>();
			List<UserDTO> team2 = new ArrayList<>();

			// Distribute players based on their roles and ranking
			for (String role : validRoles) {
				for (UserDTO userDTO : usersReacted) {
					if (userDTO.getPrimaryRole().equals(role) || userDTO.getSecondaryRole().equals(role)
							|| userDTO.getTertiaryRole().equals(role)) {
						int team1RoleCount = (int) team1
								.stream().filter(u -> u.getPrimaryRole().equals(role)
										|| u.getSecondaryRole().equals(role) || u.getTertiaryRole().equals(role))
								.count();
						int team2RoleCount = (int) team2
								.stream().filter(u -> u.getPrimaryRole().equals(role)
										|| u.getSecondaryRole().equals(role) || u.getTertiaryRole().equals(role))
								.count();

						if (team1RoleCount < team2RoleCount) {
							if (!team1.contains(userDTO)) {
								team1.add(userDTO);
							}
						} else {
							if (!team2.contains(userDTO)) {
								team2.add(userDTO);
							}
						}
					}
				}
			}

			// Fill the remaining spots
			for (UserDTO userDTO : usersReacted) {
				if (!team1.contains(userDTO) && !team2.contains(userDTO)) {
					if (team1.size() < team2.size()) {
						team1.add(userDTO);
					} else {
						team2.add(userDTO);
					}
				}
			}

			// Build and send the response
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
			System.out.println(usersReacted);
			event.getChannel().sendMessage("Not enough users with ranking information.").queue();
		}
	}
}
