package com.darianngo.discordBot.services;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.entities.UserEntity;

public interface MatchResultService {

	void saveVote(MatchResultDTO matchResultDTO, UserEntity userId);

}
