package com.darianngo.discordBot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserVoteDTO;
import com.darianngo.discordBot.embeds.FinalResultEmbed;
import com.darianngo.discordBot.embeds.LeaderboardEmbed;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.services.EloService;
import com.darianngo.discordBot.services.MatchResultService;
import com.darianngo.discordBot.services.MatchService;
import com.darianngo.discordBot.services.UserService;
import com.darianngo.discordBot.services.VotingService;

import jakarta.persistence.EntityNotFoundException;
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
	private final UserService userService;
	private final EloService eloService;
	private final MatchResultService matchResultService;
	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final LeaderboardEmbed leaderboardEmbed;

	public ButtonClickListener(MatchService matchService, VotingService votingService, UserService userService,
			EloService eloService, MatchResultService matchResultService, UserMapper userMapper,
			UserRepository userRepository, LeaderboardEmbed leaderboardEmbed) {
		this.matchService = matchService;
		this.votingService = votingService;
		this.userService = userService;
		this.eloService = eloService;
		this.matchResultService = matchResultService;
		this.userMapper = userMapper;
		this.userRepository = userRepository;
		this.leaderboardEmbed = leaderboardEmbed;
	}

	@Autowired
	private DiscordChannelConfig discordChannelConfig;

	private Map<String, UserVoteDTO> userVotes = new ConcurrentHashMap<>();
	private final Map<String, MatchResultDTO> matchResults = new HashMap<>();
	private final Map<String, AtomicInteger> usersFullyVoted = new ConcurrentHashMap<>();
	private Map<String, UserVoteDTO> adminUserVotes = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private final Set<String> usersWhoVoted = Collections.synchronizedSet(new HashSet<>());
	private final ConcurrentHashMap<String, Boolean> majorityReached = new ConcurrentHashMap<>();

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
		} else if ("cancel".equals(action)) {
			handleCancelButtonClick(event);
		} else if (event.getComponentId().startsWith("prev_page") || event.getComponentId().startsWith("next_page")) {
			handleLeaderboardPagination(event);
		}

		event.deferEdit().queue(); // Acknowledge the event
	}

	private void handleLeaderboardPagination(ButtonClickEvent event) {
		List<UserDTO> leaderboard = userService.getLeaderboard();
		int totalPages = (int) Math.ceil((double) leaderboard.size() / 10);

		int currentPage = 0; // Initialize with a default value
		String[] componentIdParts = event.getComponentId().split(":");

		if (componentIdParts.length > 1) {
			currentPage = Integer.parseInt(componentIdParts[1]);
		} else {
			// Log an error or handle the issue in some other way
		}

		if (event.getComponentId().startsWith("prev_page")) {
			currentPage--;
		} else if (event.getComponentId().startsWith("next_page")) {
			currentPage++;
		}

		MessageEmbed updatedEmbed = leaderboardEmbed.generateLeaderboardEmbed(leaderboard, currentPage);
		Button updatedPrevButton = currentPage == 0
				? Button.primary("prev_page:" + currentPage, "Previous").asDisabled()
				: Button.primary("prev_page:" + currentPage, "Previous");
		Button updatedNextButton = currentPage == totalPages - 1
				? Button.primary("next_page:" + currentPage, "Next").asDisabled()
				: Button.primary("next_page:" + currentPage, "Next");
		ActionRow updatedActionRow = ActionRow.of(updatedPrevButton, updatedNextButton);

		event.getHook().editOriginalEmbeds(updatedEmbed).setActionRows(updatedActionRow).queue();

	}

	private void handleCancelButtonClick(ButtonClickEvent event) {
		String componentId = event.getComponentId();
		if (!componentId.startsWith("cancel_match_")) {
			return; // Ignore the button click if the component ID does not start with
					// "cancel_match_".
		}

		String matchId = componentId.substring("cancel_match_".length());
		matchService.cancelMatch(Long.parseLong(matchId)); // Cancel the match in the service layer

		// Remove the "End Match" and "Cancel Match" buttons
		removeButtons(event.getMessage(), "end_match_" + matchId, "cancel_match_" + matchId);

		// Get the discordId of the user who clicked the button
		long discordId = event.getUser().getIdLong();

		// Retrieve the user's name and send a message with their name
		event.getJDA().retrieveUserById(discordId).queue(user -> {
			String userName = user.getName();
			event.getChannel().sendMessage("Match " + matchId + " has been cancelled by " + "<@" + discordId + ">.")
					.queue();
		});
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

		// Remove the "End Match" and "Cancel Match" buttons
		removeButtons(event.getMessage(), "end_match_" + matchId, "cancel_match_" + matchId);

		// Get the discordId of the user who clicked the button
		long discordId = event.getUser().getIdLong();

		// Retrieve the user's name and send a message with their name and an @mention
		event.getJDA().retrieveUserById(discordId).queue(user -> {
			String userName = user.getName();
			event.getChannel().sendMessage("Match " + matchId + " was ended by " + "<@" + discordId
					+ ">. Check your DMs to vote for the winner!").queue();
		});
	}

	private void handleApproveButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String matchId = buttonIdParts[1];
		UserVoteDTO userVote = userVotes.get(event.getUser().getId() + "_" + matchId);
		MatchDTO match = new MatchDTO();
		MatchResultDTO matchResult = new MatchResultDTO();

		match.setId(Long.parseLong(matchId));
		matchResult.setMatch(match);
		matchResult.setMatchId(Long.parseLong(matchId));
		matchResult.setWinningTeamNumber(userVote.getTeamVote());
		matchResult.setLosingTeamNumber(matchResult.getWinningTeamNumber() == 1L ? 2L : 1L);
		matchResult.setWinningTeamId(
				matchResultService.getWinningTeamId(Long.parseLong(matchId), matchResult.getWinningTeamNumber()));
		matchResult.setLosingTeamId(
				matchResultService.getLosingTeamId(Long.parseLong(matchId), matchResult.getWinningTeamNumber()));
		matchResult.setWinningScore(userVote.getWinningScore());
		matchResult.setLosingScore(userVote.getLosingScore());

		// Save the results to the database
		matchService.saveMatchResult(matchResult);

		// Get the match using the matchId
		try {
			match = matchService.getMatchById(Long.parseLong(matchId));
		} catch (EntityNotFoundException | NumberFormatException | NotFoundException e) {
			// Handle the exception (e.g., log the error, send a message to the user, etc.)
			event.getChannel().sendMessage("Match not found with ID: " + matchId).queue();
			return;
		}

		// Get the teams associated with the match
		Map<Long, List<UserDTO>> teamsWithMatchId = matchService.getTeamsWithMatchId(Long.parseLong(matchId));
		List<TeamDTO> teams = teamsWithMatchId.entrySet().stream().map(entry -> {
			TeamDTO teamDTO = new TeamDTO();
			teamDTO.setId(entry.getKey());
			teamDTO.setMatchId(Long.parseLong(matchId));
			teamDTO.setMembers(entry.getValue());
			return teamDTO;
		}).collect(Collectors.toList());
		match.setTeams(teams);

		// Get the UserDTOs for all users in the match
		List<UserDTO> users = getUsersInMatch(match);
		// Update ELO ratings using the eloService
		eloService.updateElo(matchResult, users);

		// Fetch updated UserEntities from the database and convert them to UserDTOs
		List<UserDTO> updatedUsers = users.stream().map(user -> userRepository.findById(user.getDiscordId())
				.map(userEntity -> userMapper.toDto(userEntity)).orElse(null)).collect(Collectors.toList());

		// Replace existing UserDTOs in the TeamDTO objects with the updated ones
		for (TeamDTO team : match.getTeams()) {
			List<UserDTO> updatedTeamMembers = team.getMembers().stream()
					.map(user -> updatedUsers.stream()
							.filter(updatedUser -> updatedUser.getDiscordId().equals(user.getDiscordId())).findFirst()
							.orElse(user))
					.collect(Collectors.toList());
			team.setMembers(updatedTeamMembers);
		}

		// Display the final results in the current channel
		event.getChannel().sendMessageEmbeds(FinalResultEmbed.createEmbed(matchResult, match)).queue();

		// Display the final results in match channel
		matchService.sendMatchResultToDesignatedChannel(matchResult, match);

		// Delete the message
		event.getMessage().delete().queue();
	}

	private List<UserDTO> getUsersInMatch(MatchDTO match) {
		List<UserDTO> users = new ArrayList<>();
		for (TeamDTO team : match.getTeams()) {
			for (UserDTO user : team.getMembers()) {
				String userId = user.getDiscordId();
				UserDTO fetchedUser = userService.getUserById(userId);
				if (fetchedUser != null) {
					users.add(fetchedUser);
				}
			}
		}
		return users;
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
		MatchDTO match = new MatchDTO();
		MatchResultDTO matchResult = new MatchResultDTO();

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
			matchResult.setMatchId(Long.parseLong(matchId));
			matchResult.setWinningTeamNumber(userVote.getTeamVote());
			matchResult.setLosingTeamNumber(matchResult.getWinningTeamNumber() == 1L ? 2L : 1L);
			matchResult.setWinningTeamId(
					matchResultService.getWinningTeamId(Long.parseLong(matchId), matchResult.getWinningTeamNumber()));
			matchResult.setLosingTeamId(
					matchResultService.getLosingTeamId(Long.parseLong(matchId), matchResult.getWinningTeamNumber()));
			matchResult.setWinningScore(userVote.getWinningScore());
			matchResult.setLosingScore(userVote.getLosingScore());

			// Save the results to the database
			matchService.saveMatchResult(matchResult);

			// Get the match using the matchId
			try {
				match = matchService.getMatchById(Long.parseLong(matchId));
			} catch (EntityNotFoundException | NumberFormatException | NotFoundException e) {
				// Handle the exception (e.g., log the error, send a message to the user, etc.)
				event.getChannel().sendMessage("Match not found with ID: " + matchId).queue();
				return;
			}

			// Get the teams associated with the match
			Map<Long, List<UserDTO>> teamsWithMatchId = matchService.getTeamsWithMatchId(Long.parseLong(matchId));
			List<TeamDTO> teams = teamsWithMatchId.entrySet().stream().map(entry -> {
				TeamDTO teamDTO = new TeamDTO();
				teamDTO.setId(entry.getKey());
				teamDTO.setMatchId(Long.parseLong(matchId));
				teamDTO.setMembers(entry.getValue());
				return teamDTO;
			}).collect(Collectors.toList());
			match.setTeams(teams);

			// Get the UserDTOs for all users in the match
			List<UserDTO> users = getUsersInMatch(match);
			// Update ELO ratings using the eloService
			eloService.updateElo(matchResult, users);

			// Fetch updated UserEntities from the database and convert them to UserDTOs
			List<UserDTO> updatedUsers = users.stream().map(user -> userRepository.findById(user.getDiscordId())
					.map(userEntity -> userMapper.toDto(userEntity)).orElse(null)).collect(Collectors.toList());

			// Replace existing UserDTOs in the TeamDTO objects with the updated ones
			for (TeamDTO team : match.getTeams()) {
				List<UserDTO> updatedTeamMembers = team.getMembers().stream()
						.map(user -> updatedUsers.stream()
								.filter(updatedUser -> updatedUser.getDiscordId().equals(user.getDiscordId()))
								.findFirst().orElse(user))
						.collect(Collectors.toList());
				team.setMembers(updatedTeamMembers);
			}

			// Display the final results in the current channel
			event.getChannel().sendMessageEmbeds(FinalResultEmbed.createEmbed(matchResult, match)).queue();

			// Display the final results in match channel
			matchService.sendMatchResultToDesignatedChannel(matchResult, match);

			// Remove the vote from adminUserVotes after processing
			adminUserVotes.remove(userVoteKey);
		}
	}

	private void handleVoteButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String voteType = buttonIdParts[1];
		String matchId = buttonIdParts[2];
		String userVoteKey = event.getUser().getId() + "_" + matchId;

		// Check if the user has already voted
		if (usersWhoVoted.contains(userVoteKey)) {
			event.reply("You can only vote once per match.").setEphemeral(true).queue();
			return;
		}

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

		// Check if 7 votes have been received and process the results
		usersFullyVoted.putIfAbsent(matchId, new AtomicInteger(0));
		if (userVote.getTeamVote() != null && userVote.getWinningScore() != null && userVote.getLosingScore() != null) {
			if (usersFullyVoted.get(matchId).incrementAndGet() >= 7) {

				// Calculate the majority vote for the winning team and score
				Map<Long, Integer> teamVoteCounts = new HashMap<>();
				Map<String, Integer> scoreVoteCounts = new HashMap<>();

				// Add the user ID to the usersWhoVoted set
				usersWhoVoted.add(userVoteKey);

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

				// Check if the majority vote has already been reached
				if (!majorityReached.getOrDefault(matchId, false)) {

					long winningTeam = teamVoteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).get()
							.getKey();
					String winningScore = scoreVoteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).get()
							.getKey();

					// Update matchResult
					MatchResultDTO matchResult = new MatchResultDTO();
					matchResult.setMatchId(Long.parseLong(matchId));
					matchResult.setWinningTeamNumber(winningTeam);
					matchResult.setWinningScore(Integer.parseInt(winningScore.split("-")[0]));
					matchResult.setLosingScore(Integer.parseInt(winningScore.split("-")[1]));

					// Send the approval request
					sendApprovalRequest(event, matchResult, matchId);
					usersFullyVoted.remove(matchId);
					majorityReached.put(matchId, true); // Set the majorityReached flag to true

					// Cancel the scheduled admin voting
					votingService.cancelVoteCountdown(matchId);
				}
			}
		}
	}

	private void sendApprovalRequest(ButtonClickEvent event, MatchResultDTO matchResult, String matchId) {
		MessageChannel approvalChannel = event.getJDA().getTextChannelById(discordChannelConfig.getApprovalChannelId());
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Match Result Approval");
		embedBuilder.setDescription("Please approve or reject the match result for match ID: " + matchId);
		embedBuilder.setColor(Color.YELLOW);
		embedBuilder.addField("Winning Team", "Team " + matchResult.getWinningTeamNumber(), true);
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

	private void removeButtons(Message message, String... buttonIds) {
		List<String> buttonIdsToRemove = Arrays.asList(buttonIds);
		List<ActionRow> actionRows = message.getActionRows();
		List<ActionRow> updatedActionRows = new ArrayList<>();

		for (ActionRow actionRow : actionRows) {
			List<Component> updatedComponents = new ArrayList<>();

			for (Component component : actionRow.getComponents()) {
				if (component instanceof Button) {
					Button button = (Button) component;
					if (buttonIdsToRemove.contains(button.getId())) {
						continue; // Skip buttons with IDs that need to be removed
					}
				}
				updatedComponents.add(component);
			}
			if (!updatedComponents.isEmpty()) {
				updatedActionRows.add(ActionRow.of(updatedComponents));
			}
		}

		message.editMessageComponents(updatedActionRows).queue();
	}
}