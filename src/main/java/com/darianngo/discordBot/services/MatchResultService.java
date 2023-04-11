package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MatchResultDTO;

public interface MatchResultService {
	MatchResultDTO saveMatchResult(MatchResultDTO matchResultDTO);
}
