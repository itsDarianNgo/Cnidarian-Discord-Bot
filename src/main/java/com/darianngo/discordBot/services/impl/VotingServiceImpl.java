package com.darianngo.discordBot.services.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.config.DiscordChannelConfig;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.entities.MatchEntity;
import com.darianngo.discordBot.services.MatchService;
import com.darianngo.discordBot.services.VotingService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

@Service
public class VotingServiceImpl implements VotingService {
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private final Map<String, AtomicInteger> usersFullyVoted = new ConcurrentHashMap<>();
	private final Map<String, MatchResultDTO> matchResults = new HashMap<>();

	@Autowired
	private MatchService matchService;
	@Autowired
	private DiscordChannelConfig discordChannelConfig;

	@Override
	public void startVoteCountdown(ButtonClickEvent event, String matchId) {
		executorService.schedule(() -> {
			if (usersFullyVoted.get(matchId) == null || usersFullyVoted.get(matchId).get() < 2) {
				// Send admin voting message if no majority vote or tie within 10 minutes
				User admin = event.getJDA().retrieveUserById(event.getUser().getId()).complete();
				sendAdminVoting(admin, matchId, matchResults.get(matchId));
			}
		}, 10, TimeUnit.MINUTES);
	}

	@Override
	public void sendAdminVoting(User admin, String matchId, MatchResultDTO matchResult) {
		Map<Long, List<UserDTO>> teamMembers = matchService
				.getTeamMembers(matchService.getMatchEntityById(Long.parseLong(matchId)).getTeams());

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Vote has been rejected");
		embedBuilder.setDescription("Choose the CORRECT winning team and score for match: " + matchId + "\n\n");
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

		List<Component> components = new ArrayList<>();
		components.add(Button.primary("admin_vote_team1_" + matchId, "Team 1"));
		components.add(Button.primary("admin_vote_team2_" + matchId, "Team 2"));
		components.add(Button.primary("admin_vote_score20_" + matchId, "2-0"));
		components.add(Button.primary("admin_vote_score21_" + matchId, "2-1"));

		String approvalChannelId = discordChannelConfig.getApprovalChannelId();
		TextChannel approvalChannel = admin.getJDA().getTextChannelById(approvalChannelId);

		// Build the embed from the embedBuilder
		MessageEmbed embed = embedBuilder.build();

		if (approvalChannel != null) {
			approvalChannel.sendMessageEmbeds(embed).setActionRow(components).queue();
		} else {
			System.out.println("Error: Approval channel not found.");
		}
	}

	@Override
	public void sendVotingDM(User user, String matchId) {
		Long matchIdLong = Long.parseLong(matchId);
		MatchEntity matchEntity = matchService.getMatchEntityById(matchIdLong);
		Map<Long, List<UserDTO>> teamMembers = matchService.getTeamMembers(matchEntity.getTeams());

		MessageEmbed embed = buildEmbed(matchId, teamMembers);

		List<Component> components = new ArrayList<>();
		components.add(Button.primary("vote_team1_" + matchId, "Team 1"));
		components.add(Button.primary("vote_team2_" + matchId, "Team 2"));
		components.add(Button.primary("vote_score20_" + matchId, "2-0"));
		components.add(Button.primary("vote_score21_" + matchId, "2-1"));

		user.openPrivateChannel().queue(privateChannel -> {
			privateChannel.sendMessageEmbeds(embed).setActionRow(components).queue(message -> {
				// Schedule a task to disable the buttons after 10 minutes
				executorService.schedule(() -> {
					disableButtons(message);
				}, 10, TimeUnit.MINUTES);
			});
		});
	}

// Helper methods
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

	private void disableButtons(Message message) {
		List<ActionRow> updatedActionRows = new ArrayList<>();

		for (ActionRow actionRow : message.getActionRows()) {
			List<Component> updatedComponents = new ArrayList<>();

			for (Component component : actionRow.getComponents()) {
				if (component instanceof Button) {
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
}
