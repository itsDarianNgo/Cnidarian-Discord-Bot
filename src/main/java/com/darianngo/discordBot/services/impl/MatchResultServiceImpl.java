package com.darianngo.discordBot.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.entities.MatchResultEntity;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.mappers.MatchResultMapper;
import com.darianngo.discordBot.repositories.MatchResultRepository;
import com.darianngo.discordBot.services.MatchResultService;

@Service
public class MatchResultServiceImpl implements MatchResultService {

	@Autowired
	private MatchResultRepository matchResultRepository;

	@Autowired
	private MatchResultMapper matchResultMapper;

	@Override
	public void saveVote(MatchResultDTO matchResultDTO, UserEntity userId) {
		MatchResultEntity matchResultEntity = matchResultRepository.findByMatchIdAndUser(matchResultDTO.getMatchId(),
				userId);

		if (matchResultEntity == null) {
			matchResultEntity = new MatchResultEntity();
			matchResultEntity.setMatchId(matchResultDTO.getMatchId());
		}

		matchResultEntity.setWinningTeamId(matchResultDTO.getWinningTeamId());
		matchResultEntity.setWinningScore(matchResultDTO.getWinningScore());
		matchResultEntity.setLosingScore(matchResultDTO.getLosingScore());

		matchResultRepository.save(matchResultEntity);
	}
}
