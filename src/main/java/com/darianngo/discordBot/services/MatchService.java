package com.darianngo.discordBot.services;

import java.util.List;

import com.darianngo.discordBot.dtos.MatchDTO;
import com.darianngo.discordBot.dtos.UserDTO;

public interface MatchService {
	MatchDTO createMatch(MatchDTO matchDTO);

	void saveTeamsWithMatchId(List<UserDTO> team1, List<UserDTO> team2, Long matchId);

}
