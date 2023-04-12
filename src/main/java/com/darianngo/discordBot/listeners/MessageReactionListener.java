package com.darianngo.discordBot.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Component;

import com.darianngo.discordBot.commands.MonitorChannelCommand;
import com.darianngo.discordBot.commands.SetUserRankingCommand;
import com.darianngo.discordBot.commands.SetUserRolesCommand;
import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.MatchService;
import com.darianngo.discordBot.services.TeamBalancerService;
import com.darianngo.discordBot.services.UserService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
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
	private final MatchService matchService;
	@Autowired
	private DiscordChannelConfig discordChannelConfig;

	public MessageReactionListener(UserService userService, TeamBalancerService teamBalancerService,
			MatchService matchService) {
		this.userService = userService;
		this.teamBalancerService = teamBalancerService;
		this.matchService = matchService;
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
				if (realUsersCount == 4) {
					List<String> reactions = Collections.singletonList("👍");
					List<UserDTO> usersReacted = new ArrayList<>();

					CountDownLatch latch = new CountDownLatch(message.getReactions().size());

					for (MessageReaction reaction : message.getReactions()) {
						if (reaction.getReactionEmote().getEmoji().equals("👍")) {
							reaction.retrieveUsers().queue(users -> {
								for (User user : users) {
									if (!user.isBot()) {
										UserDTO userDTO = userService.getUserById(user.getId());
										usersReacted.add(userDTO);
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

					sendMatchApprovalRequest(event, usersReacted, approvalRequest -> waitForApproval(event,
							approvalRequest.getId(), reactions, usersReacted));

				}

			});
		}
	}

	private void createPoll(MessageReactionAddEvent event, List<UserDTO> usersReacted, MatchDTO match,
			Consumer<MatchDTO> callback) {
		Long matchId = match.getId();
		MessageEmbed pollEmbed = createPollEmbed(matchId);
		event.getChannel().sendMessage(pollEmbed).queue(message -> {
			message.addReaction("1️⃣").queue();
			message.addReaction("2️⃣").queue();
			message.addReaction("🔥").queue();
			message.addReaction("🌊").queue();
			callback.accept(match);
		});
	}

	private MessageEmbed createPollEmbed(Long matchId) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Match Result Poll");
		embedBuilder.setDescription("React with 1️⃣ if Team 1 won and 2️⃣ if Team 2 won");
		embedBuilder.addField("Match ID", matchId.toString(), false);
		return embedBuilder.build();
	}

	private void waitForPollResults(MessageReactionAddEvent event, Long matchId, List<UserDTO> usersReacted) {
		event.getJDA().addEventListener(new ListenerAdapter() {
			@Override
			public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent pollEvent) {
				if (!event.getUser().isBot() && isUserInMatch(usersReacted, pollEvent.getUserId())) {
					processPollResults(pollEvent, matchId, usersReacted);
				}
			}
		});
	}

	private void processPollResults(MessageReactionAddEvent event, Long matchId, List<UserDTO> usersReacted) {
		List<MessageReaction> reactions = event.getTextChannel().retrieveMessageById(event.getMessageId()).complete()
				.getReactions();

		AtomicInteger team1Votes = new AtomicInteger();
		AtomicInteger team2Votes = new AtomicInteger();
		AtomicReference<String> finalScore = new AtomicReference<>("");

		AtomicBoolean team1Reacted = new AtomicBoolean(false);
		AtomicBoolean team2Reacted = new AtomicBoolean(false);
		AtomicBoolean scoreReacted = new AtomicBoolean(false);

		event.getJDA().addEventListener(new ListenerAdapter() {
			@Override
			public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
				if (event.getUser().isBot()) {
					return;
				}

				String emoji = event.getReactionEmote().getEmoji();
				if (emoji.equals("1️⃣")) {
					team1Votes.incrementAndGet();
					team1Reacted.set(true);
				} else if (emoji.equals("2️⃣")) {
					team2Votes.incrementAndGet();
					team2Reacted.set(true);
				} else if (emoji.equals("🔥")) {
					finalScore.set("2-0");
					scoreReacted.set(true);
				} else if (emoji.equals("🌊")) {
					finalScore.set("2-1");
					scoreReacted.set(true);
				}

				Optional<String> winningTeam = Optional.empty();
				if (team1Votes.get() >= 1 && team1Reacted.get()) {
					winningTeam = Optional.of("Team 1");
				} else if (team2Votes.get() >= 1 && team2Reacted.get()) {
					winningTeam = Optional.of("Team 2");
				}

				if (winningTeam.isPresent() && scoreReacted.get() && !finalScore.get().isEmpty()) {
					try {
						updateMatchResults(matchId, winningTeam.get(), finalScore.get());
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					event.getChannel().sendMessage("Poll has ended. " + winningTeam.get()
							+ " won the match with a score of " + finalScore.get()).queue();

					Message message = event.getTextChannel().retrieveMessageById(event.getMessageId()).complete();
					for (MessageReaction reaction : message.getReactions()) {
						reaction.removeReaction().queue();
					}
					message.clearReactions().queue();

					event.getJDA().removeEventListener(this); // Remove the listener after processing the poll
				}
			}
		});
	}

	private void updateMatchResults(Long matchId, String winningTeam, String finalScore) throws NotFoundException {
		System.out.println("Updating match results for matchId: " + matchId);
		MatchDTO match = matchService.getMatchById(matchId);
		match.setWinningTeam(winningTeam);
		match.setFinalScore(finalScore);
		matchService.updateMatch(match);
		System.out.println("Updated match results for matchId: " + matchId + ", winningTeam: " + winningTeam
				+ ", finalScore: " + finalScore);
	}

	private boolean isUserInMatch(List<UserDTO> usersReacted, String userId) {
		return usersReacted.stream().anyMatch(user -> user.getDiscordId().equals(userId));
	}

	private void sendMatchApprovalRequest(MessageReactionAddEvent event, List<UserDTO> usersReacted,
			Consumer<Message> callback) {
		String approvalChannelId = discordChannelConfig.getApprovalChannelId();

		MessageEmbed approvalEmbed = createMatchApprovalEmbed(usersReacted);
		event.getJDA().getTextChannelById(approvalChannelId).sendMessage(approvalEmbed).queue(message -> {
			message.addReaction("✅").queue();
			message.addReaction("❌").queue();
			callback.accept(message);
		});
	}

	private MessageEmbed createMatchApprovalEmbed(List<UserDTO> usersReacted) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Match Approval Request");
		embedBuilder.setDescription("React with ✅ to approve or ❌ to reject");

		StringBuilder usersBuilder = new StringBuilder();
		for (UserDTO userDTO : usersReacted) {
			usersBuilder.append("<@").append(userDTO.getDiscordId()).append("> (").append(userDTO.getRanking())
					.append(")\n");
		}
		embedBuilder.addField("Users:", usersBuilder.toString(), false);

		return embedBuilder.build();
	}

	private void waitForApproval(MessageReactionAddEvent event, String approvalMessageId, List<String> reactions,
			List<UserDTO> usersReacted) {

		event.getJDA().addEventListener(new ListenerAdapter() {
			@Override
			public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent approvalEvent) {
				if (!approvalEvent.getMessageId().equals(approvalMessageId)) {
					return;
				}

				if (!approvalEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
					return;
				}

				if (approvalEvent.getReactionEmote().getEmoji().equals("✅")) {
					// Create a match when the approval reaction is "✅"
					MatchDTO match = matchService.createMatch(new MatchDTO());
					Long matchId = match.getId();

					MessageEmbed embed = teamBalancerService.balanceTeams(reactions, usersReacted, matchId);
					event.getChannel().sendMessage(embed).queue();

					// Create poll after a match has been created
					createPoll(event, usersReacted, match, matchResult -> {
						waitForPollResults(event, matchId, usersReacted);
					});
				} else if (approvalEvent.getReactionEmote().getEmoji().equals("❌")) {
					event.getChannel().sendMessage("Match creation request rejected.").queue();
				}

				event.getJDA().removeEventListener(this);
			}
		});
	}

}
