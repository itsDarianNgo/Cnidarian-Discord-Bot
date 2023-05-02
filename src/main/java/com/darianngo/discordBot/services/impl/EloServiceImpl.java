package com.darianngo.discordBot.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
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
	private static final double K_FACTOR = 350;
	private static final double BETA = 200;
	private static final Double INITIAL_ELO = Double.valueOf(1200);
	private static final Double INITIAL_SIGMA = Double.valueOf(800);
	private static final Double SIGMA_DECAY_RATE = 0.99;
	private static final Double PROGRESSIVE_SCALING_THRESHOLD_1 = 50.0;
	private static final Double PROGRESSIVE_SCALING_THRESHOLD_2 = 200.0;
	private static final Double PROGRESSIVE_SCALING_LOWER_BOUND = 0.5;
	private static final Double WIN_STREAK_BONUS_MULTIPLIER = 1.1;

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
			double eloChange = calculateEloChange(winner, true, matchResult, winningTeam, losingTeam);
			updateEloForUser(winner, eloChange, true);
		}

		for (UserDTO loser : losingTeam) {
			double eloChange = calculateEloChange(loser, false, matchResult, losingTeam, winningTeam);
			updateEloForUser(loser, eloChange, false);
		}
	}

	public Map<Long, List<UserDTO>> groupUsersByTeam(List<UserDTO> usersInMatch) {
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

	private double calculateEloChange(UserDTO user, boolean isWinner, MatchResultDTO matchResult,
			List<UserDTO> userTeam, List<UserDTO> opposingTeam) {
		double actualOutcome = isWinner ? 1 : 0;
		double matchWeight = calculateMatchWeight(matchResult.getWinningScore(), matchResult.getLosingScore());

		double winProb = winProbability(userTeam, opposingTeam);
		double totalEloChange = K_FACTOR * matchWeight * (actualOutcome - winProb);
		// Introduce scaling factor based on user's sigma
		double sigmaScalingFactor = user.getSigma() / INITIAL_SIGMA;

		// Introduce scaling factor based on average ELO of the other team and teammates
		double avgEloOtherTeam = calculateAverageElo(opposingTeam);
		double eloScalingExponent = 1.2; // You can adjust this value to change the scaling effect
		double eloScalingFactor = 1.0;

		// Introduce progressive scaling factor based on total matches played
		double progressiveScalingFactor = calculateProgressiveScalingFactor(user.getTotalMatches());

		if (isWinner) {
			eloScalingFactor = Math.pow(avgEloOtherTeam / user.getElo(), eloScalingExponent);
		} else {
			eloScalingFactor = Math.pow(user.getElo() / avgEloOtherTeam, eloScalingExponent);
		}

		// Calculate final ELO change considering scaling factors and team size
		return (totalEloChange * sigmaScalingFactor * eloScalingFactor * progressiveScalingFactor) / userTeam.size();
	}

	private double calculateProgressiveScalingFactor(Integer totalMatches) {
		if (totalMatches <= PROGRESSIVE_SCALING_THRESHOLD_1) {
			return 1.0;
		} else if (totalMatches <= PROGRESSIVE_SCALING_THRESHOLD_2) {
			double slope = (0.7 - 1.0) / (PROGRESSIVE_SCALING_THRESHOLD_2 - PROGRESSIVE_SCALING_THRESHOLD_1);
			return 1.0 + slope * (totalMatches - PROGRESSIVE_SCALING_THRESHOLD_1);
		} else {
			return PROGRESSIVE_SCALING_LOWER_BOUND;
		}
	}

	public double winProbability(List<UserDTO> team1, List<UserDTO> team2) {
		double team1Elo = calculateAverageElo(team1);
		double team1Sigma = calculateAverageSigma(team1);
		double team2Elo = calculateAverageElo(team2);
		double team2Sigma = calculateAverageSigma(team2);

		double deltaMu = team1Elo - team2Elo;
		double sumSigma = team1Sigma * team1Sigma + team2Sigma * team2Sigma;
		double denom = Math.sqrt(2 * (BETA * BETA) + sumSigma);

		NormalDistribution normalDistribution = new NormalDistribution(0, 1);
		return normalDistribution.cumulativeProbability(deltaMu / denom);
	}

	private void updateEloForUser(UserDTO user, double eloChange, boolean isWinner) {
		int roundedEloChange = (int) Math.ceil(eloChange);

		// Update winning streak and apply the bonus if necessary
		if (isWinner) {
			user.setWinningStreak((user.getWinningStreak() == null ? 0 : user.getWinningStreak()) + 1);
			if (user.getWinningStreak() >= 2) {
				roundedEloChange *= Math.pow(WIN_STREAK_BONUS_MULTIPLIER, user.getWinningStreak() - 1);
			}
		} else {
			user.setWinningStreak(0);
		}

		user.setElo(user.getElo() + roundedEloChange);
		user.setRecentEloChange((double) roundedEloChange);

		// Update totalMatches, wins, and losses
		user.setTotalMatches((user.getTotalMatches() == null ? 0 : user.getTotalMatches()) + 1);
		if (isWinner) {
			user.setWins((user.getWins() == null ? 0 : user.getWins()) + 1);
		} else {
			user.setLosses((user.getLosses() == null ? 0 : user.getLosses()) + 1);
		}

		// Update the sigma value
		user.setSigma(calculateSigmaChange(user.getSigma()));

		UserEntity userEntity = userMapper.toEntity(user);
		userRepository.save(userEntity);
	}

	private double calculateSigmaChange(double currentSigma) {
		return currentSigma * SIGMA_DECAY_RATE;
	}

	private double calculateAverageElo(List<UserDTO> team) {
		return team.stream().mapToDouble(UserDTO::getElo).average().orElse(INITIAL_ELO);
	}

	private double calculateAverageSigma(List<UserDTO> team) {
		return team.stream().mapToDouble(UserDTO::getSigma).average().orElse(INITIAL_SIGMA);
	}

	private double calculateMatchWeight(Integer winningScore, Integer losingScore) {
		double scoreDifference = Math.abs(winningScore - losingScore);
		double totalScore = winningScore + losingScore;
		return (scoreDifference + 1) / (totalScore + 2);
	}

}
