package com.darianngo.discordBot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.MatchService;

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
	private final Map<String, AtomicInteger> matchVoteCounter = new HashMap<>();
	private final Map<String, MatchResultDTO> matchResults = new HashMap<>();

	public ButtonClickListener(MatchService matchService) {
		this.matchService = matchService;
	}

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		String[] buttonIdParts = event.getComponentId().split("_");
		String action = buttonIdParts[0];

		if ("end".equals(action)) {
			handleEndButtonClick(event);
		} else if ("vote".equals(action)) {
			handleVoteButtonClick(event, buttonIdParts);
		}
		event.deferEdit().queue();// Acknowledge the event
	}

	private void handleEndButtonClick(ButtonClickEvent event) {
		String componentId = event.getComponentId();
		if (!componentId.startsWith("end_match_")) {
			return; // Ignore the button click if the component ID does not start with "end_match_".
		}

		String matchId = componentId.substring("end_match_".length());
		List<UserDTO> usersReacted = matchService.getUsersReactedForMatch(Long.parseLong(matchId));

		for (UserDTO userDTO : usersReacted) {
			User user = event.getJDA().retrieveUserById(userDTO.getDiscordId()).complete();
			sendVotingDM(user, matchId);
		}
	}

	private void sendVotingDM(User user, String matchId) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Vote for the winning team and score");
		embedBuilder.setDescription("Please select the winning team and score for match " + matchId + ".");
		embedBuilder.setColor(Color.CYAN);

		MessageEmbed embed = embedBuilder.build();

		List<Component> components = new ArrayList<>();
		components.add(Button.primary("vote_team1_" + matchId, "Team 1"));
		components.add(Button.primary("vote_team2_" + matchId, "Team 2"));
		components.add(Button.primary("vote_score20_" + matchId, "2-0"));
		components.add(Button.primary("vote_score21_" + matchId, "2-1"));

		user.openPrivateChannel().queue(privateChannel -> {
			privateChannel.sendMessageEmbeds(embed).setActionRow(components).queue();
		});
	}

	private void handleVoteButtonClick(ButtonClickEvent event, String[] buttonIdParts) {
		String voteType = buttonIdParts[1];
		String matchId = buttonIdParts[2];
		String userVoteKey = event.getUser().getId() + "_" + matchId;

		if (matchVoteCounter.containsKey(userVoteKey)) {

			AtomicInteger voteCount = matchVoteCounter.get(userVoteKey);
			if (voteCount.get() >= 5) {
				event.reply("You cannot vote anymore. The maximum number of votes has been reached.").setEphemeral(true)
						.queue();
				return;
			}
		} else {
			matchVoteCounter.put(userVoteKey, new AtomicInteger(0));
		}

		MatchResultDTO matchResult = matchResults.getOrDefault(matchId, new MatchResultDTO());
		matchResults.put(matchId, matchResult);
		matchResult.setMatchId(Long.parseLong(matchId));

		if ("team1".equals(voteType)) {
			matchResult.setWinningTeamId(1L);
			disableButton(event.getMessage(), "vote_team2_" + matchId);
		} else if ("team2".equals(voteType)) {
			matchResult.setWinningTeamId(2L);
			disableButton(event.getMessage(), "vote_team1_" + matchId);
		} else if ("score20".equals(voteType)) {
			matchResult.setWinningScore(2);
			matchResult.setLosingScore(0);
			disableButton(event.getMessage(), "vote_score21_" + matchId);
		} else if ("score21".equals(voteType)) {
			matchResult.setWinningScore(2);
			matchResult.setLosingScore(1);
			disableButton(event.getMessage(), "vote_score20_" + matchId);
		}

		matchVoteCounter.get(userVoteKey).incrementAndGet();

		if (matchResult.getWinningTeamId() != null && matchResult.getWinningScore() != null) {
			matchService.saveMatchResult(matchResult);
			displayFinalResult(event.getChannel(), matchResult);
		}
	}

	private void disableButton(Message message, String buttonIdToDisable) {
		List<Component> updatedComponents = new ArrayList<>();

		for (Component component : message.getActionRows().get(0).getComponents()) {
			if (component.getId().equals(buttonIdToDisable)) {
				Button button = (Button) component;
				updatedComponents
						.add(Button.of(button.getStyle(), button.getId(), button.getLabel()).withDisabled(true));
			} else {
				updatedComponents.add(component);
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