package com.darianngo.discordBot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserVoteDTO;
import com.darianngo.discordBot.services.MatchService;
import com.darianngo.discordBot.services.VotingService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

@org.springframework.stereotype.Component
public class ButtonClickListener extends ListenerAdapter {
	private final MatchService matchService;
	private final VotingService votingService;
	private Map<String, UserVoteDTO> userVotes = new ConcurrentHashMap<>();
	private final Map<String, MatchResultDTO> matchResults = new HashMap<>();
	private final Map<String, AtomicInteger> usersFullyVoted = new ConcurrentHashMap<>();
	private Map<String, UserVoteDTO> adminUserVotes = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	public ButtonClickListener(MatchService matchService, VotingService votingService) {
		this.matchService = matchService;
		this.votingService = votingService;
	}

	@Autowired
	private DiscordChannelConfig discordChannelConfig;

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		String[] buttonIdParts = event.getComponentId().split("_");
		String action = buttonIdParts[0];

		if ("end".equals(action)) {
			handleEndButtonClick(event);
		} else if ("vote".equals(action)) {
			handleVoteButtonClick(event, buttonIdParts);
		} else if ("approve".equals(action)) {
			handleApproveButtonClick(event, buttonIdParts);
		} else if ("reject".equals(action)) {
			handleRejectButtonClick(event, buttonIdParts);
		} else if ("admin".equals(action)) {
			handleAdminVoteButtonClick(event, buttonIdParts);
		}
		event.deferEdit().queue(); // Acknowledge the event
	}

	private void handleEndButtonClick(ButtonClickEvent event) {
		String componentId = event.getComponentId();
		if (!componentId.startsWith("end_match_")) {
			return; // Ignore the button click if the component ID does not start with "end_match_".
		}

		String matchId = componentId.substring("end_match_".length());
		if (matchResults.containsKey(matchId)) {
			// Display an error message and return if the match has already ended.
			event.reply("This match has already ended. Please wait for the results.").setEphemeral(true).queue();
			return;
		}

		List<UserDTO> usersReacted = matchService.getUsersReactedForMatch(Long.parseLong(matchId));

		for (UserDTO userDTO : usersReacted) {
			User user = event.getJDA().retrieveUserById(userDTO.getDiscordId()).complete();
			votingService.sendVotingDM(user, matchId);
		}
		// Start 5-minute countdown
		votingService.startVoteCountdown(event, matchId);

		// Disable the "End Match" button
		disableButton(event.getMessage(), "end_match_" + matchId);
	}

	private void handleApproveButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String matchId = buttonIdParts[1];
		UserVoteDTO userVote = userVotes.get(event.getUser().getId() + "_" + matchId);
		MatchResultDTO matchResult = new MatchResultDTO();
		matchResult.setMatchId(Long.parseLong(matchId));
		matchResult.setWinningTeamId(userVote.getTeamVote());
		matchResult.setWinningScore(userVote.getWinningScore());
		matchResult.setLosingScore(userVote.getLosingScore());

		matchService.saveMatchResult(matchResult); // Save the results to the database
		displayFinalResult(event.getChannel(), matchResult); // Display the final results

		// Delete the message
		event.getMessage().delete().queue();
	}

	private void handleRejectButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String matchId = buttonIdParts[1];
		MatchResultDTO matchResult = matchResults.get(matchId);

		User user = event.getUser();
		votingService.sendAdminVoting(user, matchId, matchResult);

		// Delete the message
		event.getMessage().delete().queue();
	}

	private void handleAdminVoteButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String voteType = buttonIdParts[2];
		String matchId = buttonIdParts[3];
		String userVoteKey = event.getUser().getId() + "_" + matchId;
		UserVoteDTO userVote = adminUserVotes.getOrDefault(userVoteKey, new UserVoteDTO());
		adminUserVotes.put(userVoteKey, userVote);

		if ("team1".equals(voteType)) {
			userVote.setTeamVote(1L);
			disableButton(event.getMessage(), "admin_vote_team2_" + matchId);
		} else if ("team2".equals(voteType)) {
			userVote.setTeamVote(2L);
			disableButton(event.getMessage(), "admin_vote_team1_" + matchId);
		} else if ("score20".equals(voteType)) {
			userVote.setWinningScore(2);
			userVote.setLosingScore(0);
			disableButton(event.getMessage(), "admin_vote_score21_" + matchId);
		} else if ("score21".equals(voteType)) {
			userVote.setWinningScore(2);
			userVote.setLosingScore(1);
			disableButton(event.getMessage(), "admin_vote_score20_" + matchId);
		}

		// Check if 1 full vote has been received and process the results
		if (userVote.getTeamVote() != null && userVote.getWinningScore() != null && userVote.getLosingScore() != null) {
			// Update matchResult
			MatchResultDTO matchResult = new MatchResultDTO();
			matchResult.setMatchId(Long.parseLong(matchId));
			matchResult.setWinningTeamId(userVote.getTeamVote());
			matchResult.setWinningScore(userVote.getWinningScore());
			matchResult.setLosingScore(userVote.getLosingScore());

			matchService.saveMatchResult(matchResult); // Save the results to the database
			// Display final results
			displayFinalResult(event.getChannel(), matchResult);

			// Remove the vote from adminUserVotes after processing
			adminUserVotes.remove(userVoteKey);
		}
	}

	private void handleVoteButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String voteType = buttonIdParts[1];
		String matchId = buttonIdParts[2];
		String userVoteKey = event.getUser().getId() + "_" + matchId;
		UserVoteDTO userVote = userVotes.getOrDefault(userVoteKey, new UserVoteDTO());
		userVotes.put(userVoteKey, userVote);

		if ("team1".equals(voteType)) {
			userVote.setTeamVote(1L);
			disableButton(event.getMessage(), "vote_team2_" + matchId);
		} else if ("team2".equals(voteType)) {
			userVote.setTeamVote(2L);
			disableButton(event.getMessage(), "vote_team1_" + matchId);
		} else if ("score20".equals(voteType)) {
			userVote.setWinningScore(2);
			userVote.setLosingScore(0);
			disableButton(event.getMessage(), "vote_score21_" + matchId);
		} else if ("score21".equals(voteType)) {
			userVote.setWinningScore(2);
			userVote.setLosingScore(1);
			disableButton(event.getMessage(), "vote_score20_" + matchId);
		}

		// Check if 2 votes have been received and process the results
		usersFullyVoted.putIfAbsent(matchId, new AtomicInteger(0));
		if (userVote.getTeamVote() != null && userVote.getWinningScore() != null && userVote.getLosingScore() != null) {
			if (usersFullyVoted.get(matchId).incrementAndGet() >= 2) {

				// Calculate the majority vote for the winning team and score
				Map<Long, Integer> teamVoteCounts = new HashMap<>();
				Map<String, Integer> scoreVoteCounts = new HashMap<>();

				for (UserVoteDTO vote : userVotes.values()) {
					if (vote.getTeamVote() != null && vote.getWinningScore() != null && vote.getLosingScore() != null) {
						teamVoteCounts.put(vote.getTeamVote(), teamVoteCounts.getOrDefault(vote.getTeamVote(), 0) + 1);
						String scoreVote = vote.getWinningScore() + "-" + vote.getLosingScore();
						scoreVoteCounts.put(scoreVote, scoreVoteCounts.getOrDefault(scoreVote, 0) + 1);
					}
				}

				if (teamVoteCounts.isEmpty() || scoreVoteCounts.isEmpty()) {
					return;
				}

				long winningTeam = teamVoteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
				String winningScore = scoreVoteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).get()
						.getKey();

				// Update matchResult
				System.out.println("Match ID: " + matchId);
				MatchResultDTO matchResult = new MatchResultDTO();
				System.out.println("Match ID Test2: " + Long.parseLong(matchId));
				matchResult.setMatchId(Long.parseLong(matchId));
				matchResult.setWinningTeamId(winningTeam);
				matchResult.setWinningScore(Integer.parseInt(winningScore.split("-")[0]));
				matchResult.setLosingScore(Integer.parseInt(winningScore.split("-")[1]));

				// Send the approval request
				sendApprovalRequest(event, matchResult, matchId);
				usersFullyVoted.remove(matchId);

				// Cancel scheduled admin voting
				executorService.shutdown();

			}
		}
	}

	private void sendApprovalRequest(ButtonClickEvent event, MatchResultDTO matchResult, String matchId) {
		MessageChannel approvalChannel = event.getJDA().getTextChannelById(discordChannelConfig.getApprovalChannelId());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Match Result Approval");
		embedBuilder.setDescription("Please approve or reject the match result for match ID: " + matchId);
		embedBuilder.setColor(Color.YELLOW);
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamId(), true);
		embedBuilder.addField("Score", matchResult.getWinningScore() + "-" + matchResult.getLosingScore(), true);
		MessageEmbed embed = embedBuilder.build();
		ActionRow actionRow = ActionRow.of(Button.success("approve_" + matchId, "✅"),
				Button.danger("reject_" + matchId, "❌"));
		approvalChannel.sendMessageEmbeds(embed).setActionRows(Collections.singletonList(actionRow)).queue();
	}

	private void disableButton(Message message, String buttonIdToDisable) {
		List<ActionRow> updatedActionRows = new ArrayList<>();

		for (ActionRow actionRow : message.getActionRows()) {
			List<Component> updatedComponents = new ArrayList<>();

			for (Component component : actionRow.getComponents()) {
				if (component.getId().equals(buttonIdToDisable)) {
					Button button = (Button) component;
					updatedComponents
							.add(Button.of(button.getStyle(), button.getId(), button.getLabel()).withDisabled(true));
				} else {
					updatedComponents.add(component);
				}
			}

			updatedActionRows.add(ActionRow.of(updatedComponents));
		}

		message.editMessageComponents(updatedActionRows).queue();
	}

	private void displayFinalResult(MessageChannel channel, MatchResultDTO matchResult) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Final Match Result");
		embedBuilder.setDescription("The final result for match " + matchResult.getMatchId() + " is:");
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamId(), false);
		embedBuilder.addField("Score", matchResult.getWinningScore() + "-" + matchResult.getLosingScore(), false);
		embedBuilder.setColor(Color.GREEN);

		MessageEmbed embed = embedBuilder.build();
		channel.sendMessageEmbeds(embed).queue();
	}

}