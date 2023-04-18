package com.darianngo.discordBot.services;

import java.util.List;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;

public interface EloService {
	void updateElo(MatchResultDTO matchResult, List<UserDTO> usersInMatch);
}
