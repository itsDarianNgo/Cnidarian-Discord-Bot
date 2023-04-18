package com.darianngo.discordBot.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.entities.MatchResultEntity;
import com.darianngo.discordBot.entities.TeamEntity;
import com.darianngo.discordBot.mappers.MatchResultMapper;
import com.darianngo.discordBot.repositories.MatchResultRepository;
import com.darianngo.discordBot.repositories.TeamRepository;
import com.darianngo.discordBot.services.MatchResultService;

@Service
public class MatchResultServiceImpl implements MatchResultService {

	@Autowired
	private MatchResultRepository matchResultRepository;

	@Autowired
	private MatchResultMapper matchResultMapper;
	@Autowired
	private TeamRepository teamRepository;

//	@Override
//	public void saveVote(MatchResultDTO matchResultDTO, UserEntity userId) {
//		MatchResultEntity matchResultEntity = matchResultRepository.findByMatchIdAndUser(matchResultDTO.getMatchId(),
//				userId);
//
//		if (matchResultEntity == null) {
//			matchResultEntity = new MatchResultEntity();
//			matchResultEntity.setMatchId(matchResultDTO.getMatchId());
//		}
//
//		matchResultEntity.setWinningTeamId(matchResultDTO.getWinningTeamId());
//		matchResultEntity.setWinningScore(matchResultDTO.getWinningScore());
//		matchResultEntity.setLosingScore(matchResultDTO.getLosingScore());
//
//		matchResultRepository.save(matchResultEntity);
//	}

	@Override
	public MatchResultDTO saveMatchResult(MatchResultDTO matchResult) {
		MatchResultEntity matchResultEntity = matchResultMapper.toEntity(matchResult);
		matchResultEntity = matchResultRepository.save(matchResultEntity);
		return matchResultMapper.toDTO(matchResultEntity);
	}

	@Override
	public Long getWinningTeamId(Long matchId, Long winningTeamNumber) {
		List<TeamEntity> teamsInMatch = teamRepository.findByMatchId(matchId);

		if (teamsInMatch == null || teamsInMatch.size() != 2) {
			throw new IllegalArgumentException("Invalid matchId provided");
		}

		TeamEntity winningTeam = winningTeamNumber == 1 ? teamsInMatch.get(0) : teamsInMatch.get(1);

		return winningTeam.getId();
	}
	@Override
	public Long getLosingTeamId(Long matchId, Long winningTeamNumber) {
		List<TeamEntity> teamsInMatch = teamRepository.findByMatchId(matchId);

		if (teamsInMatch == null || teamsInMatch.size() != 2) {
			throw new IllegalArgumentException("Invalid matchId provided");
		}

		TeamEntity losingTeam = winningTeamNumber == 1 ? teamsInMatch.get(1) : teamsInMatch.get(0);

		return losingTeam.getId();
	}
}
