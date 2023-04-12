package com.darianngo.discordBot.listeners;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.services.MatchResultService;
import com.darianngo.discordBot.services.MatchService;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

@Component
public class ButtonClickListener extends ListenerAdapter {
	private MatchService matchService;
	private MatchResultService matchResultService;

	// Store the votes for each match
	private Map<Long, Map<Long, AtomicInteger>> teamVotes = new HashMap<>();
	private Map<Long, Map<String, AtomicInteger>> scoreVotes = new HashMap<>();

	public ButtonClickListener(MatchService matchService, MatchResultService matchResultService) {
		this.matchService = matchService;
		this.matchResultService = matchResultService;
	}

	@Override
//	CODE WORKS BUT SPAMS messages twice then voting doesnt work atm.
	public void onButtonClick(@Nonnull ButtonClickEvent event) {
	    System.out.println("Button clicked: " + event.getButton().getId());

	    if (event.getButton().getId().startsWith("end_match_")) {
	        // Acknowledge the interaction
	        event.deferReply().setEphemeral(true).queue();

	        Long matchId = Long.parseLong(event.getButton().getId().split("_")[2]);

	        // Retrieve the users involved in the match, and populate the usersInMatch list
	        List<UserDTO> usersInMatch = matchService.getUsersInMatch(matchId);

	        sendDmToUsers(usersInMatch, matchId, event.getJDA());
	        System.out.println("DM sent to users for match " + matchId);
	    } else if (event.getButton().getId().startsWith("vote_")) {
	        // Disable the clicked button
	        Button clickedButton = event.getButton();
	        Button disabledButton = clickedButton.asDisabled();
	        event.getHook().editOriginalComponents(ActionRow.of(disabledButton)).queue();

	        // Process the vote
	        if (event.getButton().getId().endsWith("_team_1") || event.getButton().getId().endsWith("_team_2")) {
	        }
	    }
	}


	// Send DM to users with buttons for voting
	private void sendDmToUsers(List<UserDTO> users, Long matchId, JDA jda) {
		EmbedBuilder embed = new EmbedBuilder().setTitle("Vote for the winning team and score")
				.setDescription("Please select the winning team and the final score").setColor(Color.CYAN);

		ActionRow teamRow = ActionRow.of(Button.primary("vote_" + matchId + "_team_1", "Team 1"),
				Button.primary("vote_" + matchId + "_team_2", "Team 2"));

		ActionRow scoreRow = ActionRow.of(Button.primary("vote_" + matchId + "_score_2-0", "2-0"),
				Button.primary("vote_" + matchId + "_score_2-1", "2-1"));

		users.stream().filter(Objects::nonNull)
				.filter(user -> user.getDiscordId() != null && !user.getDiscordId().isEmpty()).forEach(user -> {
					jda.openPrivateChannelById(user.getDiscordId()).queue(privateChannel -> {
						System.out.println("Sending DMs to " + users.size() + " users for match " + matchId);

						System.out.println("Opening private channel with user " + user.getDiscordId());
						privateChannel.sendMessageEmbeds(embed.build()).setActionRows(teamRow, scoreRow)
								.queue(message -> {
									System.out.println(
											"Sent DM to user " + user.getDiscordId() + " for match " + matchId);
								});

					});
				});
	}
}