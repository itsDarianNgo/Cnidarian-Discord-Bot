package com.darianngo.discordBot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserVoteDTO;
import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.services.MatchService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

@org.springframework.stereotype.Component
public class ButtonClickListener extends ListenerAdapter {
	private final MatchService matchService;
	private Map<String, UserVoteDTO> userVotes = new ConcurrentHashMap<>();
	private final Map<String, MatchResultDTO> matchResults = new HashMap<>();
	private final Map<String, AtomicInteger> usersFullyVoted = new ConcurrentHashMap<>();

	public ButtonClickListener(MatchService matchService) {
		this.matchService = matchService;
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
		}
		event.deferEdit().queue();// Acknowledge the event
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
			sendVotingDM(user, matchId);
		}

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
	}

	private void handleRejectButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String matchId = buttonIdParts[1];
		MatchResultDTO matchResult = matchResults.get(matchId);

		User user = event.getUser();
		sendAdminVotingDM(user, matchId, matchResult);
	}

	private void sendVotingDM(User user, String matchId) {
		Long matchIdLong = Long.parseLong(matchId);
		MatchEntity matchEntity = matchService.getMatchEntityById(matchIdLong);
		Map<Long, List<UserDTO>> teamMembers = matchService.getTeamMembers(matchEntity.getTeams());

		// Log the value of matchId
		System.out.println("matchId: " + matchId);

		MessageEmbed embed = buildEmbed(matchId, teamMembers);

		List<Component> components = new ArrayList<>();
		components.add(Button.primary("vote_team1_" + matchId, "Team 1"));
		components.add(Button.primary("vote_team2_" + matchId, "Team 2"));
		components.add(Button.primary("vote_score20_" + matchId, "2-0"));
		components.add(Button.primary("vote_score21_" + matchId, "2-1"));

		user.openPrivateChannel().queue(privateChannel -> {
			privateChannel.sendMessageEmbeds(embed).setActionRow(components).queue();
		});
	}

	private void sendAdminVotingDM(User admin, String matchId, MatchResultDTO matchResult) {
		Map<Long, List<UserDTO>> teamMembers = matchService
				.getTeamMembers(matchService.getMatchEntityById(Long.parseLong(matchId)).getTeams());

		MessageEmbed embed = buildEmbed(matchId, teamMembers);

		List<Component> components = new ArrayList<>();
		components.add(Button.primary("admin_vote_team1_" + matchId, "Team 1"));
		components.add(Button.primary("admin_vote_team2_" + matchId, "Team 2"));
		components.add(Button.primary("admin_vote_score20_" + matchId, "2-0"));
		components.add(Button.primary("admin_vote_score21_" + matchId, "2-1"));

		String approvalChannelId = discordChannelConfig.getApprovalChannelId();
		TextChannel approvalChannel = admin.getJDA().getTextChannelById(approvalChannelId);

		if (approvalChannel != null) {
			approvalChannel.sendMessageEmbeds(embed).setActionRow(components).queue();
		} else {
			System.out.println("Error: Approval channel not found.");
		}
	}

	private MessageEmbed buildEmbed(String matchId, Map<Long, List<UserDTO>> teamMembers) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Vote for the winning team and score");
		embedBuilder.setDescription("Please select the winning team and score for match: " + matchId + "\n\n");
		embedBuilder.setColor(Color.CYAN);

		int teamNumber = 1;
		for (Map.Entry<Long, List<UserDTO>> entry : teamMembers.entrySet()) {
			StringBuilder teamDescription = new StringBuilder();
			List<String> memberNames = new ArrayList<>();
			for (UserDTO userDTO : entry.getValue()) {
				memberNames.add("@" + userDTO.getDiscordName());
			}
			teamDescription.append(String.join(", ", memberNames));

			embedBuilder.addField("Team " + teamNumber, teamDescription.toString(), true);
			teamNumber++;
		}

		return embedBuilder.build();
	}

	private void handleVoteButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		System.out.println("Button ID Parts: " + Arrays.toString(buttonIdParts));

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
		List<Component> updatedComponents = new ArrayList<>();

		for (ActionRow actionRow : message.getActionRows()) {
			for (Component component : actionRow.getComponents()) {

				if (component.getId().equals(buttonIdToDisable)) {
					Button button = (Button) component;
					updatedComponents
							.add(Button.of(button.getStyle(), button.getId(), button.getLabel()).withDisabled(true));
				} else {
					updatedComponents.add(component);
				}
			}
		}

		message.editMessageComponents(ActionRow.of(updatedComponents)).queue();
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