package com.darianngo.discordBot.services;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;

public interface MatchService {
	MatchDTO createMatch(MatchDTO matchDTO);

	void saveTeamsWithMatchId(List<UserDTO> team1, List<UserDTO> team2, Long matchId);

	void updateMatch(MatchDTO matchDTO);

	MatchDTO getMatchById(Long matchId) throws NotFoundException;

	List<UserDTO> getUsersInMatch(Long matchId);

	List<UserDTO> getUsersReactedForMatch(Long matchId);

	void saveMatchResult(MatchResultDTO matchResultDTO);

}
