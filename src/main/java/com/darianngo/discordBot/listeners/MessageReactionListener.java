package com.darianngo.discordBot.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.MonitorChannelCommand;
import com.darianngo.discordBot.commands.SetUserRankingCommand;
import com.darianngo.discordBot.commands.SetUserRolesCommand;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.TeamBalancerService;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageReactionListener extends ListenerAdapter {

	private final UserService userService;
	private final TeamBalancerService teamBalancerService;

	public MessageReactionListener(UserService userService, TeamBalancerService teamBalancerService) {
		this.userService = userService;
		this.teamBalancerService = teamBalancerService;
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}

		String messageContent = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
		if (messageContent.startsWith("!monitor")) {
			MonitorChannelCommand.monitorChannel(event);
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
						.filter(r -> r.getReactionEmote().getEmoji().equals("👍")).mapToInt(MessageReaction::getCount)
						.sum();
				// Subtract 1 from the reactionCount to exclude the bot
				int realUsersCount = reactionCount - 1;

				// Check if the number of real users who reacted is 10 or less
				if (realUsersCount == 2) {
					List<String> reactions = Collections.singletonList("👍");
					List<UserDTO> usersReacted = new ArrayList<>();
					CountDownLatch latch = new CountDownLatch(message.getReactions().size());

					for (MessageReaction reaction : message.getReactions()) {
						if (reaction.getReactionEmote().getEmoji().equals("👍")) {
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
						} else {
							latch.countDown();
						}
					}

					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (usersReacted.size() == 2) {
					    MessageEmbed embed = teamBalancerService.balanceTeams(reactions, usersReacted);
					    event.getChannel().sendMessage(embed).queue();
					} else {
						System.out.println(usersReacted);
						event.getChannel().sendMessage("Not enough users with ranking information.").queue();
					}
				}
			});
		}
	}
}
