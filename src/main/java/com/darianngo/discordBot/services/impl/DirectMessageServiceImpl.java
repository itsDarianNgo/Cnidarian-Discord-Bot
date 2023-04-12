package com.darianngo.discordBot.services.impl;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.TeamDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.listeners.ButtonClickListener;
import com.darianngo.discordBot.services.DirectMessageService;
import com.darianngo.discordBot.services.MatchResultService;
import com.darianngo.discordBot.services.MatchService;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

@Service
public class DirectMessageServiceImpl implements DirectMessageService, EventListener {
	@Autowired
	private MatchResultService matchResultService;
	@Autowired
	private MatchService matchService;

	private Map<Long, List<User>> matchIdToUsersReacted = new HashMap<>();

	public void sendEndMatchButtons(List<User> usersReacted, Long matchId) {
		matchIdToUsersReacted.put(matchId, usersReacted);

		for (User user : usersReacted) {
			user.openPrivateChannel().flatMap(
					privateChannel -> privateChannel.sendMessage("Please choose the winning team and the score:"))
					.flatMap(message -> message.editMessageComponents(
							ActionRow.of(Button.primary("winning_team_1_" + matchId, "Team 1"),
									Button.primary("winning_team_2_" + matchId, "Team 2")),
							ActionRow.of(Button.secondary("winning_score_2_0_" + matchId, "2-0"),
									Button.secondary("winning_score_2_1_" + matchId, "2-1"))))
					.queue();
		}
	}

	@Override
	public void onButtonClick(@Nonnull ButtonClickEvent event) {
		try {
			if (event.getButton().getId().startsWith("endMatch")) {
				Long matchId = Long.parseLong(event.getButton().getId().substring(8));
				ButtonClickListener buttonClickListener = new ButtonClickListener(matchService, matchResultService);;

				event.getJDA().addEventListener(buttonClickListener);

				MatchDTO match = matchService.getMatchById(matchId); // Implement a method to get the match by ID
				for (TeamDTO team : match.getTeams()) {
					for (UserDTO userDTO : team.getMembers()) {
						User user = event.getJDA().getUserById(userDTO.getDiscordId());
						if (user != null) {
							MessageEmbed embed = new EmbedBuilder().setTitle("Match Result")
									.setDescription("Please select the winning team and score:").build();
							user.openPrivateChannel()
									.queue(privateChannel -> privateChannel.sendMessageEmbeds(embed)
											.setActionRows(
													ActionRow.of(Button.primary("winningTeam1", "Team 1"),
															Button.primary("winningTeam2", "Team 2")),
													ActionRow.of(Button.primary("winningScore20", "2-0"),
															Button.primary("winningScore21", "2-1")))
											.queue());
						}
					}
				}
			}
		} catch (ChangeSetPersister.NotFoundException e) {
			// Handle the exception appropriately, e.g. log it, show an error message to the
			// user, etc.
		}
	}

	public void postMatchResults(MessageChannel channel, MatchResultDTO matchResultDTO) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setTitle("Match Results");
		embedBuilder.addField("Winning Team", "Team " + matchResultDTO.getWinningTeamId(), true);
		embedBuilder.addField("Final Score", matchResultDTO.getWinningScore() + "-" + matchResultDTO.getLosingScore(),
				true);
		channel.sendMessageEmbeds(embedBuilder.build()).queue();
	}

	@Override
	public void onEvent(@Nonnull GenericEvent event) {
		if (event instanceof ButtonClickEvent) {
			onButtonClick((ButtonClickEvent) event);
		}
	}
}