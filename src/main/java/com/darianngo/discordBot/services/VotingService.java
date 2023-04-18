package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MatchResultDTO;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public interface VotingService {

	void startVoteCountdown(ButtonClickEvent event, String matchId);

	void sendAdminVoting(User admin, String matchId, MatchResultDTO matchResult);

	void sendVotingDM(User user, String matchId);

	void cancelVoteCountdown(String matchId);

}
