package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MatchResultDTO;

public interface MatchResultService {

//	void saveVote(MatchResultDTO matchResultDTO, UserEntity userId);

	MatchResultDTO saveMatchResult(MatchResultDTO matchResult);

	Long getWinningTeamId(Long matchId, Long winningTeamNumber);

	Long getLosingTeamId(Long matchId, Long winningTeamNumber);

}
