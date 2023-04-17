package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.stereotype.Service;

import com.darianngo.discordBot.dtos.MatchResultDTO;
import com.darianngo.discordBot.dtos.UserDTO;
import com.darianngo.discordBot.dtos.UserTeamDTO;
import com.darianngo.discordBot.entities.UserEntity;
import com.darianngo.discordBot.mappers.UserMapper;
import com.darianngo.discordBot.repositories.UserRepository;
import com.darianngo.discordBot.services.EloService;

@Service
public class EloServiceImpl implements EloService {
	private static final double K_FACTOR = 100;
	private static final double BETA = 200;
	private static final Double INITIAL_ELO = Double.valueOf(1200);
	private static final Double INITIAL_SIGMA = Double.valueOf(400);

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public EloServiceImpl(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public void updateElo(MatchResultDTO matchResult, List<UserDTO> usersInMatch) {
		Map<Long, List<UserDTO>> teams = groupUsersByTeam(usersInMatch);
		List<UserDTO> winningTeam = teams.get(matchResult.getWinningTeamId());
		List<UserDTO> losingTeam = teams.get(matchResult.getLosingTeamId());

		for (UserDTO winner : winningTeam) {
			double eloChange = calculateEloChange(winner, true, matchResult, losingTeam);
			updateEloForUser(winner, eloChange);
		}

		for (UserDTO loser : losingTeam) {
			double eloChange = calculateEloChange(loser, false, matchResult, winningTeam);
			updateEloForUser(loser, eloChange);
		}
	}

	private Map<Long, List<UserDTO>> groupUsersByTeam(List<UserDTO> usersInMatch) {
		Map<Long, List<UserDTO>> teams = new HashMap<>();

		for (UserDTO user : usersInMatch) {
			for (UserTeamDTO userTeam : user.getUserTeams()) {
				Long teamId = userTeam.getTeamId();
				if (!teams.containsKey(teamId)) {
					teams.put(teamId, new ArrayList<>());
				}
				teams.get(teamId).add(user);
			}
		}

		return teams;
	}

	private double winProbability(UserDTO user1, UserDTO user2) {
		double deltaMu = user1.getElo() - user2.getElo();
		double sumSigma = user1.getSigma() * user1.getSigma() + user2.getSigma() * user2.getSigma();
		double denom = Math.sqrt(2 * (BETA * BETA) + sumSigma);

		NormalDistribution normalDistribution = new NormalDistribution(0, 1);
		return normalDistribution.cumulativeProbability(deltaMu / denom);
	}

//	private SummaryStatistics calculateTeamStats(List<UserDTO> team) {
//		SummaryStatistics teamStats = new SummaryStatistics();
//
//		for (UserDTO user : team) {
//			if (user.getElo() == null || Double.isNaN(user.getElo()) || user.getElo() == 0.0 || user.getSigma() == null
//					|| Double.isNaN(user.getSigma()) || user.getSigma() == 0.0) {
//				user.setElo(INITIAL_ELO);
//				user.setSigma(INITIAL_SIGMA);
//			}
//			teamStats.addValue(user.getElo());
//			teamStats.addValue(user.getSigma() * user.getSigma());
//		}
//		return teamStats;
//	}

	private double calculateEloChange(UserDTO user, boolean isWinner, MatchResultDTO matchResult,
			List<UserDTO> opposingTeam) {
		double actualOutcome = isWinner ? 1 : 0;
		double matchWeight = calculateMatchWeight(matchResult.getWinningScore(), matchResult.getLosingScore());

		double totalEloChange = 0;
		for (UserDTO opponent : opposingTeam) {
			double winProb = winProbability(user, opponent);
			double eloChange = K_FACTOR * matchWeight * (actualOutcome - winProb);
			totalEloChange += eloChange;
		}

		return totalEloChange / opposingTeam.size();
	}

	private double calculateMatchWeight(int winningScore, int losingScore) {
		double scoreDifference = Math.abs(winningScore - losingScore);
		double totalScore = winningScore + losingScore;
		return (scoreDifference + 1) / (totalScore + 2);
	}

	private void updateEloForUser(UserDTO user, double eloChange) {
		int roundedEloChange = (int) Math.ceil(eloChange);
		user.setElo(user.getElo() + roundedEloChange);
		user.setRecentEloChange((double) roundedEloChange);
		UserEntity userEntity = userMapper.toEntity(user);
		userRepository.save(userEntity);
	}
}
